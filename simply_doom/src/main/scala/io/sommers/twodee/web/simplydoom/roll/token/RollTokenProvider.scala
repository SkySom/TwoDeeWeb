package io.sommers.twodee.web.simplydoom.roll.token

trait RollTokenProvider[T <: RollToken] {
  def provide(token: String, suggest: Boolean = false): RollTokenProvided[T]
}

object RollTokenProvider {
  val all: Seq[RollTokenProvider[_]] = Seq(
    FlatModifierRollTokenProvider,
    DiceRollTokenProvider
  )

  def provide(token: String, suggest: Boolean = true): RollTokenProvided[RollToken] = {
    all
      .map(_.provide(token, suggest))
      .find(_ != RollTokenEmpty)
      .getOrElse(RollTokenEmpty)
  }
}

type RollTokenProvided[T <: RollToken] = T | RollTokenSuggestion | RollTokenEmpty | RollTokenError

case class RollTokenSuggestion(
    suggestions: Seq[String]
)

trait RollTokenEmpty {}

object RollTokenEmpty extends RollTokenEmpty

case class RollTokenError(token: String, message: String)
