package io.sommers.twodee.web.simplydoom.exception

case class InvalidFieldException(
    message: String
) extends Exception(message)
