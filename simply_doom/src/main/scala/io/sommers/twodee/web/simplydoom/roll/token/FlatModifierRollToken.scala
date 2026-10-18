package io.sommers.twodee.web.simplydoom.roll.token

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.value.{FlatModifierRollValue, RollValue}

import scala.util.matching.Regex

case class FlatModifierRollToken(
    amount: Int
) extends RollToken {

  override def provide(): IO[Seq[RollValue]] = IO.pure(Seq(FlatModifierRollValue(amount)))
}

object FlatModifierRollTokenProvider extends RollTokenProvider[FlatModifierRollToken] {
  private val fullPattern: Regex = """^([+-])(\d{1,4})$""".r
  private val symbolPattern: Regex = """^([+-])(.*)$""".r

  override def provide(token: String, suggest: Boolean): RollTokenProvided[FlatModifierRollToken] =
    (token, suggest) match {
      case (fullPattern(symbol, digits), _) =>
        if (symbol.equals("+")) {
          FlatModifierRollToken(digits.toInt)
        } else {
          FlatModifierRollToken(-digits.toInt)
        }
      case (symbolPattern(symbol, ""), true) =>
        RollTokenSuggestion((1 to 6).map(number => s"$symbol$number"))
      case (symbolPattern(symbol, rest), _) =>
        ErroredRollToken(token, s"$token does not match +/- followed by numbers")
      case (_, _) => NoRollToken
    }
}
