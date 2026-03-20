package io.sommers.twodee.web.model.user

case class LoggedInUser(
    token: String,
    override val username: String,
    override val image: Option[String],
    override val roles: List[String]
) extends User(username, image, roles) {}

object LoggedInUser {
  def apply(token: String, user: User): LoggedInUser = {
    new LoggedInUser(
      token,
      user.username,
      user.image,
      user.roles
    )
  }
}
