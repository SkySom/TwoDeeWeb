package io.sommers.twodee.web.simplydoom.roll

import io.sommers.twodee.web.simplydoom.model.Character

case class RollData(
    depth: Int,
    character: Option[Character]
)

object RollData {
  def apply(): RollData = RollData(0, None)
}
