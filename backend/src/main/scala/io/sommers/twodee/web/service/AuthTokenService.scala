package io.sommers.twodee.web.service

import cats.effect.IO
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import doobie.{ConnectionIO, Transactor}
import io.sommers.twodee.web.model.auth.AuthTokenKind

trait AuthTokenService {
  def getTokenById(id: Long): IO[Option[(Long, AuthTokenKind, Boolean, Long)]]

  def createToken(kind: AuthTokenKind, createdBy: Long): IO[Long]
  
  def deactivateToken(id: Long): IO[Int]
}

object AuthTokenService {
  def apply(transactor: Transactor[IO]): IO[AuthTokenService] = for {
    _ <- AuthTokenServiceImpl.createTables()
      .transact(transactor)
  } yield AuthTokenServiceImpl(transactor)
}

case class AuthTokenServiceImpl(
    transactor: Transactor[IO]
) extends AuthTokenService {

  override def getTokenById(id: Long): IO[Option[(Long, AuthTokenKind, Boolean, Long)]] =
    sql"select id, kind, active, created_by from auth_token where id = $id"
      .query[(Long, AuthTokenKind, Boolean, Long)]
      .option
      .transact(transactor)

  override def createToken(kind: AuthTokenKind, createdBy: Long): IO[Long] =
    sql"INSERT INTO auth_token(kind, created_by) VALUES ($kind, $createdBy)".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)

  override def deactivateToken(id: Long): IO[Int] =
    sql"UPDATE auth_token set active = 0 where id = $id"
      .update
      .run
      .transact(transactor)
}

object AuthTokenServiceImpl {
  def createTables(): ConnectionIO[Int] = createAuthTokenTable

  private def createAuthTokenTable: ConnectionIO[Int] =
    sql"""
         | CREATE TABLE IF NOT EXISTS auth_token(
         |  id INTEGER PRIMARY KEY,
         |  kind VARCHAR NOT NULL,
         |  active INTEGER NOT NULL DEFAULT 1,
         |  created_by INTEGER NOT NULL,
         |  FOREIGN KEY (created_by) REFERENCES user (id)
         | )
       """.stripMargin.update.run
}


