package io.sommers.twodee.web.config

import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.ConfigValue
import com.typesafe.config.{Config, ConfigFactory}
import org.typelevel.log4cats.LoggerFactory

case class MainConfig(
    databaseConfig: DatabaseConfig,
    httpConfig: HTTPConfig
)

object MainConfig {

  def loadResource()(implicit
      loggerFactory: LoggerFactory[IO]
  ): Resource[IO, MainConfig] = Resource.eval { load() }

  def load()(implicit loggerFactory: LoggerFactory[IO]): IO[MainConfig] = for {
    hoconConfig <- IO(ConfigFactory.load)
    mainConfig <- load(hoconConfig).load[IO]
  } yield mainConfig

  private def load(config: Config)(implicit
      loggerFactory: LoggerFactory[IO]
  ): ConfigValue[IO, MainConfig] = for {
    httpConfig <- HTTPConfig.load(config)
    databaseConfig <- DatabaseConfig.load(config)
  } yield MainConfig(databaseConfig, httpConfig)
}
