package io.sommers.twodee.web.simplydoom.roll

import io.sommers.twodee.web.simplydoom.roll.token.{RollTokenError, FlatModifierRollToken, FlatModifierRollTokenProvider, RollTokenProvided, RollTokenSuggestion}
import weaver.SimpleIOSuite

object FlatValueRollTokenSuite extends SimpleIOSuite {
  pureTest("String +1 should return FlatModifierRollToken") {
    matches(FlatModifierRollTokenProvider.provide("+1")) { case token: FlatModifierRollToken =>
      expect.same(FlatModifierRollToken(1), token)
    }
  }

  pureTest("String -2 should return FlatModifierRollToken") {
    matches(FlatModifierRollTokenProvider.provide("-2")) { case token: FlatModifierRollToken =>
      expect.same(FlatModifierRollToken(-2), token)
    }
  }

  pureTest("String +-1 should return ErroredRollToken") {
    matches(FlatModifierRollTokenProvider.provide("+-1")) { case error: RollTokenError =>
      expect.same(RollTokenError("+-1", "+-1 does not match +/- followed by numbers"), error)
    }
  }

  pureTest("String +d should return ErroredRollToken") {
    matches(FlatModifierRollTokenProvider.provide("+d")) { case error: RollTokenError =>
      expect.same(RollTokenError("+d", "+d does not match +/- followed by numbers"), error)
    }
  }

  pureTest("Suggested string + should return RollTokenSuggestion") {
    matches(FlatModifierRollTokenProvider.provide("+", true)) { case suggestion: RollTokenSuggestion =>
      expect.same(RollTokenSuggestion((1 to 6).map(number => s"+$number")), suggestion)
    }
  }

  pureTest("Suggested string +1 should return FlatModifierRollToken") {
    matches(FlatModifierRollTokenProvider.provide("+1", true)) { case token: FlatModifierRollToken =>
      expect.same(FlatModifierRollToken(1), token)
    }
  }
}
