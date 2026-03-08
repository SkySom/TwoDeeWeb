package io.sommers.twodee.web.logic

import cats.effect.IO
import io.sommers.twodee.web.model.{DoomPool, DoomPoolCreate, DoomPoolUpdate}
import io.sommers.twodee.web.service.DoomService
import io.sommers.twodee.web.util.TwoDeeIO

trait DoomLogic {
  def createDoomPool(doomPoolCreate: DoomPoolCreate): IO[DoomPool]

  def listDoomPool(): IO[List[DoomPool]]

  def addDoomToPool(
      doomPoolId: Long,
      amount: Int,
      note: Option[String]
  ): IO[DoomPool]

  def updateDoomPool(
      id: Long,
      doomPoolUpdate: DoomPoolUpdate
  ): IO[Option[DoomPool]]

  def getById(id: Long): IO[Option[DoomPool]]
}

case class DoomLogicImpl(
    doomService: DoomService
) extends DoomLogic {

  override def createDoomPool(
      doomPoolCreate: DoomPoolCreate
  ): IO[DoomPool] =
    doomService
      .insertDoomPool(doomPoolCreate.name, 0)
      .map(id => DoomPool(id, doomPoolCreate.name, 0))

  override def listDoomPool(): IO[List[DoomPool]] =
    doomService
      .listDoomPools()
      .map(_.map(DoomPool.apply))

  override def addDoomToPool(
      doomPoolId: Long,
      amount: Int,
      note: Option[String]
  ): IO[DoomPool] = for {
    _ <- doomService.insertDoomTransaction(doomPoolId, amount, note)
    pool <- doomService
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
      updatedCount <- doomService.updateDoomPool(id, doomPoolUpdate.name.get)
      updatedPool <- TwoDeeIO.when(updatedCount > 0)(
        doomService.getDoomPool(id),
        None
      )
    } yield updatedPool.map(DoomPool.apply)
  }

  override def getById(id: Long): IO[Option[DoomPool]] =
    doomService
      .getDoomPool(id)
      .map(_.map(DoomPool.apply))
}
