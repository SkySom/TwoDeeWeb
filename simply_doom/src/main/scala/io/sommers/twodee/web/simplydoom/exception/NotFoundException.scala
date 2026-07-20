package io.sommers.twodee.web.simplydoom.exception

case class NotFoundException(
    message: String
) extends Exception(message)
