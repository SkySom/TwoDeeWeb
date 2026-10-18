package io.sommers.twodee.web.simplydoom.roll.token

import cats.effect.IO

trait RollTokenProvider[T <: RollToken] {
  def provide(token: String, suggest: Boolean = false): RollTokenProvided[T]
}

type RollTokenProvided[T <: RollToken] = T | RollTokenSuggestion | NoRollToken | ErroredRollToken

case class RollTokenSuggestion(
    suggestions: Seq[String]
)

trait NoRollToken {}

object NoRollToken extends NoRollToken

case class ErroredRollToken(token: String, message: String)
