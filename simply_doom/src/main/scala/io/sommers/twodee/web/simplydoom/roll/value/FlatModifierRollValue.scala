package io.sommers.twodee.web.simplydoom.roll.value

import io.sommers.twodee.web.simplydoom.roll.RollOptions
import io.sommers.twodee.web.simplydoom.roll.cost.RollResourceCost

case class FlatModifierRollValue(
    amount: Int
) extends RollValue {

  override def category: RollValueCategory = FlatModifierValueCategory

  override def value: Int = amount

  override def maxValue: Int = amount

  override def toFormula: String = if (this.value >= 0) s"+$amount" else s"-$amount"

  override def rollCost: Option[RollResourceCost] = None
}

object FlatModifierValueCategory extends RollValueCategory {

  /**
   * All lowercase, no spaces, used for localization
   *
   * @return unique Id
   */
  override def id: String = "flat_modifier"

  /**
   * Orders categories for output, lower is sooner
   *
   *
   * @return priority
   */
  override def priority: Int = 100

  /**
   * @param categoryDice All Dice in a roll that are of this category
   * @param rollOptions  The roll options used for the roll, most notably the amount of kept dice
   * @return the dice that are selected
   */
  override def pickValues(categoryDice: Seq[RollValue], rollOptions: RollOptions): Seq[RollValue] = categoryDice
}
