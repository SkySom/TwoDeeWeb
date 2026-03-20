package io.sommers.twodee.web.logic

import cats.effect.IO
import io.sommers.twodee.web.exception.NotFoundException
import io.sommers.twodee.web.model.user.User
import io.sommers.twodee.web.service.UserService

trait UserLogic {
  def insertUser(user: UserCreate): IO[User] 
  
  def getOptionalUser(id: Long): IO[Option[User]]
  
  def getUser(id: Long): IO[User]
  
  def getUserByAuthId(authId: String): IO[Option[User]]
}

object UserLogic {
  def apply(userService: UserService): UserLogic = UserLogicImpl(userService)
}

case class UserCreate(
    username: String,
    image: Option[String],
    authId: String
)

case class UserLogicImpl(
    userService: UserService
) extends UserLogic {

  override def insertUser(user: UserCreate): IO[User] = 
    userService.insertUser(user.username, user.image, user.authId)
      .map(id => User(id, user.username, user.image, user.authId))
  
  override def getOptionalUser(id: Long): IO[Option[User]] = userService.getUserById(id)
    .map(_.map(User.apply))

  override def getUser(id: Long): IO[User] = getOptionalUser(id)
    .flatMap(IO.fromOption(_)(NotFoundException(s"No User with id $id")))

  override def getUserByAuthId(authId: String): IO[Option[User]] = userService.getUserByAuthId(authId)
    .map(_.map(User.apply))
}
