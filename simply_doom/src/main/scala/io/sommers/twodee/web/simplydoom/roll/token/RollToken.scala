package io.sommers.twodee.web.simplydoom.roll.token

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.value.RollValue

trait RollToken {
  def provide(): IO[Seq[RollValue]]
}
