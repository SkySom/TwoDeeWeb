package io.sommers.twodee.web.model.response

import io.sommers.twodee.web.model.user.{LoggedInUser, User}

case class LoginResponse(
    user: LoggedInUser
) {

}
