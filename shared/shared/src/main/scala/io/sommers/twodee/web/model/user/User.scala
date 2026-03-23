package io.sommers.twodee.web.model.user

import io.circe.{Decoder, Encoder, HCursor, Json}

trait User {
  val id: Long
  val username: String
  val image: Option[String]
  val authId: String
}

object User {
  implicit val decoder: Decoder[User] = (c: HCursor) =>
    for {
      id <- c.get[Long]("id")
      username <- c.downField("username").as[String]
      image <- c.downField("image").as[Option[String]]
      authId <- c.get[String]("authId")
    } yield User(id, username, image, authId)

  implicit val encoder: Encoder[User] = Encoder.instance(user => {
    Json.obj(
      ("id", Json.fromLong(user.id)),
      ("username", Json.fromString(user.username)),
      ("image", Json.fromStringOrNull(user.image)),
      ("authId", Json.fromString(user.authId))
    )
  })

  def apply(id: Long, username: String, image: Option[String], authId: String) =
    UserImpl(id: Long, username, image, authId)
}

case class UserImpl(
  override val id: Long,
  override val username: String,
  override val image: Option[String],
  override val authId: String
) extends User
