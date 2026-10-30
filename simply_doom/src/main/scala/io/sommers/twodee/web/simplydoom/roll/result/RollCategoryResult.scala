package io.sommers.twodee.web.simplydoom.roll.result

import io.circe.Encoder
import io.sommers.twodee.web.simplydoom.roll.RollOptions
import io.sommers.twodee.web.simplydoom.roll.value.{RollValue, RollValueCategory}

case class RollCategoryResult(
    category: RollValueCategory,
    rollValues: Seq[RollValueSelect]
) derives Encoder.AsObject {
  lazy val total: Int = rollValues
    .filter(_.selected)
    .map(_.rollValue.value)
    .sum

  lazy val formula: String = rollValues
    .map(_.rollValue.toFormula)
    .mkString(" ")
}

case class RollValueSelect(rollValue: RollValue, selected: Boolean) derives Encoder.AsObject

object RollCategoryResult {
  def apply(
      category: RollValueCategory,
      rollValues: Seq[RollValue],
      rollOptions: RollOptions
  ): RollCategoryResult = {
    val categoryRollValues = rollValues.filter(_.category == category)
    val pickedRollValues = category.pickValues(categoryRollValues, rollOptions)

    RollCategoryResult(
      category,
      rollValues.map(rollValue => RollValueSelect(rollValue, pickedRollValues.exists(_ eq rollValue)))
    )
  }
}
