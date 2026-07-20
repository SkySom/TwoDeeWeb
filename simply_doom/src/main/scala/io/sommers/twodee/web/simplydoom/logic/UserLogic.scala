package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import io.chrisdavenport.mules.{Cache, MemoryCache, TimeSpec}
import io.sommers.twodee.web.simplydoom.exception.NotFoundException
import io.sommers.twodee.web.simplydoom.model.{Permission, User}
import io.sommers.twodee.web.simplydoom.service.UserService

import scala.concurrent.duration.DurationInt

trait UserLogic {
  def createUser(
      name: String,
      characterPermissions: Permission,
      doomPermission: Permission,
      userPermission: Permission,
      tokenPermission: Permission,
      externalId: Option[String],
      notes: String,
      createdBy: Option[Long]
  ): IO[User]

  def getUser(id: Long): IO[User]

  def deleteUser(id: Long): IO[Unit]

  def searchUsers(filters: Map[String, String]): IO[List[User]]
  
  def deleteUserFromCache(id: Long): IO[Unit]
  
  def addCharacterPermission(userId: Long, characterId: Long): IO[Unit]
}

object UserLogic {
  def apply(userService: UserService): IO[UserLogic] = for {
    userCache <- MemoryCache.ofConcurrentHashMap[IO, Long, User](
      TimeSpec.fromDuration(8.hour)
    )
  } yield UserLogicImpl(
    userService,
    userCache
  )
}

case class UserLogicImpl(
    userService: UserService,
    userCache: Cache[IO, Long, User]
) extends UserLogic {

  override def createUser(
      name: String,
      characterPermission: Permission,
      doomPermission: Permission,
      userPermission: Permission,
      tokenPermission: Permission,
      externalId: Option[String],
      notes: String,
      createdBy: Option[Long]
  ): IO[User] = userService
    .createUser(
      name,
      characterPermission,
      doomPermission,
      userPermission,
      tokenPermission,
      externalId,
      notes,
      createdBy
    )
    .flatMap(this.getUser)

  override def getUser(id: Long): IO[User] = for {
    cachedUser <- userCache.lookup(id)
    returnedUser <- cachedUser.fold(pullUserFromDB(id))(IO.pure)
  } yield returnedUser

  override def deleteUser(id: Long): IO[Unit] = userService
    .deleteUser(id)
    .map(updated => IO.raiseWhen(updated == 0)(NotFoundException(s"No User with $id")))

  override def searchUsers(filters: Map[String, String]): IO[List[User]] =
    userService
      .searchUsers(filters)
      .map(users => users.map(User(_)))

  private def pullUserFromDB(id: Long): IO[User] = for {
    user <- userService
      .getById(id)
      .flatMap(
        _.fold[IO[User]](IO.raiseError(NotFoundException(s"No User with $id")))(user =>
          IO.pure(User(user))
        )
      )
    _ <- userCache.insert(id, user)
  } yield user

  override def deleteUserFromCache(id: Long): IO[Unit] = for {
    user <- getUser(id)
    _ <- userCache.delete(user.id)
  } yield ()

  override def addCharacterPermission(userId: Long, characterId: Long): IO[Unit] = for {
    user <- this.getUser(userId)
    _ <- userService.updateUserPermissions(
      user.id,
      user.characterPermissions.withValue(characterId.toString),
      user.doomPermission,
      user.userPermission,
      user.tokenPermission
    )
    _ <- deleteUserFromCache(userId)
  } yield ()
}
