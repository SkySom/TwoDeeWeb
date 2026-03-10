package io.sommers.twodee.web.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.logic.DoomPoolLogic
import io.sommers.twodee.web.model.{DoomPool, DoomPoolCreate, DoomPoolUpdate}
import io.sommers.twodee.web.util.TwoDeeIO
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*

case class DoomPoolRoute(
    doomPoolLogic: DoomPoolLogic
) {
  implicit val doomPoolCreateDecoder: EntityDecoder[IO, DoomPoolCreate] =
    jsonOf[IO, DoomPoolCreate]

  implicit val doomPoolUpdateDecoder: EntityDecoder[IO, DoomPoolUpdate] =
    jsonOf[IO, DoomPoolUpdate]

  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root                       => listDoomPool()
    case GET -> Root / LongVar(doomPoolId) => getDoomPool(doomPoolId)

    case req @ POST -> Root => createDoomPool(req)
    case req @ POST -> Root / LongVar(doomPoolId) / "transaction" =>
      createDoomTransaction(doomPoolId, req)

    case req @ PUT -> Root / LongVar(doomPoolId) =>
      updateDoomPool(doomPoolId, req)
  }

  private def listDoomPool(): IO[Response[IO]] = for {
    doomPools <- doomPoolLogic.listDoomPool()
    response <- Ok(doomPools)
  } yield response

  private def createDoomPool(request: Request[IO]): IO[Response[IO]] = for {
    doomPoolCreate <- request.as[DoomPoolCreate]
    createdDoomPool <- doomPoolLogic.createDoomPool(doomPoolCreate)
    response <- Created(createdDoomPool)
  } yield response

  private def getDoomPool(
      doomPoolId: Long
  ): IO[Response[IO]] = for {
    doomPool <- doomPoolLogic.getById(doomPoolId)
    response <- Ok(doomPool)
  } yield response

  private def updateDoomPool(
      doomPoolId: Long,
      request: Request[IO]
  ): IO[Response[IO]] = for {
    doomPoolUpdate <- request.as[DoomPoolUpdate]
    doomPool <- doomPoolLogic
      .getById(doomPoolId)
    updatedDoomPool <- TwoDeeIO.when(doomPoolUpdate.isChanged(doomPool))(
      doomPoolLogic.updateDoomPool(doomPoolId, doomPoolUpdate),
      None
    )
    response <- updatedDoomPool
      .map(Ok(_))
      .getOrElse(NoContent())
  } yield response

  private def createDoomTransaction(
      doomPoolId: Long,
      value: Request[IO]
  ): IO[Response[IO]] = for {
    _ <- doomPoolLogic.getById(doomPoolId)
    response <- Ok()
  } yield response
}
