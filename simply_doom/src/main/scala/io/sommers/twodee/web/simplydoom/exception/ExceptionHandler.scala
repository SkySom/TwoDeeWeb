package io.sommers.twodee.web.simplydoom.exception

import cats.Functor
import cats.syntax.all.*
import fs2.Stream
import org.http4s.{Response, Status}
import org.typelevel.log4cats.{Logger, LoggerFactory}

object ExceptionHandler {
  def apply[F[_]: Functor](
      loggerFactory: LoggerFactory[F]
  ): F[PartialFunction[Throwable, F[Response[F]]]] = for {
    logger <- loggerFactory.fromName("ExceptionHandler")
  } yield {
    case MissingPermissionException(message) => returnError(Status.Forbidden, message)(logger)
    case NotFoundException(message)          => returnError(Status.NotFound, message)(logger)
    case InvalidTokenException(message)      => returnError(Status.Unauthorized, message)(logger)
    case SheetException(message)        => returnError(Status.InternalServerError, message)(logger)
    case InvalidFieldException(message) => returnError(Status.BadRequest, message)(logger)
    case e: Exception =>
      returnError(Status.InternalServerError, "Unprepared for exception", Some(e))(logger)
  }

  private def returnError[F[_]: Functor](
      status: Status,
      message: String,
      exception: Option[Exception] = None
  )(
      logger: Logger[F]
  ): F[Response[F]] = for {
    _ <- exception.fold(logger.info(s"Returned $status with message $message"))(
      logger.error(_)(message)
    )
  } yield Response(
    status = status,
    body = Stream.emits("{\"message\": \"$message\"}".getBytes().toSeq)
  )
}
