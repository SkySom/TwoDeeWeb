package io.sommers.twodee.web.simplydoom.roll.token

import io.sommers.twodee.web.simplydoom.roll.value.RollValue

trait RollToken {
  def provide(): Seq[RollValue]
}
