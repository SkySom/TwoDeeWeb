package io.sommers.twodee.web.simplydoom.roll.dice

import cats.effect.IO
import cats.effect.std.Random
import io.sommers.twodee.web.simplydoom.roll.{DiceRoller, RollOptions}
import io.sommers.twodee.web.simplydoom.roll.value.{DiceRollValue, RollValue, RollValueCategory}

object RegularDiceRollValueCreator extends DiceRollValueCreator {
  override val id: String = "d"
  
  override def create(faces: Int, roller: DiceRoller): IO[DiceRollValue] = for {
    roll <- roller.roll(faces)
  } yield DiceRollValue(
    RegularDiceRollValueCategory,
    roll,
    faces,
    s"$id$faces"
  )
}

object ChaosDiceRollValueCreator extends DiceRollValueCreator {
  override val id: String = "cd"
  
  override def create(faces: Int, roller: DiceRoller): IO[DiceRollValue] = for {
    roll <- roller.roll(faces)
  } yield DiceRollValue(
    RegularDiceRollValueCategory,
    -roll,
    faces,
    s"$id$faces"
  )
}

object RegularDiceRollValueCategory extends RollValueCategory {

  /**
   * All lowercase, no spaces, used for localization
   *
   * @return unique Id
   */
  override def id: String = "regular_dice"

  /**
   * Orders categories for output, lower is sooner
   *
   *
   * @return priority
   */
  override def priority: Int = 20

  /**
   * @param categoryDice All Dice in a roll that are of this category
   * @param rollOptions  The roll options used for the roll, most notably the amount of kept dice
   * @return the dice that are selected
   */
  override def pickValues(categoryDice: Seq[RollValue], rollOptions: RollOptions): Seq[RollValue] = {
    val positive = categoryDice.filter(_.value > 0)
      .sortBy(_.value)
      .slice(0, rollOptions.kept)

    val negative = categoryDice.filter(_.value < 0)
      .sortBy(_.value)
      .slice(0, 2)

    positive ++ negative
  }
}
