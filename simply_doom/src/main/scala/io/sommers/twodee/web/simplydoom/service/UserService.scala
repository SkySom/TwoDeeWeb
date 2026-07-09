package io.sommers.twodee.web.simplydoom.service

import cats.effect.IO
import doobie.implicits.{
  toConnectionIOOps,
  toDoobieStreamOps,
  toSqlInterpolator
}
import doobie.util.fragment.Fragment
import doobie.{ConnectionIO, Transactor}
import io.sommers.twodee.web.simplydoom.model.Permission

import scala.collection.mutable.ArrayBuffer

trait UserService {
  def getById(id: Long): IO[Option[
    (
        Long,
        String,
        Permission,
        Permission,
        Permission,
        Permission,
        String,
        Boolean,
        Option[Long]
    )
  ]]

  def createUser(
    name: String,
    characterPermission: Permission,
    doomPermission: Permission,
    userPermission: Permission,
    tokenPermission: Permission,
    notes: String,
    createdBy: Option[Long]
  ): IO[Long]

  def deleteUser(id: Long): IO[Int]

  def searchUsers(filters: Map[String, String]): IO[List[
    (
        Long,
        String,
        Permission,
        Permission,
        Permission,
        Permission,
        String,
        Boolean,
        Option[Long]
    )
  ]]
}

object UserService {
  def apply(transactor: Transactor[IO]): IO[UserService] = for {
    _ <- UserServiceImpl
      .createTables()
      .transact(transactor)
  } yield UserServiceImpl(transactor)
}

case class UserServiceImpl(
    transactor: Transactor[IO]
) extends UserService {

  override def getById(id: Long): IO[Option[
    (
        Long,
        String,
        Permission,
        Permission,
        Permission,
        Permission,
        String,
        Boolean,
        Option[Long]
    )
  ]] =
    sql"select id, name, character_permissions, doom_permissions, user_permissions, token_permissions, notes, active, created_by from user where id = $id"
      .query[
        (
            Long,
            String,
            Permission,
            Permission,
            Permission,
            Permission,
            String,
            Boolean,
            Option[Long]
        )
      ]
      .option
      .transact(transactor)

  override def createUser(
    name: String,
    characterPermission: Permission,
    doomPermission: Permission,
    userPermission: Permission,
    tokenPermission: Permission,
    notes: String,
    createdBy: Option[Long]
  ): IO[Long] =
    sql""" INSERT INTO user(name, character_permissions, doom_permissions, user_permissions, token_permissions, notes, created_by)
         | VALUES ($name, $characterPermission, $doomPermission, $userPermission, $tokenPermission, $notes, $createdBy)
          """.stripMargin.update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)

  override def deleteUser(id: Long): IO[Int] =
    sql"UPDATE user set active = 0, update_at = CURRENT_TIMESTAMP where id = $id".update.run
      .transact(transactor)

  override def searchUsers(filters: Map[String, String]): IO[List[
    (
        Long,
        String,
        Permission,
        Permission,
        Permission,
        Permission,
        String,
        Boolean,
        Option[Long]
    )
  ]] = {
    var query =
      fr"select id, name, character_permissions, doom_permissions, user_permissions, token_permissions, notes, active, created_by from user"

    val queryFilters = new ArrayBuffer[Fragment]()

    filters
      .get("id")
      .map(_.toLong)
      .map(id => fr"id = $id")
      .foreach(queryFilters.addOne)
    filters
      .get("active")
      .map(_.toBoolean)
      .map(active => fr"active = $active")
      .foreach(queryFilters.addOne)

    if (queryFilters.nonEmpty) {
      val reducedFilters = queryFilters.reduce((a, b) => fr"$a AND $b")
      query = fr"$query WHERE $reducedFilters"
    }

    query
      .query[
        (
            Long,
            String,
            Permission,
            Permission,
            Permission,
            Permission,
            String,
            Boolean,
            Option[Long]
        )
      ]
      .stream
      .transact(transactor)
      .compile
      .toList

  }
}

object UserServiceImpl {
  def createTables(): ConnectionIO[Int] = createUserTable

  private def createUserTable: ConnectionIO[Int] =
    sql"""
         | CREATE TABLE IF NOT EXISTS user(
         |  id INTEGER PRIMARY KEY,
         |  name VARCHAR NOT NULL,
         |  character_permissions VARCHAR NOT NULL DEFAULT '',
         |  doom_permissions VARCHAR NOT NULL DEFAULT '',
         |  user_permissions VARCHAR NOT NULL DEFAULT '',
         |  token_permissions VARCHAR NOT NULL DEFAULT '',
         |  notes VARCHAR NOT NULL,
         |  active INTEGER NOT NULL DEFAULT 1,
         |  created_by INTEGER,
         |  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |  FOREIGN KEY (created_by) REFERENCES user(id)
         | )
       """.stripMargin.update.run
}
