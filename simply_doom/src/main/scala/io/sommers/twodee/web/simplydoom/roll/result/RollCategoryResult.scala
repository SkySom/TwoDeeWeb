package io.sommers.twodee.web.simplydoom.roll.result

import io.sommers.twodee.web.simplydoom.roll.RollOptions
import io.sommers.twodee.web.simplydoom.roll.value.{RollValue, RollValueCategory}

case class RollCategoryResult(
    category: RollValueCategory,
    rollValues: Seq[(RollValue, Boolean)]
) {
  lazy val total: Int = rollValues
    .filter(_._2)
    .map(_._1.value)
    .sum

  lazy val formula: String = rollValues
    .map(_._1.toFormula)
    .mkString(" ")
}

object RollCategoryResult {
  def apply(category: RollValueCategory, rollValues: Seq[RollValue], rollOptions: RollOptions): RollCategoryResult = {
    val categoryRollValues = rollValues.filter(_.category == category)
    val pickedRollValues = category.pickValues(categoryRollValues, rollOptions)
    
    RollCategoryResult(
      category,
      rollValues.map(rollValue => (rollValue, pickedRollValues.contains(rollValue)))
    )
  }
}
