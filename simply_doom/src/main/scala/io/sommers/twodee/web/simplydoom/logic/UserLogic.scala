package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.exception.NotFoundException
import io.sommers.twodee.web.simplydoom.model.{Permission, User}
import io.sommers.twodee.web.simplydoom.service.UserService

trait UserLogic {
  def createUser(
      name: String,
      characterPermissions: Permission,
      doomPermission: Permission,
      userPermission: Permission,
      tokenPermission: Permission,
      notes: String,
      createdBy: Option[Long]
  ): IO[User]

  def getUser(id: Long): IO[User]

  def deleteUser(id: Long): IO[Unit]

  def searchUsers(filters: Map[String, String]): IO[List[User]]
}

object UserLogic {
  def apply(userService: UserService): UserLogic = UserLogicImpl(
    userService
  )
}

case class UserLogicImpl(
    userService: UserService
) extends UserLogic {

  override def createUser(
    name: String,
    characterPermission: Permission,
    doomPermission: Permission,
    userPermission: Permission,
    tokenPermission: Permission,
    notes: String,
    createdBy: Option[Long]
  ): IO[User] = userService
    .createUser(
      name,
      characterPermission,
      doomPermission,
      userPermission,
      tokenPermission,
      notes,
      createdBy
    )
    .flatMap(this.getUser)

  override def getUser(id: Long): IO[User] = userService
    .getById(id)
    .flatMap(
      _.fold[IO[User]](IO.raiseError(NotFoundException(s"No User with $id")))(
        user => IO.pure(User(user))
      )
    )

  override def deleteUser(id: Long): IO[Unit] = userService
    .deleteUser(id)
    .map(updated =>
      IO.raiseWhen(updated == 0)(NotFoundException(s"No User with $id"))
    )

  override def searchUsers(filters: Map[String, String]): IO[List[User]] =
    userService
      .searchUsers(filters)
      .map(users => users.map(User(_)))
}
