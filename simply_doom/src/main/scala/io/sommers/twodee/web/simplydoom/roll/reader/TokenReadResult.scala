package io.sommers.twodee.web.simplydoom.roll.reader

import io.sommers.twodee.web.simplydoom.roll.token.RollToken

type TokenReadResult = TokenReadSuccess | TokenReadSuggest | TokenReadFailure

case class TokenReadSuccess(
    rollToken: RollToken
)

case class TokenReadSuggest(
    suggestions: Seq[String]
)

case class TokenReadFailure(
    error: String
)
