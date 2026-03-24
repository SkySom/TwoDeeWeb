package io.sommers.twodee.web.route

import cats.effect.IO
import io.sommers.twodee.web.exception.{
  MissingPermissionException,
  NotFoundException
}
import io.sommers.twodee.web.logic.AuthLogic
import io.sommers.twodee.web.model.auth.{AuthToken, UserAuthToken}
import org.http4s.*
import org.http4s.dsl.io.*

case class AuthRoute(
    authLogic: AuthLogic
) {
  def routes: HttpRoutes[IO] =
    authLogic.middleware(AuthedRoutes.of[AuthToken, IO] {
      case DELETE -> Root / "token" / LongVar(tokenId) as authToken =>
        deleteToken(authToken, tokenId)
      case DELETE -> Root / "token" as authToken =>
        deleteToken(authToken, authToken.id)
    })

  private def deleteToken(
      authToken: AuthToken,
      tokenId: Long
  ): IO[Response[IO]] = for {
    userToken <- authToken match {
      case token: UserAuthToken => IO(token)
      case _ =>
        IO.raiseError(MissingPermissionException("Only users can delete"))
    }
    tokenInfo <- authLogic
      .getTokenInfoById(tokenId)
      .flatMap(IO.fromOption(_)(NotFoundException("No Token Found")))
    _ <-
      if (tokenInfo.createdBy.equals(userToken.user.id)) {
        authLogic.deactivateToken(tokenId)
      } else IO.raiseError(NotFoundException("No Token Found"))
    noContent <- NoContent()
  } yield noContent
}
