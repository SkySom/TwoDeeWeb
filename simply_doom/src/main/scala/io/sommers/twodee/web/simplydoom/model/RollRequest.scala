package io.sommers.twodee.web.simplydoom.model

import io.circe.{Decoder, HCursor}
import io.sommers.twodee.web.simplydoom.roll.RollOptions

case class RollRequest(
    formula: String,
    options: RollOptions = RollOptions(),
    characterId: Option[Long] = None,
    doomPoolId: Option[Long] = None
) {}

object RollRequest {
  implicit val decoder: Decoder[RollRequest] = (c: HCursor) =>
    for {
      formula <- c.get[String]("formula")
      options <- c.get[Option[RollOptions]]("options")
      characterId <- c.get[Option[Long]]("characterId")
      doomPoolId <- c.get[Option[Long]]("doomPoolId")
    } yield RollRequest(formula, options.getOrElse(RollOptions()), characterId, doomPoolId)
}
