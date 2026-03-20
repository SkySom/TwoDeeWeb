package io.sommers.twodee.web.service

import cats.effect.IO
import doobie.Transactor
import doobie.free.connection.ConnectionIO
import doobie.implicits.*

trait UserService {
  def insertUser(
      username: String,
      image: Option[String],
      authId: String
  ): IO[Long]

  def getUserById(id: Long): IO[Option[(Long, String, Option[String], String)]]

  def getUserByAuthId(
      authId: String
  ): IO[Option[(Long, String, Option[String], String)]]
}

object UserService {
  def create(transactor: Transactor[IO]): IO[UserService] = for {
    _ <- UserServiceImpl.createTables()
      .transact(transactor)
  } yield UserServiceImpl(transactor)
}

case class UserServiceImpl(
    transactor: Transactor[IO]
) extends UserService {

  override def insertUser(
      username: String,
      image: Option[String],
      authId: String
  ): IO[Long] =
    insertUserSQL(username, image, authId)
      .transact(transactor)

  override def getUserById(id: Long): IO[Option[(Long, String, Option[String], String)]] =
    userByIdSQL(id)
      .transact(transactor)

  override def getUserByAuthId(authId: String): IO[Option[(Long, String, Option[String], String)]] =
    userByAuthIdSQL(authId)
      .transact(transactor)

  private def insertUserSQL(
      name: String,
      image: Option[String],
      authId: String
  ): ConnectionIO[Long] = {
    sql"insert into user(name, image, auth_id) values($name, $image, $authId)".update
      .withUniqueGeneratedKeys("id")
  }

  private def userByIdSQL(
      id: Long
  ) = {
    sql"select id, name, image, auth_id from user where id = $id"
      .query[(Long, String, Option[String], String)]
      .option
  }

  private def userByAuthIdSQL(
    authId: String
  ) = {
    sql"select id, name, image, auth_id from user where auth_id = $authId"
      .query[(Long, String, Option[String], String)]
      .option
  }
}

object UserServiceImpl {
  def createTables(): ConnectionIO[Int] = createUserTable

  private def createUserTable: ConnectionIO[Int] =
    sql"""
         | CREATE TABLE IF NOT EXISTS user(
         |  id INTEGER PRIMARY KEY,
         |  name VARCHAR NOT NULL,
         |  image VARCHAR NULL,
         |  auth_id VARCHAR NOT NULL UNIQUE
         | )
       """.stripMargin.update.run
}
