package io.sommers.twodee.web.simplydoom.roll

import io.sommers.twodee.web.simplydoom.roll.token.{
  FlatModifierRollToken,
  FlatModifierRollTokenProvider
}
import weaver.SimpleIOSuite

object FlatValueRollTokenSuite extends SimpleIOSuite {
  pureTest("+1 should return valid token") {
    whenSuccess(FlatModifierRollTokenProvider.parser.parse("+1")) { case (remaining, token) =>
      expect.eql("", remaining)
      expect.same(FlatModifierRollToken(1), token)
    }
  }

  pureTest("-2 should return valid token") {
    whenSuccess(FlatModifierRollTokenProvider.parser.parse("-2")) { case (remaining, token) =>
      expect.eql("", remaining)
      expect.same(FlatModifierRollToken(-2), token)
    }
  }

  pureTest("+-1 should return parse error") {
    matches(FlatModifierRollTokenProvider.parser.parse("+-1")) { case Left(parsingError) =>
      expect.eql(parsingError.failedAtOffset, 1)
    }
  }
}

object FlatValueRollValueSuite extends SimpleIOSuite {}
