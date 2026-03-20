package io.sommers.twodee.web.model.user

import io.circe.{Decoder, HCursor}

class User(
  val username: String,
  val image: Option[String],
  val roles: List[String]
) {}

object User {
  implicit val decoder: Decoder[User] = (c: HCursor) => for {
    username <- c.downField("username").as[String]
    image <- c.downField("image").as[Option[String]]
    roles <- c.downField("roles").as[List[String]]
  } yield User(username, image, roles)

  def apply(username: String, image: Option[String], roles: List[String]) =
    new User(username, image, roles)
}
