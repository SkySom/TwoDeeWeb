package io.sommers.twodee.web.service

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import doobie.util.transactor.Transactor

trait GameService {
  def createGame(name: String, createdBy: Long): IO[Long]

  def getGameById(id: Long): IO[Option[(Long, String, Long)]]
}

object GameService {
  def apply(transactor: Transactor[IO]): IO[GameService] = for {
    _ <- GameServiceImpl.createTables()
      .transact(transactor)
  } yield GameServiceImpl(transactor)
}

case class GameServiceImpl(
    transactor: Transactor[IO]
) extends GameService {

  override def createGame(name: String, createdBy: Long): IO[Long] =
    sql"INSERT INTO game(name, created_by) VALUES($name, $createdBy)"
      .update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)

  override def getGameById(id: Long): IO[Option[(Long, String, Long)]] =
    sql"SELECT id, name, created_by FROM game WHERE id = $id"
      .query[(Long, String, Long)]
      .option
      .transact(transactor)
}

object GameServiceImpl {
  def createTables(): ConnectionIO[Int] = createGameTable()

  private def createGameTable(): ConnectionIO[Int] =
    sql"""
         | CREATE TABLE IF NOT EXISTS game(
         |  id INTEGER PRIMARY KEY,
         |  name VARCHAR NOT NULL,
         |  created_by INTEGER NOT NULL,
         |  FOREIGN KEY (created_by) REFERENCES user(id)
         | )
         |""".stripMargin.update.run
}
