package io.sommers.twodee.web.route

import cats.effect.IO
import org.http4s.*
import org.http4s.dsl.io.*

case class AuthRoute(
) {
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root =>
    Ok("Hi")
  }
}
