package io.sommers.twodee.web.exception

case class InvalidTokenException(
    message: String
) extends Exception(message)

object InvalidTokenException {
  def apply(): InvalidTokenException = InvalidTokenException("Invalid access token")
}
