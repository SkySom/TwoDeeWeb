package io.sommers.twodee.web.simplydoom.roll.value

import io.circe.syntax.*
import io.circe.generic.auto._
import io.circe.{Encoder, Json}
import io.sommers.twodee.web.simplydoom.roll.cost.RollCost

trait RollValue {
  def category: RollValueCategory

  def value: Int

  def toFormula: String

  def rollCost: Option[RollCost]
}

object RollValue {
  implicit val rollValueEncoder: Encoder[RollValue] = Encoder.instance[RollValue] { rollValue =>
    Json.obj(
      ("category", rollValue.category.asJson),
      ("value", Json.fromBigInt(rollValue.value)),
      ("formula", Json.fromString(rollValue.toFormula)),
      ("rollCost", rollValue.rollCost.asJson)
    )
  }
}
