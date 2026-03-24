package io.sommers.twodee.web.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.config.GoogleOAuthConfig
import io.sommers.twodee.web.logic.{AuthLogic, GoogleLogic, UserCreate, UserLogic}
import io.sommers.twodee.web.model.GoogleInfo
import io.sommers.twodee.web.model.auth.AuthTokenKind.USER
import io.sommers.twodee.web.model.request.LoginRequest
import io.sommers.twodee.web.model.response.LoginResponse
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*

case class GoogleRoute(
    googleOAuthConfig: GoogleOAuthConfig,
    googleLogic: GoogleLogic,
    userLogic: UserLogic,
    authLogic: AuthLogic
) {
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "info" => Ok(GoogleInfo(googleOAuthConfig.clientId))
    case req @ POST -> Root / "login" => login(req)
  }

  private def login(value: Request[IO]): IO[Response[IO]] = {
    for {
      loginRequest <- value.as[LoginRequest]
      googleToken <- googleLogic.validateToken(loginRequest.token)
      existingUser <- userLogic.getUserByAuthId(googleToken.sub)
      user <- existingUser.fold(
        userLogic.insertUser(
          UserCreate(
            googleToken.name.getOrElse("whoami?"),
            googleToken.image,
            googleToken.sub
          )
        )
      )(user => IO.pure(user))
      token <- authLogic.createToken(user.id.toString, USER, createdBy = user.id)
      response <- Ok(
        LoginResponse(
          token,
          user
        )
      )
    } yield response
  }
}
