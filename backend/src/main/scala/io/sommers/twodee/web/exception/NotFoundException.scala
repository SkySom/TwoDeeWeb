package io.sommers.twodee.web.exception

case class NotFoundException(
    message: String
) extends Exception(message)
