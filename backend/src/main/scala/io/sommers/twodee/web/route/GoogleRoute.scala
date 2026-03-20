package io.sommers.twodee.web.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.config.GoogleOAuthConfig
import io.sommers.twodee.web.logic.GoogleLogic
import io.sommers.twodee.web.model.GoogleInfo
import io.sommers.twodee.web.model.request.LoginRequest
import io.sommers.twodee.web.model.response.LoginResponse
import io.sommers.twodee.web.model.user.LoggedInUser
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*

case class GoogleRoute(
    googleOAuthConfig: GoogleOAuthConfig,
    googleLogic: GoogleLogic
) {
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "info" => Ok(GoogleInfo(googleOAuthConfig.clientId))
    case req @ POST -> Root / "login" => login(req)
  }

  private def login(value: Request[IO]): IO[Response[IO]] = {
    for {
      loginRequest <- value.as[LoginRequest]
      googleToken <- googleLogic.validateToken(loginRequest.token)
      response <- Ok(
        LoginResponse(
          LoggedInUser(
            loginRequest.token,
            googleToken.sub,
            googleToken.image,
            List()
          )
        )
      )
    } yield response
  }
}
