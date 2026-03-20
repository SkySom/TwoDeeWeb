package io.sommers.twodee.web.exception

case class InvalidTokenException(
    message: String
) extends Exception(message)
