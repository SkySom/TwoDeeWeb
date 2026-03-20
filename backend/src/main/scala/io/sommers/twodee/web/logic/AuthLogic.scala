package io.sommers.twodee.web.logic

import cats.effect.IO
import dev.profunktor.auth.jwt.{JwtAuth, JwtToken}
import io.sommers.twodee.web.model.user.User
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import dev.profunktor.auth.{JwtAuthMiddleware, jwt}
import io.sommers.twodee.web.config.AuthConfig
import io.sommers.twodee.web.exception.InvalidTokenException
import org.http4s.server.AuthMiddleware

import java.time.Clock

trait AuthLogic {
  def authenticate: JwtToken => JwtClaim => IO[Option[User]]

  def middleware: AuthMiddleware[IO, User]
  
  def createToken(subject: String): IO[String]
}

object AuthLogic {
  def apply(authConfig: AuthConfig, userLogic: UserLogic): AuthLogic = AuthLogicImpl(authConfig, userLogic)
}

case class AuthLogicImpl(
    authConfig: AuthConfig,
    userLogic: UserLogic
) extends AuthLogic {
  implicit val clock: Clock = Clock.systemUTC()
  
  private val jwtAuth: jwt.JwtSymmetricAuth = JwtAuth.hmac(authConfig.secretKey.toCharArray, JwtAlgorithm.HS512)
  
  override def authenticate: JwtToken => JwtClaim => IO[Option[User]] = token => claim => findUser(token, claim)
  
  override val middleware: AuthMiddleware[IO, User] = JwtAuthMiddleware[IO, User](jwtAuth, authenticate)

  override def createToken(subject: String): IO[String] = {
    IO.pure(Jwt.encode(JwtClaim(subject = Some(subject)).issuedNow, authConfig.secretKey, JwtAlgorithm.HS512))
  }
  
  private def findUser(token: JwtToken, claim: JwtClaim): IO[Option[User]] = for {
    id <- IO.fromOption(claim.subject)(throw InvalidTokenException("No subject on token"))
    user <- userLogic.getOptionalUser(id.toLong)
  } yield user
}
