package io.sommers.twodee.web.simplydoom.exception

case class InvalidTokenException(
    message: String = "Invalid access token"
) extends Exception(message)
