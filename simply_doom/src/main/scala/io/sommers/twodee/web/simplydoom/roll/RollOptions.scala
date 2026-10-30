package io.sommers.twodee.web.simplydoom.roll

import io.circe.{Decoder, HCursor}

case class RollOptions(
    kept: Int = 2,
    test: Boolean = false
) {}

object RollOptions {
  implicit val decoder: Decoder[RollOptions] = (c: HCursor) =>
    for {
      kept <- c.getOrElse[Int]("kept")(2)
      test <- c.getOrElse[Boolean]("test")(false)
    } yield RollOptions(kept, test)

  val default: RollOptions = new RollOptions()

  def apply(): RollOptions = default
}
