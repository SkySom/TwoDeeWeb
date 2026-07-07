package io.sommers.twodee.web.simplydoom.service

import cats.effect.IO
import doobie.{ConnectionIO, Transactor}
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}

import java.time.Instant

trait TokenService {
  def getById(id: Long): IO[Option[(Long, String, Long, Boolean, Instant)]]

  def create(name: String, userId: Long): IO[Long]

  def delete(id: Long): IO[Int]
}

object TokenService {
  def apply(transactor: Transactor[IO]): IO[TokenService] = for {
    _ <- TokenServiceImpl.createTables()
      .transact(transactor)
  } yield TokenServiceImpl(transactor)
}

case class TokenServiceImpl(
    transactor: Transactor[IO]
) extends TokenService {
  override def create(name: String, userId: Long): IO[Long] = {
    sql"""
         | INSERT INTO token(name, user_id)
         | VALUES($name, $userId)
         |""".stripMargin.update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
  }

  override def delete(id: Long): IO[Int] = {
    sql"UPDATE token set active = 0, update_at = CURRENT_TIMESTAMP where id = $id".update.run
      .transact(transactor)
  }

  override def getById(id: Long): IO[Option[(Long, String, Long, Boolean, Instant)]] =
    sql"SELECT id, name, user_id, active, created_at from token"
      .query[(Long, String, Long, Boolean, Long)]
      .option
      .transact(transactor)
      .map(result => result.map(queried => (queried._1, queried._2, queried._3, queried._4, Instant.ofEpochMilli(queried._5))))
}

object TokenServiceImpl {
  def createTables(): ConnectionIO[Int] = createTokenTable

  private def createTokenTable: ConnectionIO[Int] =
    sql"""
         | CREATE TABLE IF NOT EXISTS token(
         |  id INTEGER PRIMARY KEY,
         |  name VARCHAR NOT NULL,
         |  user_id INTEGER NOT NULL,
         |  active INTEGER NOT NULL DEFAULT 1,
         |  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |  FOREIGN KEY (user_id) REFERENCES user(id)
         | )
       """.stripMargin.update.run
}
