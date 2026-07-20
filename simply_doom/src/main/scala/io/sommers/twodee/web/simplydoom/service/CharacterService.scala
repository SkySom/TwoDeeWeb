package io.sommers.twodee.web.simplydoom.service

import cats.effect.IO
import doobie.implicits.{autoDerivedRead, toConnectionIOOps, toDoobieStreamOps, toSqlInterpolator}
import doobie.util.fragment.Fragment
import doobie.{ConnectionIO, Transactor}
import io.sommers.twodee.web.simplydoom.model.CharacterRow

import scala.collection.mutable.ArrayBuffer

trait CharacterService {
  def createCharacter(name: String, sheet: String, ownerId: Long): IO[Long]

  def getCharacter(id: Long): IO[Option[CharacterRow]]
  
  def searchCharacters(filters: Map[String, String]): IO[List[CharacterRow]]
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

  override def searchCharacters(filters: Map[String, String]): IO[List[CharacterRow]] = {
    var query = fr"SELECT id, name, sheet, owner_id from character"

    val queryFilters = new ArrayBuffer[Fragment]()
    filters
      .get("id")
      .map(_.toLong)
      .map(id => fr"id = $id")
      .foreach(queryFilters.addOne)
    filters
      .get("name")
      .map(_.toBoolean)
      .map(name => fr"name = $name")
      .foreach(queryFilters.addOne)
    filters
      .get("sheet")
      .map(sheet => fr"sheet = $sheet")
      .foreach(queryFilters.addOne)
    filters
      .get("owner_id")
      .map(ownerId => fr"owner_id = $ownerId")
      .foreach(queryFilters.addOne)

    if (queryFilters.nonEmpty) {
      val reducedFilters = queryFilters.reduce((a, b) => fr"$a AND $b")
      query = fr"$query WHERE $reducedFilters"
    }
    
    query.query[CharacterRow]
      .stream
      .transact(transactor)
      .compile
      .toList
  }
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
