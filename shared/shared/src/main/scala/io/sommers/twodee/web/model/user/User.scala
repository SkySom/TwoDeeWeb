package io.sommers.twodee.web.model.user

import io.circe.{Decoder, Encoder, HCursor, Json}

class User(
    val id: Long,
    val username: String,
    val image: Option[String],
    val authId: String,
    val roles: List[String]
) {}

object User {
  implicit val decoder: Decoder[User] = (c: HCursor) =>
    for {
      id <- c.get[Long]("id")
      username <- c.downField("username").as[String]
      image <- c.downField("image").as[Option[String]]
      authId <- c.get[String]("authId")
      roles <- c.downField("roles").as[List[String]]
    } yield new User(id, username, image, authId, roles)

  implicit val encoder: Encoder[User] = Encoder.instance(user => {
    Json.obj(
      ("id", Json.fromLong(user.id)),
      ("username", Json.fromString(user.username)),
      ("image", Json.fromStringOrNull(user.image)),
      ("authId", Json.fromString(user.authId)),
      ("roles", Json.fromValues(user.roles.map(Json.fromString)))
    )
  })

  def apply(id: Long, username: String, image: Option[String], authId: String) =
    new User(id: Long, username, image, authId, List())
}
