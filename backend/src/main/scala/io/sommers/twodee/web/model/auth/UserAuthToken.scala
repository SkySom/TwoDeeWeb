package io.sommers.twodee.web.model.auth

import io.sommers.twodee.web.model.auth.AuthTokenKind.USER
import io.sommers.twodee.web.model.user.User

case class UserAuthToken(
    override val id: Long,
    user: User
) extends AuthToken {
  override val kind: AuthTokenKind = USER
}

object UserAuthToken {
  def fromToken(token: AuthToken): Option[UserAuthToken] = token match {
    case userAuthToken: UserAuthToken => Some(userAuthToken)
    case _ => None
  }
}
