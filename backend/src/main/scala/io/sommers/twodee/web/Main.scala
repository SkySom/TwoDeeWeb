package io.sommers.twodee.web

import cats.*
import cats.effect.*
import doobie.Transactor
import io.sommers.twodee.web.config.MainConfig
import io.sommers.twodee.web.database.Database
import io.sommers.twodee.web.exception.{InvalidTokenException, NotFoundException}
import io.sommers.twodee.web.logic.{DoomPoolLogicImpl, GoogleLogic}
import io.sommers.twodee.web.route.{DoomPoolRoute, GoogleRoute, UIRoute}
import io.sommers.twodee.web.service.DoomPoolService
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

    resources()
      .use(runWith)
  }

  private def runWith(config: MainConfig, transactor: Transactor[IO])(implicit
      loggerFactory: LoggerFactory[IO]
  ): IO[ExitCode] = {
    for {
      doomService <- DoomPoolService.create(transactor)
      exitCode <- EmberServerBuilder
        .default[IO]
        .withHttpApp(
          Router(
            "/api" -> Router(
              "/doom-pool" -> DoomPoolRoute(
                DoomPoolLogicImpl(doomService)
              ).routes,
              "/google" -> GoogleRoute(
                config.oauth.google,
                GoogleLogic(config.oauth.google)
              ).routes
            ),
            "/" -> UIRoute().routes
          ).orNotFound
        )
        .withErrorHandler {
          case NotFoundException(message) => NotFound(message)
          case InvalidTokenException(message) => Forbidden(message)
        }
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield exitCode
  }

  private def resources()(implicit
      loggerFactory: LoggerFactory[IO]
  ): Resource[IO, (MainConfig, Transactor[IO])] = for {
    config <- MainConfig.loadResource()
    transactor <- Database.transactor(
      config.database,
      loggerFactory.getLoggerFromName("database")
    )
  } yield (config, transactor)
}
