package io.sommers.twodee.web.simplydoom.roll.token

import cats.parse.{Numbers, Parser}
import io.sommers.twodee.web.simplydoom.roll.value.{FlatModifierRollValue, RollValue}

case class FlatModifierRollToken(
    amount: Int
) extends RollToken {

  override def provide(): Seq[RollValue] = Seq(FlatModifierRollValue(amount))
}

object FlatModifierRollTokenProvider extends RollTokenProvider[FlatModifierRollToken] {

  override def parser: Parser[FlatModifierRollToken] = (Parser.charIn("+-") ~ Numbers.digits).map {
    case ('+', str) => FlatModifierRollToken(str.toInt)
    case ('-', str) => FlatModifierRollToken(-str.toInt)
  }
}
