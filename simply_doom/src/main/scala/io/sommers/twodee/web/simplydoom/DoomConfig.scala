package io.sommers.twodee.web.simplydoom

import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.*
import ciris.circe.yaml.circeYamlConfigDecoder
import com.zaxxer.hikari.HikariConfig
import io.circe.generic.auto.deriveDecoder
import org.typelevel.log4cats.LoggerFactory

import java.nio.file.Path

case class DoomConfig(
    database: DatabaseConfig,
    http: HTTPConfig,
    auth: AuthConfig
)

object DoomConfig {
  given ConfigDecoder[String, DoomConfig] = circeYamlConfigDecoder("DoomConfig")

  def loadResource()(implicit
      loggerFactory: LoggerFactory[IO]
  ): Resource[IO, DoomConfig] = Resource.eval {
    parseConfig().load[IO]
  }

  private def parseConfig()(implicit
      loggerFactory: LoggerFactory[IO]
  ): ConfigValue[IO, DoomConfig] = for {
    filePath <- prop("config.file")
      .or(env("CONFIG_FILE"))
      .as[String]
      .map(Path.of(_))
    mainConfig <- file(filePath)
      .as[DoomConfig]
  } yield mainConfig
}

case class DatabaseConfig(
    jdbcUrl: String,
    maximumPoolSize: Option[Int],
    minimumIdle: Option[Int],
    connectionTimeout: Option[Long],
    idleTimeout: Option[Long],
    maxLifetime: Option[Long],
    leakDetectionThreshold: Option[Long]
) {
  def toHikari: HikariConfig = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(jdbcUrl)
    maximumPoolSize.foreach(hikariConfig.setMaximumPoolSize)
    minimumIdle.foreach(hikariConfig.setMinimumIdle)
    connectionTimeout.foreach(hikariConfig.setConnectionTimeout)
    idleTimeout.foreach(hikariConfig.setIdleTimeout)
    maxLifetime.foreach(hikariConfig.setMaxLifetime)
    leakDetectionThreshold.foreach(hikariConfig.setLeakDetectionThreshold)
    hikariConfig
  }
}

object DatabaseConfig {
  given ConfigDecoder[String, DatabaseConfig] = circeYamlConfigDecoder(
    "DatabaseConfig"
  )
}

case class AuthConfig(
    secretKey: String
)

case class HTTPConfig(
    port: Int
)
