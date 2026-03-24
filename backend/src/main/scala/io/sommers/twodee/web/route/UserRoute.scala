package io.sommers.twodee.web.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.logic.{AuthLogic, UserLogic}
import io.sommers.twodee.web.model.auth.{AuthToken, UserAuthToken}
import io.sommers.twodee.web.model.response.WhoAmIResponse
import io.sommers.twodee.web.model.user.User
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import org.http4s.{AuthedRoutes, HttpRoutes, Response}

case class UserRoute(
    userLogic: UserLogic,
    authLogic: AuthLogic
) {
  def routes: HttpRoutes[IO] =
    authLogic.middleware(AuthedRoutes.of[AuthToken, IO] {
      case GET -> Root / "whoami" as authToken => whoAmI(authToken)
    })

  private def whoAmI(token: AuthToken): IO[Response[IO]] = UserAuthToken
    .fromToken(token)
    .fold(NotFound())(userToken => Ok(WhoAmIResponse(userToken.user)))
}
