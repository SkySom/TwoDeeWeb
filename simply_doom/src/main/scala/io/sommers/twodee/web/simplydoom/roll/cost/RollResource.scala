package io.sommers.twodee.web.simplydoom.roll.cost

import io.circe.{Encoder, Json}

enum RollResource {
  case PlotPoints extends RollResource
  case Doom extends RollResource
}

object RollResource {
  implicit val encoderRollResource: Encoder[RollResource] = Encoder.instance(rollResource => Json.fromString(rollResource.toString))
}