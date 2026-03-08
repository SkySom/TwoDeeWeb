package io.sommers.twodee.web.config

import cats.effect.IO
import ciris.*
import com.typesafe.config.{Config, ConfigFactory}
import lt.dvim.ciris.Hocon.*

case class HTTPConfig(
    port: Int
)
object HTTPConfig {

  def load(config: Config): ConfigValue[IO, HTTPConfig] = {
    val httpHocon = hoconAt(config)("http")
    for {
      port <- httpHocon("port").as[Int]
    } yield HTTPConfig(port)
  }
}
