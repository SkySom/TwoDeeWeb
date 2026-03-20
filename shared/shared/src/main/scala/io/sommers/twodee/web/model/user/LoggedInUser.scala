package io.sommers.twodee.web.model.user

case class LoggedInUser(
    token: String,
    override val id: Long,
    override val username: String,
    override val image: Option[String],
    override val authId: String,
    override val roles: List[String]
) extends User(id, username, image, authId, roles) {}

object LoggedInUser {
  def apply(token: String, user: User): LoggedInUser = {
    new LoggedInUser(
      token,
      user.id,
      user.username,
      user.image,
      user.authId,
      user.roles
    )
  }
}
