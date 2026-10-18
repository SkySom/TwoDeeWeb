package io.sommers.twodee.web.simplydoom.roll.value

import io.sommers.twodee.web.simplydoom.roll.cost.RollCost

case class DiceRollValue(
    override val category: RollValueCategory,
    override val value: Int,
    maxValue: Int,
    override val toFormula: String,
    override val rollCost: Option[RollCost] = None
) extends RollValue {
  
}
