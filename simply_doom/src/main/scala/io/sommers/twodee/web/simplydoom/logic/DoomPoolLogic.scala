package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.exception.NotFoundException
import io.sommers.twodee.web.simplydoom.model.DoomPool
import io.sommers.twodee.web.simplydoom.service.DoomPoolService

trait DoomPoolLogic {
  def create(name: String, starting: Int): IO[DoomPool]

  def getById(id: Long): IO[DoomPool]

  def changeDoomAmount(id: Long, amount: Int): IO[Unit]

  def list(filters: Map[String, String]): IO[List[DoomPool]]
}

object DoomPoolLogic {
  def apply(doomPoolService: DoomPoolService): DoomPoolLogic = DoomPoolLogicImpl(doomPoolService)
}

case class DoomPoolLogicImpl(
    doomPoolService: DoomPoolService
) extends DoomPoolLogic {
  override def create(name: String, starting: Int): IO[DoomPool] = for {
    id <- doomPoolService.createDoomPool(name, starting)
    doomPool <- this.getById(id)
  } yield doomPool

  override def getById(id: Long): IO[DoomPool] = doomPoolService
    .getDoomPool(id)
    .flatMap(
      _.fold[IO[DoomPool]](
        IO.raiseError(NotFoundException(s"No Doom Pool with $id"))
      )(doomPool => IO.pure(DoomPool(doomPool._1, doomPool._2, doomPool._3)))
    )

  override def changeDoomAmount(id: Long, amount: Int): IO[Unit] =
    doomPoolService
      .changeDoomAmount(id, amount)
      .map(updated =>
        IO.raiseWhen(updated == 0)(NotFoundException(s"No Doom Pool with $id"))
      )

  override def list(filters: Map[String, String]): IO[List[DoomPool]] =
    doomPoolService
      .list(filters)
      .map(_.map(doomPool => DoomPool(doomPool._1, doomPool._2, doomPool._3)))
}
