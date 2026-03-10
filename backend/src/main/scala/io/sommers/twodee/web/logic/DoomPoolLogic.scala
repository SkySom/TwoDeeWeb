package io.sommers.twodee.web.logic

import cats.effect.IO
import io.sommers.twodee.web.exception.NotFoundException
import io.sommers.twodee.web.model.{DoomPool, DoomPoolCreate, DoomPoolUpdate}
import io.sommers.twodee.web.service.DoomPoolService
import io.sommers.twodee.web.util.TwoDeeIO

trait DoomPoolLogic {
  def createDoomPool(doomPoolCreate: DoomPoolCreate): IO[DoomPool]

  def listDoomPool(): IO[List[DoomPool]]

  def createDoomPoolTransaction(
      doomPoolId: Long,
      amount: Int,
      note: Option[String]
  ): IO[DoomPool]

  def updateDoomPool(
      id: Long,
      doomPoolUpdate: DoomPoolUpdate
  ): IO[Option[DoomPool]]

  def getOptionalById(id: Long): IO[Option[DoomPool]]

  def getById(id: Long): IO[DoomPool]
}

case class DoomPoolLogicImpl(
    doomPoolService: DoomPoolService
) extends DoomPoolLogic {

  override def createDoomPool(
      doomPoolCreate: DoomPoolCreate
  ): IO[DoomPool] =
    doomPoolService
      .insertDoomPool(doomPoolCreate.name, 0)
      .map(id => DoomPool(id, doomPoolCreate.name, 0))

  override def listDoomPool(): IO[List[DoomPool]] =
    doomPoolService
      .listDoomPools()
      .map(_.map(DoomPool.apply))

  override def createDoomPoolTransaction(
      doomPoolId: Long,
      amount: Int,
      note: Option[String]
  ): IO[DoomPool] = for {
    _ <- doomPoolService.insertDoomTransaction(doomPoolId, amount, note)
    pool <- doomPoolService
      .getDoomPool(doomPoolId)
      .map(
        _.map(DoomPool.apply).getOrElse(
          throw new IllegalStateException("No doom pool found after insert?")
        )
      )
  } yield pool

  override def updateDoomPool(
      id: Long,
      doomPoolUpdate: DoomPoolUpdate
  ): IO[Option[DoomPool]] = {
    for {
      updatedCount <- doomPoolService.updateDoomPool(
        id,
        doomPoolUpdate.name.get
      )
      updatedPool <- TwoDeeIO.when(updatedCount > 0)(
        doomPoolService.getDoomPool(id),
        None
      )
    } yield updatedPool.map(DoomPool.apply)
  }

  override def getOptionalById(id: Long): IO[Option[DoomPool]] =
    doomPoolService
      .getDoomPool(id)
      .map(_.map(DoomPool.apply))

  override def getById(id: Long): IO[DoomPool] = getOptionalById(id)
    .flatMap(IO.fromOption(_)(NotFoundException(s"No Doom Pool with id $id")))
}
