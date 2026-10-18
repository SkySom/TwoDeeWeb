package io.sommers.twodee.web.simplydoom.roll.dice

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.DiceRoller
import io.sommers.twodee.web.simplydoom.roll.value.DiceRollValue

trait DiceRollValueCreator {
  val id: String

  def create(faces: Int, diceRoller: DiceRoller): IO[DiceRollValue]
}
