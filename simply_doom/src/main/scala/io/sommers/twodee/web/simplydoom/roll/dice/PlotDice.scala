package io.sommers.twodee.web.simplydoom.roll.dice

import cats.effect.IO
import cats.effect.std.Random
import io.sommers.twodee.web.simplydoom.roll.cost.RollCost
import io.sommers.twodee.web.simplydoom.roll.cost.RollResource.PlotPoints
import io.sommers.twodee.web.simplydoom.roll.value.{DiceRollValue, RollValue, RollValueCategory}
import io.sommers.twodee.web.simplydoom.roll.{DiceRoller, RollOptions}

object PlotDiceRollValueCreator extends DiceRollValueCreator {

  override val id: String = "pd"

  override def create(faces: Int, roller: DiceRoller): IO[DiceRollValue] = for {
    roll <- roller.roll(faces)
  } yield DiceRollValue(
    PlotDiceRollValueCategory,
    roll,
    faces,
    s"$id$faces",
    Some(RollCost(PlotPoints, faces / 2))
  )
}

object EnhancedDiceRollValueCreator extends DiceRollValueCreator {
  override val id: String = "ed"

  override def create(faces: Int, diceRoller: DiceRoller): IO[DiceRollValue] = for {
    roll <- diceRoller.roll(faces)
  } yield DiceRollValue(
    PlotDiceRollValueCategory,
    roll,
    faces,
    s"$id$faces"
  )
}

object PlotDiceRollValueCategory extends RollValueCategory {

  /** All lowercase, no spaces, used for localization
    *
    * @return
    *   unique Id
    */
  override def id: String = "plot_dice"

  /** Orders categories for output, lower is sooner
    *
    * @return
    *   priority
    */
  override def priority: Int = 30

  /** @param categoryDice
    *   All Dice in a roll that are of this category
    * @param rollOptions
    *   The roll options used for the roll, most notably the amount of kept dice
    * @return
    *   the dice that are selected
    */
  override def pickValues(categoryDice: Seq[RollValue], rollOptions: RollOptions): Seq[RollValue] =
    categoryDice.sortBy(_.value) match {
      case (head: DiceRollValue) :: Nil =>
        Seq(head.copy(value = Math.max(head.value, head.maxValue / 2)))
      case values => values.slice(0, 2)
    }
}
