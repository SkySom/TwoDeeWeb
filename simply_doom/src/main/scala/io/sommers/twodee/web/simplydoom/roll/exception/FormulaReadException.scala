package io.sommers.twodee.web.simplydoom.roll.exception

case class FormulaReadException (formula: String, errors: Seq[String]) extends Throwable(
  s"Failed to parse formula $formula due to errors: [${errors.mkString(",")}]}"
)
