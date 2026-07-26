package io.sommers.twodee.web.simplydoom.route

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

private case class HealthRoute () {
  
  private def routes: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root => Ok()
  }
}

object HealthRoute {
  def apply(): HttpRoutes[IO] = new HealthRoute().routes
}