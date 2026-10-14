package io.sommers.twodee.web.simplydoom.roll.token

import cats.parse.Parser

trait RollTokenProvider[T <: RollToken] {
    def parser: Parser[T]
}
