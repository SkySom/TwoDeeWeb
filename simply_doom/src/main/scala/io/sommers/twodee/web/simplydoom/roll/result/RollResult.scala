package io.sommers.twodee.web.simplydoom.roll.result

import io.circe.syntax.*
import io.circe.{Encoder, Json}
import io.sommers.twodee.web.simplydoom.roll.RollOptions
import io.sommers.twodee.web.simplydoom.roll.cost.RollCost
import io.sommers.twodee.web.simplydoom.roll.cost.RollResource.{Doom, PlotPoints}
import io.sommers.twodee.web.simplydoom.roll.value.RollValue

//noinspection ScalaWeakerAccess
case class RollResult(
    categoryResults: Seq[RollCategoryResult],
    rollCosts: Seq[RollCost],
    rollGains: Seq[RollCost]
) {
  lazy val total: Int = categoryResults.map(_.total).sum

  lazy val tier: ResultTier = ResultTier.fromTotal(this.total)
  lazy val extraordinaryTier: Option[ResultTier] = {
    val value = ResultTier.fromTotal(this.total - 7)
    if (value == ResultTier.None) None else Some(value)
  }
}

object RollResult {
  implicit val encoderRollResult: Encoder[RollResult] = Encoder.instance { rollResult =>
    Json.obj(
      ("categoryResults", rollResult.categoryResults.asJson),
      ("rollCosts", rollResult.rollCosts.asJson),
      ("rollGains", rollResult.rollGains.asJson),
      ("total", Json.fromInt(rollResult.total)),
      ("tier", Json.fromString(rollResult.tier.toString)),
      ("extraordinaryTier", Json.fromStringOrNull(rollResult.extraordinaryTier.map(_.toString)))
    )
  }

  def apply(rollValues: Seq[RollValue], rollOptions: RollOptions): RollResult = {

    val categories = rollValues.map(_.category).distinct

    val categoryResults = categories
      .map(RollCategoryResult(_, rollValues, rollOptions))
      .sortBy(_.category.priority)

    val rollCosts = rollValues
      .flatMap(_.rollCost)
      .groupBy(_.resource)
      .map(value => RollCost(value._1, value._2.map(_.amount).sum))
      .toSeq

    val numberOfOnes = rollValues.count(_.value == 1)
    val rollGains =
      if (numberOfOnes > 0) {
        Seq(
          RollCost(Doom, numberOfOnes),
          RollCost(PlotPoints, 1)
        )
      } else Seq()

    RollResult(categoryResults, rollCosts, rollGains)
  }
}
