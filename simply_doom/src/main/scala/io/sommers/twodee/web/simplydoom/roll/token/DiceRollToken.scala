package io.sommers.twodee.web.simplydoom.roll.token

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.DiceRoller
import io.sommers.twodee.web.simplydoom.roll.dice.{DiceRollValueCreator, DiceRollValueModifier}
import io.sommers.twodee.web.simplydoom.roll.value.RollValue

import scala.util.matching.Regex

case class DiceRollToken(
    diceRollValueCreator: DiceRollValueCreator,
    number: Int,
    faces: Int,
    modifier: Option[DiceRollValueModifier] = None
) extends RollToken {

  override def roll(roller: DiceRoller): IO[Seq[RollValue]] = for {
    original <- IO.parSequence {
      LazyList
        .from(1 until number + 1)
        .map(_ => diceRollValueCreator.create(faces, roller))
    }
  } yield modifier.fold(original)(_.modify(original))

  override val toFormula: String =
    s"$number${modifier.fold("")(_.letter)}${diceRollValueCreator.id}$faces"
}

object DiceRollToken {
  def apply(
      diceRollValueCreator: DiceRollValueCreator,
      number: Int,
      faces: Int,
      modifier: DiceRollValueModifier
  ): DiceRollToken = DiceRollToken(diceRollValueCreator, number, faces, Some(modifier))
}

object DiceRollTokenProvider extends RollTokenProvider[DiceRollToken] {
  private val fullPattern: Regex =
    ("""^(\d*)([""" + DiceRollValueModifier.letters + """]?)([""" + DiceRollValueCreator.letters + """]{1})(\d+)$""").r

  private val suggestPattern: Regex =
    ("""^(\d*)([""" + DiceRollValueModifier.letters + """]?)([""" + DiceRollValueCreator.lettersNoD + """]?)(d?)(\d*)$""").r

  private val dice = Seq(4, 6, 8, 10, 12)
  private val dice2 = Seq(10, 12)

  override def provide(token: String, suggest: Boolean): RollTokenProvided[DiceRollToken] =
    (token, suggest) match {
      case (fullPattern(number, modifier, creator, faces), true) if faces == "1" =>
        RollTokenSuggestion(Seq(s"${token}0", s"${token}2"))
      case (fullPattern(number, modifier, creator, faces), true) if faces == "2" =>
        RollTokenSuggestion(Seq(s"${token}0"))
      case (fullPattern(number, modifier, creator, faces), _) =>
        val parsedNumber = if (number.isEmpty) 1 else number.toInt
        DiceRollValueCreator.map
          .get(creator)
          .map(DiceRollToken(_, parsedNumber, faces.toInt, DiceRollValueModifier.map.get(modifier)))
          .getOrElse(RollTokenError(token, s"$creator is not a valid dice"))
      case (suggestPattern(number, "", "", "", ""), true) =>
        RollTokenSuggestion(dice.map(faces => s"${number}d$faces"))
      case (suggestPattern(number, modifier, "", "", ""), true) =>
        RollTokenSuggestion(dice.map(faces => s"$number${modifier}d$faces"))
      case (suggestPattern(number, modifier, creator, _, ""), true) =>
        RollTokenSuggestion(dice.map(faces => s"$number$modifier${creator}d$faces"))
      case (_, _) => RollTokenEmpty
    }
}
