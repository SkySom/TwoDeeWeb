package io.sommers.twodee.web.simplydoom.roll

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.RollData
import io.sommers.twodee.web.simplydoom.roll.exception.FormulaReadException
import io.sommers.twodee.web.simplydoom.roll.reader.{
  FormulaReadFailure,
  FormulaReadSuccess,
  FormulaReadSuggest,
  FormulaReader
}
import io.sommers.twodee.web.simplydoom.roll.result.RollResult

class FormulaRoller {
  
}

object FormulaRoller {
  def apply(formula: String, rollData: RollData, rollOptions: RollOptions): IO[RollResult] = for {
    rollTokens <- FormulaReader(formula, false, rollData).read() match {
      case FormulaReadSuccess(rollTokens) => IO.pure(rollTokens)
      case FormulaReadFailure(errors)     => IO.raiseError(FormulaReadException(formula, errors))
      case FormulaReadSuggest(_) =>
        IO.raiseError(FormulaReadException(formula, Seq("Suggestions are invalid for roll")))
    }
    diceRoller <- RandomDiceRoller()
    rollValues <- IO
      .traverse(rollTokens)(_.roll(diceRoller))
      .map(_.flatten)
  } yield RollResult(rollValues, rollOptions)
}
