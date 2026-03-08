package io.sommers.twodee.web.route

import cats.effect.IO
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.Router
import org.http4s.server.staticcontent.resourceServiceBuilder

case class UIRoute() {
  private val staticAssetsService: HttpRoutes[IO] = resourceServiceBuilder[IO]("/static").toRoutes
  
  def router: HttpRoutes[IO] = Router.define(
    "/" -> routes
  )(default = staticAssetsService)

  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case request @ GET -> Root =>
    StaticFile
      .fromResource("/static/index.html", Some(request))
      .getOrElseF(InternalServerError())
  }
}
