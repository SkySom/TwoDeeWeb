package io.sommers.twodee.web.config

import cats.effect.IO
import ciris.ConfigValue
import com.typesafe.config.{Config, ConfigValue as HoconConfigValue}
import com.zaxxer.hikari.HikariConfig
import lt.dvim.ciris.Hocon.hoconAt

import java.util
import java.util.Properties

case class DatabaseConfig(
    hikariConfig: HikariConfig
)

object DatabaseConfig {
  def load(config: Config): ConfigValue[IO, DatabaseConfig] = {
    val databaseHocon = hoconAt(config)("database")
    for {
      hikari <- databaseHocon("hikari")
        .map(readToProperties)
        .map(HikariConfig(_))
    } yield DatabaseConfig(hikari)
  }

  private def readToProperties(
      config: HoconConfigValue
  ): Properties = {
    val properties = new Properties()
    readConfig(config.unwrapped(), None, properties)
    properties
  }

  private def readConfig(
      value: AnyRef,
      parentOpt: Option[String],
      properties: Properties
  ): Unit = {
    val pathName: String => Option[String] = child =>
      Some(parentOpt.fold(child)(parent => s"$parent.$child"))
    (value, parentOpt) match {
      case (map: util.Map[_, _], parent) =>
        map
          .entrySet()
          .forEach(entry =>
            readConfig(
              entry.getValue,
              pathName(entry.getKey.toString),
              properties
            )
          )
      case (value, Some(parent)) =>
        properties.put(parent, value)
      case (value, None) =>
        throw new IllegalArgumentException("database.hikari must be a map")
    }
  }
}
