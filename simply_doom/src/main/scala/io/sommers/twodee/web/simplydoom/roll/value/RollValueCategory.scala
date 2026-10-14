package io.sommers.twodee.web.simplydoom.roll.value

import io.sommers.twodee.web.simplydoom.roll.RollOptions

trait RollValueCategory {
  /**
   * All lowercase, no spaces, used for localization
   * 
   * @return unique Id
   */
  def id: String

  /**
   * Orders categories for output, lower is sooner
   * 
   * @return priority
   */
  def priority: Int

  /**
   * @param categoryDice All Dice in a roll that are of this category
   * @param rollOptions The roll options used for the roll, most notably the amount of kept dice
   * @return the dice that are selected
   */
  def pickValues(categoryDice: Seq[RollValue], rollOptions: RollOptions): Seq[RollValue]
}
