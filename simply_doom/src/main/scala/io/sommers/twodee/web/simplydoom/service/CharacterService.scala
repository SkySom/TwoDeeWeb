package io.sommers.twodee.web.simplydoom.service

import cats.effect.IO
import doobie.implicits.{autoDerivedRead, toConnectionIOOps, toSqlInterpolator}
import doobie.{ConnectionIO, Transactor}
import io.sommers.twodee.web.simplydoom.model.CharacterRow

trait CharacterService {
  def createCharacter(name: String, sheet: String, ownerId: Long): IO[Long]

  def getCharacter(id: Long): IO[Option[CharacterRow]]
}

object CharacterService {
  def apply(transactor: Transactor[IO]): IO[CharacterService] = for {
    _ <- CharacterServiceImpl
      .createTables()
      .transact(transactor)
  } yield CharacterServiceImpl(transactor)
}

case class CharacterServiceImpl(
    transactor: Transactor[IO]
) extends CharacterService {
  override def createCharacter(name: String, sheet: String, ownerId: Long): IO[Long] =
    sql"INSERT INTO character(name, sheet, owner_id) VALUES($name, $sheet, $ownerId)".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)

  override def getCharacter(id: Long): IO[Option[CharacterRow]] =
    sql"SELECT id, name, sheet, owner_id from character where id = $id"
      .query[CharacterRow]
      .option
      .transact(transactor)
}

object CharacterServiceImpl {
  def createTables(): ConnectionIO[Int] = createCharacterTable

  private def createCharacterTable: ConnectionIO[Int] =
    sql"""
         | CREATE TABLE IF NOT EXISTS character(
         |  id INTEGER PRIMARY KEY,
         |  name VARCHAR NOT NULL,
         |  sheet VARCHAR NOT NULL,
         |  owner_id INTEGER NOT NULL,
         |  foreign key (owner_id) references user(id)
         | )
       """.stripMargin.update.run
}
