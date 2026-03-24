package io.sommers.twodee.web

import cats.*
import cats.effect.*
import doobie.Transactor
import io.sommers.twodee.web.config.MainConfig
import io.sommers.twodee.web.database.Database
import io.sommers.twodee.web.exception.{EndpointException, InvalidTokenException, NotFoundException}
import io.sommers.twodee.web.logic.{AuthLogic, DoomPoolLogicImpl, GoogleLogic, UserLogic}
import io.sommers.twodee.web.route.{DoomPoolRoute, GoogleRoute, UIRoute, UserRoute}
import io.sommers.twodee.web.service.{AuthTokenService, DoomPoolService, UserService}
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
      userService <- UserService.create(transactor)
      userLogic <- IO.pure(UserLogic(userService))
      authTokenService <- AuthTokenService(transactor)
      authLogic <- IO.pure(AuthLogic(config.auth, userLogic, authTokenService))
      exitCode <- EmberServerBuilder
        .default[IO]
        .withHttpApp(
          Router(
            "/api" -> Router(
              "/doom-pool" -> DoomPoolRoute(
                DoomPoolLogicImpl(doomService)
              ).routes,
              "/google" -> GoogleRoute(
                config.auth.google,
                GoogleLogic(config.auth.google),
                userLogic,
                authLogic
              ).routes,
              "/user" -> UserRoute(
                userLogic,
                authLogic
              ).routes
            ),
            "/" -> UIRoute().routes
          ).orNotFound
        )
        .withErrorHandler {
          case NotFoundException(message)     => NotFound(message)
          case InvalidTokenException(message) => Forbidden(message)
          case e: EndpointException => e.asResponse
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
