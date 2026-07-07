package io.sommers.twodee.web.simplydoom.service

import cats.effect.IO
import doobie.implicits.{toDoobieStreamOps, toSqlInterpolator}
import doobie.syntax.all.toConnectionIOOps
import doobie.util.fragment.Fragment
import doobie.{ConnectionIO, Transactor}

import scala.collection.mutable.ArrayBuffer

trait DoomPoolService {
  def createDoomPool(name: String, starting: Int): IO[Long]

  def getDoomPool(id: Long): IO[Option[(Long, String, Int)]]

  def changeDoomAmount(id: Long, change: Int): IO[Int]

  def list(filters: Map[String, String]): IO[List[(Long, String, Int)]]
}

object DoomPoolService {
  def apply(transactor: Transactor[IO]): IO[DoomPoolService] = for {
    _ <- DoomPoolServiceImpl
      .createTables()
      .transact(transactor)
  } yield DoomPoolServiceImpl(transactor)
}

case class DoomPoolServiceImpl(
    transactor: Transactor[IO]
) extends DoomPoolService {

  override def createDoomPool(name: String, starting: Int): IO[Long] =
    sql"INSERT INTO doom_pool(name, doom) VALUES($name, $starting)".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)

  override def getDoomPool(id: Long): IO[Option[(Long, String, Int)]] =
    sql"SELECT id, name, doom from doom_pool where id = $id"
      .query[(Long, String, Int)]
      .option
      .transact(transactor)

  override def changeDoomAmount(id: Long, change: Int): IO[Int] =
    sql"UPDATE doom_pool set doom = doom + $change where id = $id".update.run
      .transact(transactor)

  override def list(
      filters: Map[String, String]
  ): IO[List[(Long, String, Int)]] = {
    var query =
      fr"select id, name, doom from doom_pool"

    val queryFilters = new ArrayBuffer[Fragment]()

    filters
      .get("id")
      .map(_.toLong)
      .map(id => fr"id = $id")
      .foreach(queryFilters.addOne)
    filters
      .get("name")
      .map(name => fr"name = $name")
      .foreach(queryFilters.addOne)

    if (queryFilters.nonEmpty) {
      val reducedFilters = queryFilters.reduce((a, b) => fr"$a AND $b")
      query = fr"$query WHERE $reducedFilters"
    }

    query
      .query[(Long, String, Int)]
      .stream
      .transact(transactor)
      .compile
      .toList

  }
}

object DoomPoolServiceImpl {
  def createTables(): ConnectionIO[Int] = createDoomPoolTable

  private def createDoomPoolTable: ConnectionIO[Int] =
    sql"""
         | CREATE TABLE IF NOT EXISTS doom_pool(
         |  id INTEGER PRIMARY KEY,
         |  name VARCHAR NOT NULL,
         |  doom INTEGER NOT NULL DEFAULT 0
         | )
       """.stripMargin.update.run
}
