package io.sommers.twodee.web.simplydoom.roll.reader

import io.sommers.twodee.web.simplydoom.roll.token.RollToken

trait FormulaReadResult {
  def :+(tokenReadResult: TokenReadResult): FormulaReadResult
}

object FormulaReadNone extends FormulaReadResult {
  override def :+(tokenReadResult: TokenReadResult): FormulaReadResult = tokenReadResult match {
    case tokenReadSuccess: TokenReadSuccess => FormulaReadSuccess(tokenReadSuccess.rollToken)
    case tokenReadSuggest: TokenReadSuggest => FormulaReadSuggest(tokenReadSuggest.suggestions)
    case tokenReadFailure: TokenReadFailure => FormulaReadFailure(tokenReadFailure.error)
  }
}

case class FormulaReadSuccess(
    rollTokens: Seq[RollToken]
) extends FormulaReadResult {
  override def :+(tokenReadResult: TokenReadResult): FormulaReadResult = tokenReadResult match {
    case tokenReadSuccess: TokenReadSuccess =>
      FormulaReadSuccess(this.rollTokens :+ tokenReadSuccess.rollToken)
    case tokenReadSuggest: TokenReadSuggest =>
      val currentFormula = this.rollTokens.map(_.toFormula).mkString(" ")
      FormulaReadSuggest(
        tokenReadSuggest.suggestions.map(suggestedToken => s"$currentFormula $suggestedToken")
      )
    case tokenReadFailure: TokenReadFailure => FormulaReadFailure(tokenReadFailure.error)
  }
}

object FormulaReadSuccess {
  def apply(rollToken: RollToken): FormulaReadSuccess = FormulaReadSuccess(Seq(rollToken))
}

case class FormulaReadSuggest(
    suggestions: Seq[String]
) extends FormulaReadResult {
  override def :+(tokenReadResult: TokenReadResult): FormulaReadResult = FormulaReadFailure(
    "Cannot append new tokens after suggestions"
  )
}

case class FormulaReadFailure(
    errors: Seq[String]
) extends FormulaReadResult {
  override def :+(tokenReadResult: TokenReadResult): FormulaReadResult = tokenReadResult match {
    case TokenReadFailure(error) => FormulaReadFailure(errors :+ error)
    case _                       => this
  }
}

object FormulaReadFailure {
  def apply(error: String): FormulaReadFailure = FormulaReadFailure(Seq(error))
}
