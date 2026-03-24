package io.sommers.twodee.web.logic

import cats.effect.IO
import dev.profunktor.auth.jwt.{JwtAuth, JwtToken, jwtEncode}
import dev.profunktor.auth.{JwtAuthMiddleware, jwt}
import io.sommers.twodee.web.config.AuthConfig
import io.sommers.twodee.web.exception.InvalidTokenException
import io.sommers.twodee.web.model.auth.AuthTokenKind.USER
import io.sommers.twodee.web.model.auth.{
  AuthToken,
  AuthTokenKind,
  TokenInfo,
  UserAuthToken
}
import io.sommers.twodee.web.service.AuthTokenService
import org.http4s.server.AuthMiddleware
import pdi.jwt.{JwtAlgorithm, JwtClaim}

import java.time.Clock
import scala.concurrent.duration.DurationInt

trait AuthLogic {
  def authenticate: JwtToken => JwtClaim => IO[Option[AuthToken]]

  def middleware: AuthMiddleware[IO, AuthToken]

  def createToken(
      subject: String,
      kind: AuthTokenKind,
      createdBy: Long
  ): IO[String]

  def getTokenInfoById(id: Long): IO[Option[TokenInfo]]

  def deactivateToken(id: Long): IO[Unit]
}

object AuthLogic {
  def apply(
      authConfig: AuthConfig,
      userLogic: UserLogic,
      authTokenService: AuthTokenService
  ): AuthLogic = AuthLogicImpl(authConfig, userLogic, authTokenService)
}

case class AuthLogicImpl(
    authConfig: AuthConfig,
    userLogic: UserLogic,
    authTokenService: AuthTokenService
) extends AuthLogic {
  implicit val clock: Clock = Clock.systemUTC()

  private val jwtAuth: jwt.JwtSymmetricAuth =
    JwtAuth.hmac(authConfig.secretKey.toCharArray, JwtAlgorithm.HS512)

  override def authenticate: JwtToken => JwtClaim => IO[Option[AuthToken]] =
    token => claim => findToken(token, claim)

  override val middleware: AuthMiddleware[IO, AuthToken] =
    JwtAuthMiddleware[IO, AuthToken](jwtAuth, authenticate)

  override def createToken(
      subject: String,
      kind: AuthTokenKind,
      createdBy: Long
  ): IO[String] = for {
    tokenId <- authTokenService.createToken(kind = kind, createdBy = createdBy)
    jwt <- jwtEncode[IO](
      JwtClaim(
        jwtId = Some(tokenId.toString),
        subject = Some(subject)
      ).issuedNow
        .expiresIn(8.hours.toSeconds),
      jwtAuth.secretKey,
      JwtAlgorithm.HS512
    )
  } yield jwt.value

  override def deactivateToken(id: Long): IO[Unit] = for {
    _ <- authTokenService.deactivateToken(id)
  } yield ()

  override def getTokenInfoById(id: Long): IO[Option[TokenInfo]] =
    authTokenService
      .getTokenById(id)
      .map(_.map(TokenInfo.apply))

  private def findToken(
      token: JwtToken,
      claim: JwtClaim
  ): IO[Option[AuthToken]] = {
    for {
      jwtId <- IO.fromOption(claim.jwtId.flatMap(_.toLongOption))(
        throw InvalidTokenException("Invalid access token")
      )
      tokenInfo <- authTokenService
        .getTokenById(jwtId)
        .map(_.map(TokenInfo.apply))
        .flatMap(token =>
          IO.fromOption(token.filter(_.active))(
            InvalidTokenException("Invalid access token")
          )
        )
      authToken <- transformToken(tokenInfo, claim)
    } yield authToken
  }

  private def transformToken(
      tokenInfo: TokenInfo,
      claim: JwtClaim
  ): IO[Option[AuthToken]] = tokenInfo.kind match {
    case USER => createUserToken(tokenInfo, claim.subject)
    case _    => IO.raiseError(InvalidTokenException("Invalid access token"))
  }

  private def createUserToken(
      info: TokenInfo,
      maybeId: Option[String]
  ): IO[Option[UserAuthToken]] = for {
    userId <- IO.fromOption(maybeId.flatMap(_.toLongOption))(
      InvalidTokenException()
    )
    user <- userLogic.getOptionalUser(userId)
  } yield user.map(UserAuthToken(info.id, _))
}
