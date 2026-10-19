package io.sommers.twodee.web.simplydoom.roll.token

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.DiceRoller
import io.sommers.twodee.web.simplydoom.roll.value.RollValue

trait RollToken {
  def roll(diceRoller: DiceRoller): IO[Seq[RollValue]]
  
  def toFormula: String
}
