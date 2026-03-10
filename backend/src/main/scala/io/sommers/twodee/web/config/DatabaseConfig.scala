package io.sommers.twodee.web.config

import cats.effect.IO
import ciris.circe.yaml.circeYamlConfigDecoder
import ciris.{ConfigDecoder, ConfigValue}
import com.zaxxer.hikari.HikariConfig
import io.circe.generic.auto.deriveDecoder

import java.util
import java.util.Properties

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
  given ConfigDecoder[String, DatabaseConfig] = circeYamlConfigDecoder("DatabaseConfig")
}
