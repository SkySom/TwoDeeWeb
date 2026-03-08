package io.sommers.twodee.web.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.exception.NotFoundException
import io.sommers.twodee.web.logic.DoomLogic
import io.sommers.twodee.web.model.{DoomPool, DoomPoolCreate, DoomPoolUpdate}
import io.sommers.twodee.web.util.TwoDeeIO
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*

case class DoomRoute(
    doomLogic: DoomLogic
) {
  implicit val doomPoolCreateDecoder: EntityDecoder[IO, DoomPoolCreate] =
    jsonOf[IO, DoomPoolCreate]

  implicit val doomPoolUpdateDecoder: EntityDecoder[IO, DoomPoolUpdate] =
    jsonOf[IO, DoomPoolUpdate]

  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root        => listDoomPool()
    case req @ POST -> Root => createDoomPool(req)
    case req @ PUT -> Root / LongVar(doomPoolId) =>
      updateDoomPool(doomPoolId, req)
  }

  private def listDoomPool(): IO[Response[IO]] = for {
    doomPools <- doomLogic.listDoomPool()
    response <- Ok(doomPools)
  } yield response

  private def createDoomPool(request: Request[IO]): IO[Response[IO]] = for {
    doomPoolCreate <- request.as[DoomPoolCreate]
    createdDoomPool <- doomLogic.createDoomPool(doomPoolCreate)
    response <- Created(createdDoomPool)
  } yield response

  private def updateDoomPool(
      doomPoolId: Long,
      request: Request[IO]
  ): IO[Response[IO]] = for {
    doomPoolUpdate <- request.as[DoomPoolUpdate]
    doomPool <- doomLogic
      .getById(doomPoolId)
      .map(
        _.getOrElse(
          throw NotFoundException(s"No doom pool with id $doomPoolId")
        )
      )
    updatedDoomPool <- TwoDeeIO.when(doomPoolUpdate.isChanged(doomPool))(
      doomLogic.updateDoomPool(doomPoolId, doomPoolUpdate),
      None
    )
    response <- updatedDoomPool
      .map(Ok(_))
      .getOrElse(NoContent())
  } yield response
}
