package io.sommers.twodee.web.simplydoom.roll.reader

import io.sommers.twodee.web.simplydoom.roll.RollData
import io.sommers.twodee.web.simplydoom.roll.token.{RollToken, RollTokenEmpty, RollTokenError, RollTokenProvider, RollTokenSuggestion}

import scala.annotation.tailrec

case class FormulaReader(
    formula: String,
    suggest: Boolean,
    rollData: RollData
) {
  def read(): FormulaReadResult = {
    val formulaTokens = formula
      .split(" ")
      .filter(_.isEmpty)
      .toSeq

    formulaTokens match {
      case _ if rollData.depth > 10 => FormulaReadFailure("Formula includes a cyclic element")
      case Nil                      => FormulaReadFailure("No formula found")
      case tokens                   => readTokens(FormulaReadNone, tokens)
    }
  }

  @tailrec
  private def readTokens(
      currentFormulaResult: FormulaReadResult,
      tokens: Seq[String]
  ): FormulaReadResult = tokens match {
    case Nil          => currentFormulaResult
    case head :: Nil  => currentFormulaResult :+ readToken(head, this.suggest)
    case head :: tail => readTokens(currentFormulaResult :+ readToken(head, false), tail)
  }

  private def readToken(currentToken: String, suggestToken: Boolean): TokenReadResult = {
    RollTokenProvider.provide(currentToken, suggestToken) match {
      case rollToken: RollToken => TokenReadSuccess(rollToken)
      case RollTokenSuggestion(suggestions) => TokenReadSuggest(suggestions)
      case RollTokenError(_, error) => TokenReadFailure(error)
      case _: RollTokenEmpty => TokenReadFailure(s"$currentToken is not a valid roll token")
    }
  }
}
