package io.sommers.twodee.web.simplydoom.model

case class User(
    id: Long,
    name: String,
    characterPermissions: Permission,
    doomPermission: Permission,
    userPermission: Permission,
    tokenPermission: Permission,
    externalId: Option[String],
    notes: String,
    active: Boolean,
    createdBy: Option[Long]
)

case class UserRequest(
    name: String,
    characterPermissions: Option[Permission],
    doomPermission: Option[Permission],
    userPermission: Option[Permission],
    tokenPermission: Option[Permission],
    externalId: Option[String],
    notes: String
)

object User {
  def apply(
      tuple: (
          Long,
          String,
          Permission,
          Permission,
          Permission,
          Permission,
          Option[String],
          String,
          Boolean,
          Option[Long]
      )
  ): User =
    User(
      tuple._1,
      tuple._2,
      tuple._3,
      tuple._4,
      tuple._5,
      tuple._6,
      tuple._7,
      tuple._8,
      tuple._9,
      tuple._10
    )
}
