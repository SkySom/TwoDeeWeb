package io.sommers.twodee.web.simplydoom.roll.token

import io.sommers.twodee.web.simplydoom.roll.dice.{AssuredDiceRollValueModifier, RegularDiceRollValueCreator}
import weaver.SimpleIOSuite

object DiceRollTokenSuite extends SimpleIOSuite {
  private val dice = Seq(4, 6, 8, 10, 12)
  private val dice2 = Seq(10, 12)

  pureTest("String 1d6 should return a single dice") {
    matches(DiceRollTokenProvider.provide("1d6")) {
      case diceRollToken: DiceRollToken =>
        expect.same(DiceRollToken(RegularDiceRollValueCreator, 1, 6), diceRollToken)
    }
  }
  pureTest("String 1ad6 should return a single dice with modifier") {
    matches(DiceRollTokenProvider.provide("1ad6")) {
      case diceRollToken: DiceRollToken =>
        expect.same(DiceRollToken(RegularDiceRollValueCreator, 1, 6, AssuredDiceRollValueModifier), diceRollToken)
    }
  }
  pureTest("String 1d should suggest 1d4, 1d6, 1d8, 1d10, 1d12") {
    matches(DiceRollTokenProvider.provide("1d", true)) {
      case rollTokenSuggestion: RollTokenSuggestion =>
        expect.same(dice.map(faces => s"1d$faces"), rollTokenSuggestion.suggestions)
    }
  }
  pureTest("String 1ad should suggest 1ad4, 1ad6, 1ad8, 1ad10, 1ad12") {
    matches(DiceRollTokenProvider.provide("1ad", true)) {
      case rollTokenSuggestion: RollTokenSuggestion =>
        expect.same(dice.map(faces => s"1ad$faces"), rollTokenSuggestion.suggestions)
    }
  }
}
