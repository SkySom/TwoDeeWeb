package io.sommers.twodee.web.exception

import cats.effect.IO
import org.http4s.Response

trait EndpointException {
  def asResponse: IO[Response[IO]]
}
