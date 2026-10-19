package io.sommers.twodee.web.simplydoom.roll.dice

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.value.{DiceRollValue, RollValue, RollValueCategory}
import io.sommers.twodee.web.simplydoom.roll.{DiceRoller, RollOptions}

object KeptDiceRollValueCreator extends DiceRollValueCreator {
  override val id: String = "kd"

  override def create(faces: Int, roller: DiceRoller): IO[DiceRollValue] = for {
    roll <- roller.roll(faces)
  } yield DiceRollValue(
    RegularDiceRollValueCategory,
    roll,
    faces,
    s"$id$faces"
  )
}

object KeptDiceRollValueCategory extends RollValueCategory {

  /** All lowercase, no spaces, used for localization
    *
    * @return
    *   unique Id
    */
  override def id: String = "kept_dice"

  /** Orders categories for output, lower is sooner
    *
    * @return
    *   priority
    */
  override def priority: Int = 60

  /** @param categoryDice
    *   All Dice in a roll that are of this category
    * @param rollOptions
    *   The roll options used for the roll, most notably the amount of kept dice
    * @return
    *   the dice that are selected
    */
  override def pickValues(
      categoryDice: Seq[RollValue],
      rollOptions: RollOptions
  ): Seq[RollValue] = categoryDice
}
