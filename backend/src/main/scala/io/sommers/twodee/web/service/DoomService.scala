package io.sommers.twodee.web.service

import cats.effect.IO
import cats.implicits.*
import doobie.*
import doobie.free.connection.ConnectionIO
import doobie.implicits.*

trait DoomService {
  def insertDoomPool(name: String, startingDoom: Int): IO[Long]

  def insertDoomTransaction(
      doomPoolId: Long,
      doom: Int,
      note: Option[String]
  ): IO[Long]

  def listDoomPools(): IO[List[(Long, String, Int)]]

  def getDoomPool(id: Long): IO[Option[(Long, String, Int)]]
  
  def updateDoomPool(id: Long, name: String): IO[Int]
}

object DoomService {
  def create(transactor: Transactor[IO]): IO[DoomService] = for {
    _ <- DoomServiceImpl
      .createTables()
      .transact(transactor)
  } yield DoomServiceImpl(transactor)
}

class DoomServiceImpl(
    transactor: Transactor[IO]
) extends DoomService {

  override def insertDoomPool(name: String, startingDoom: Int): IO[Long] = {
    insertDoomPoolSQL(name, startingDoom).transact(transactor)
  }

  override def listDoomPools(): IO[List[(Long, String, Int)]] = {
    list().transact(transactor)
  }

  override def insertDoomTransaction(
      doomPoolId: Long,
      doom: Int,
      note: Option[String]
  ): IO[Long] = {
    insertDoomTransactionSQL(doomPoolId, doom, note)
      .transact(transactor)
  }

  override def getDoomPool(id: Long): IO[Option[(Long, String, Int)]] =
    getOne(id)
      .transact(transactor)

  override def updateDoomPool(id: Long, name: String): IO[Int] =
    updateOne(id, name)
      .run
      .transact(transactor)

  private def insertDoomPoolSQL(
      name: String,
      startingDoom: Int
  ): ConnectionIO[Long] = {
    sql"insert into doom_pool(name, doom) values($name, $startingDoom)".update
      .withUniqueGeneratedKeys("id")
  }

  private def insertDoomTransactionSQL(
      doomPoolId: Long,
      doom: Int,
      note: Option[String]
  ): ConnectionIO[Long] = {
    sql"INSERT INTO doom_transaction(doom_pool_id, amount, note) VALUES ($doomPoolId, $doom, $note)".update
      .withUniqueGeneratedKeys("id")
  }

  private def list() = {
    sql"select id, name, doom from doom_pool"
      .query[(Long, String, Int)]
      .to[List]
  }

  private def getOne(id: Long) = {
    sql"select id, name, doom from doom_pool where id = $id"
      .query[(Long, String, Int)]
      .option
  }

  private def updateOne(id: Long, name: String) = {
    sql"update doom_pool set name = $name where id = $id"
      .update
  }
}

object DoomServiceImpl {
  def createTables(): ConnectionIO[Int] = (
    createDoomPoolTable,
    createDoomTransactionTable,
    deleteDoomTransactionTrigger(),
    createDoomTransactionTrigger
  ).mapN(_ + _ + _ + _)

  private def createDoomPoolTable: ConnectionIO[Int] =
    sql"""
         | CREATE TABLE IF NOT EXISTS doom_pool(
         |  id INTEGER PRIMARY KEY,
         |  name VARCHAR NOT NULL UNIQUE,
         |  doom INT
         |)
         |""".stripMargin.update.run

  private def createDoomTransactionTable: ConnectionIO[Int] =
    sql"""
         |  CREATE TABLE IF NOT EXISTS doom_transaction(
         |    id INTEGER PRIMARY KEY,
         |    doom_pool_id INTEGER NOT NULL,
         |    amount INT,
         |    note TEXT NULL
         |  )
         |""".stripMargin.update.run

  private def deleteDoomTransactionTrigger(): ConnectionIO[Int] =
    sql"""DROP TRIGGER IF EXISTS doom_transaction_trigger""".update.run

  private def createDoomTransactionTrigger: ConnectionIO[Int] =
    sql"""
         |  CREATE TRIGGER doom_transaction_trigger
         |    AFTER INSERT ON doom_transaction
         |    FOR EACH ROW
         |    BEGIN
         |      UPDATE doom_pool set doom = doom + new.amount WHERE id = new.doom_pool_id;
         |    end;
         |""".stripMargin.update.run
}
