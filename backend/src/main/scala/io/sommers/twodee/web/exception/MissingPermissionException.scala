package io.sommers.twodee.web.exception

import cats.effect.IO
import org.http4s.Response
import org.http4s.dsl.io.*


case class MissingPermissionException(message: String)
    extends Exception(message)
    with EndpointException {
  override def asResponse: IO[Response[IO]] = Forbidden(message)
}
