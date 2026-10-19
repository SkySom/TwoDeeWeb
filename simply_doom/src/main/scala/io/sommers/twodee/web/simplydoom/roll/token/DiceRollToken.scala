package io.sommers.twodee.web.simplydoom.roll.token

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.DiceRoller
import io.sommers.twodee.web.simplydoom.roll.dice.{AssuredDiceRollValueModifier, DiceRollValueCreator, DiceRollValueModifier}
import io.sommers.twodee.web.simplydoom.roll.value.RollValue

import scala.util.matching.Regex

case class DiceRollToken(
    diceRollValueCreator: DiceRollValueCreator,
    number: Int,
    faces: Int
) extends RollToken {

  override def roll(roller: DiceRoller): IO[Seq[RollValue]] = IO.parSequence {
    LazyList.from(1 until number)
      .map(_ => diceRollValueCreator.create(faces, roller))
  }

  override val toFormula: String = s"$number${diceRollValueCreator.id}$faces"
}

object DiceRollTokenProvider extends RollTokenProvider[DiceRollToken] {
  private val fullPattern: Regex = ("""^(\d*)([""" + DiceRollValueModifier.letters + """]*)([""" + DiceRollValueCreator.letters + """]{1})(\d*)$""").r
  override def provide(token: String, suggest: Boolean): RollTokenProvided[DiceRollToken] = ???
}
