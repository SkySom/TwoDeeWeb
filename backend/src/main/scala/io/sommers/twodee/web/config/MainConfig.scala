package io.sommers.twodee.web.config

import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.*
import ciris.circe.yaml.circeYamlConfigDecoder
import io.circe.generic.auto.deriveDecoder
import org.typelevel.log4cats.LoggerFactory

import java.nio.file.Path

case class MainConfig(
  database: DatabaseConfig,
  http: HTTPConfig,
  oauth: OAuthConfig
)

object MainConfig {
  given ConfigDecoder[String, MainConfig] = circeYamlConfigDecoder("MainConfig")

  def loadResource()(implicit
      loggerFactory: LoggerFactory[IO]
  ): Resource[IO, MainConfig] = Resource.eval {
    parseConfig().load[IO]
  }

  private def parseConfig()(implicit
      loggerFactory: LoggerFactory[IO]
  ): ConfigValue[IO, MainConfig] = for {
    filePath <- prop("config.file")
      .or(env("CONFIG_FILE"))
      .as[String]
      .map(Path.of(_))
    mainConfig <- file(filePath)
      .as[MainConfig]
  } yield mainConfig
}
