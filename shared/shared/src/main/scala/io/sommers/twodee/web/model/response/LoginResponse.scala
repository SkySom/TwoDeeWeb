package io.sommers.twodee.web.model.response

import io.sommers.twodee.web.model.user.User

case class LoginResponse(
    token: String,
    user: User
) {

}
