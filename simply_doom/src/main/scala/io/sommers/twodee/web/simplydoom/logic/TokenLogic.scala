package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import dev.profunktor.auth.jwt.{JwtAuth, JwtToken, jwtEncode}
import dev.profunktor.auth.{JwtAuthMiddleware, jwt}
import io.chrisdavenport.mules.{Cache, MemoryCache, TimeSpec}
import io.sommers.twodee.web.simplydoom.AuthConfig
import io.sommers.twodee.web.simplydoom.exception.{
  InvalidFieldException,
  InvalidTokenException,
  NotFoundException
}
import io.sommers.twodee.web.simplydoom.model.Token
import io.sommers.twodee.web.simplydoom.service.TokenService
import org.http4s.server.AuthMiddleware
import pdi.jwt.{JwtAlgorithm, JwtClaim}

import java.time.Clock
import scala.concurrent.duration.DurationInt

trait TokenLogic {
  def authenticate: JwtToken => JwtClaim => IO[Option[Token]]

  def middleware: AuthMiddleware[IO, Token]

  def createToken(
      name: String,
      userId: Long
  ): IO[Token]

  def deleteToken(id: Long): IO[Unit]

  def getToken(id: Long): IO[Token]
}

object TokenLogic {
  def apply(
      authConfig: AuthConfig,
      userLogic: UserLogic,
      tokenService: TokenService
  ): IO[TokenLogic] = for {
    tokenCache <- MemoryCache.ofConcurrentHashMap[IO, Long, Token](
      TimeSpec.fromDuration(1.hour)
    )
  } yield TokenLogicImpl(authConfig, userLogic, tokenService, tokenCache)
}

case class TokenLogicImpl(
    authConfig: AuthConfig,
    userLogic: UserLogic,
    tokenService: TokenService,
    tokenCache: Cache[IO, Long, Token]
) extends TokenLogic {
  implicit val clock: Clock = Clock.systemUTC()

  private val jwtAuth: jwt.JwtSymmetricAuth =
    JwtAuth.hmac(authConfig.secretKey.toCharArray, JwtAlgorithm.HS512)

  override def authenticate: JwtToken => JwtClaim => IO[Option[Token]] =
    token => claim => findToken(token, claim)

  override def middleware: AuthMiddleware[IO, Token] =
    JwtAuthMiddleware[IO, Token](jwtAuth, authenticate)

  override def createToken(
      name: String,
      userId: Long
  ): IO[Token] = for {
    user <- userLogic
      .getUser(userId)
      .adaptError { case NotFoundException(message) =>
        InvalidFieldException(s"Invalid userId: $message")
      }
    tokenId <- tokenService.create(name, user.id)
    token <- tokenService
      .getById(tokenId)
      .flatMap(IO.fromOption(_)(NotFoundException("Failed to find newly created token")))
    jwt <- jwtEncode[IO](
      JwtClaim(
        jwtId = Some(tokenId.toString)
      ).issuedNow,
      jwtAuth.secretKey,
      JwtAlgorithm.HS512
    )
  } yield Token(
    Some(jwt.value),
    token._1,
    token._2,
    user,
    token._4,
    token._5
  )

  override def deleteToken(id: Long): IO[Unit] =
    tokenService
      .delete(id)
      .map(updated => IO.raiseWhen(updated == 0)(NotFoundException(s"No Token with $id")))

  override def getToken(id: Long): IO[Token] = for {
    cachedToken <- tokenCache.lookup(id)
    token <- cachedToken.fold(pullTokenFromDB(id))(IO.pure)
  } yield token

  private def pullTokenFromDB(id: Long): IO[Token] = for {
    tokenOpt <- tokenService.getById(id)
    tokenRow <- IO.fromOption(tokenOpt)(NotFoundException(s"No Token with $id"))
    user <- userLogic.getUser(tokenRow._1)
    token <- IO.pure(
      Token(
        None,
        tokenRow._1,
        tokenRow._2,
        user,
        tokenRow._4,
        tokenRow._5
      )
    )
    _ <- tokenCache.insert(id, token)
  } yield token

  private def findToken(
      token: JwtToken,
      claim: JwtClaim
  ): IO[Option[Token]] = {
    for {
      jwtId <- IO.fromOption(claim.jwtId.flatMap(_.toLongOption))(
        throw InvalidTokenException()
      )
      token <- this
        .getToken(jwtId)
        .flatMap(token =>
          if (token.active) {
            IO.pure(token)
          } else {
            IO.raiseError(InvalidTokenException())
          }
        )
        .handleErrorWith { case e: NotFoundException =>
          IO.raiseError(InvalidTokenException())
        }
    } yield Some(token)
  }

}
