package io.sommers.twodee.web.database

import cats.effect.{IO, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.log4cats.Log4CatsDebuggingLogHandler
import io.sommers.twodee.web.config.DatabaseConfig
import org.typelevel.log4cats.Logger

object Database {
  def transactor(
      config: DatabaseConfig,
      logger: Logger[IO]
  ): Resource[IO, Transactor[IO]] =
    HikariTransactor.fromHikariConfig[IO](
      config.toHikari,
      Some(Log4CatsDebuggingLogHandler[IO](logger))
    )
}
