package io.sommers.twodee.web.simplydoom.roll

import io.sommers.twodee.web.simplydoom.roll.reader.{FormulaReadFailure, FormulaReadSuccess, FormulaReadSuggest, FormulaReader}
import io.sommers.twodee.web.simplydoom.roll.token.FlatModifierRollToken
import weaver.SimpleIOSuite

object FormulaReaderTestSuite extends SimpleIOSuite {
  pureTest("+3 should return Success") {
    expect.same(FormulaReadSuccess(Seq(FlatModifierRollToken(3))), FormulaReader("+3").read())
  }
  pureTest("1d6 3d8 + d8 should return Error") {
    expect.same(FormulaReadFailure(Seq("+ does not match +/- followed by numbers")), FormulaReader("1d6 3d8 + d8").read())
  }

  pureTest("6d6 +3 3a should return Suggestion") {
    expect.same(FormulaReadSuggest(Seq("")), FormulaReader("1d6 3d8 + d8").read())
  }
}
