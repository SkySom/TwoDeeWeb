package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.model.{Character, DoomPool}
import io.sommers.twodee.web.simplydoom.roll.cost.{RollCost, RollResource}
import io.sommers.twodee.web.simplydoom.roll.result.RollResult
import io.sommers.twodee.web.simplydoom.roll.{FormulaRoller, RollData, RollOptions}
import org.typelevel.log4cats.{Logger, LoggerFactory}

trait RollLogic {
  def roll(
      formula: String,
      rollOptions: RollOptions,
      character: Option[Character],
      doomPool: Option[DoomPool]
  ): IO[RollResult]
}

object RollLogic {
  def apply(characterLogic: CharacterLogic, doomPoolLogic: DoomPoolLogic)(implicit
      loggerFactory: LoggerFactory[IO]
  ): IO[RollLogic] = for {
    logger <- loggerFactory.fromClass(classOf[RollLogicImpl])
  } yield RollLogicImpl(characterLogic, doomPoolLogic, logger)
}

case class RollLogicImpl(
    characterLogic: CharacterLogic,
    doomPoolLogic: DoomPoolLogic,
    logger: Logger[IO]
) extends RollLogic {

  override def roll(
      formula: String,
      rollOptions: RollOptions,
      character: Option[Character],
      doomPool: Option[DoomPool]
  ): IO[RollResult] = {
    val rollResultIO = FormulaRoller(formula, RollData(0, character), rollOptions)

    val costIO = if (rollOptions.test) {
      IO.unit
    } else {
      for {
        rollResult <- rollResultIO
        _ <- IO.traverse(rollResult.rollCosts)(handleRollCost(_, character, doomPool))
        _ <- IO.traverse(rollResult.rollGains)(handleRollGains(_, character, doomPool))
      } yield ()
    }

    costIO.handleErrorWith { throwable =>
      for {
        _ <- logger.error(throwable)(s"Failed to handle costs for $formula")
      } yield ()
    } >> rollResultIO
  }

  private def handleRollCost(
      rollCost: RollCost,
      character: Option[Character],
      doomPool: Option[DoomPool]
  ): IO[Unit] = {
    rollCost.resource match {
      case RollResource.PlotPoints =>
        character
          .map(_.id)
          .map(characterLogic.changePlotPoints(_, -rollCost.amount))
          .getOrElse(IO.unit)
      case RollResource.Doom =>
        doomPool
          .map(_.id)
          .map(doomPoolLogic.changeDoomAmount(_, -rollCost.amount))
          .getOrElse(IO.unit)
    }
  }

  private def handleRollGains(
      rollCost: RollCost,
      character: Option[Character],
      doomPool: Option[DoomPool]
  ): IO[Unit] = {
    rollCost.resource match {
      case RollResource.PlotPoints =>
        character
          .map(_.id)
          .map(characterLogic.changePlotPoints(_, rollCost.amount))
          .getOrElse(IO.unit)
      case RollResource.Doom =>
        doomPool
          .map(_.id)
          .map(doomPoolLogic.changeDoomAmount(_, rollCost.amount))
          .getOrElse(IO.unit)
    }
  }
}
