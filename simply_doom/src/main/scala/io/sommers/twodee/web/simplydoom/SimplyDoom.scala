package io.sommers.twodee.web.simplydoom

import cats.effect.{ExitCode, IO, IOApp, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.log4cats.Log4CatsDebuggingLogHandler
import fs2.Stream
import io.sommers.twodee.web.simplydoom.exception.{InvalidFieldException, InvalidTokenException, MissingPermissionException, NotFoundException, SheetException}
import io.sommers.twodee.web.simplydoom.logic.{CharacterLogic, DoomPoolLogic, TokenLogic, UserLogic}
import io.sommers.twodee.web.simplydoom.model.AllowAllPermission
import io.sommers.twodee.web.simplydoom.route.{CharacterRoute, DoomPoolRoute, TokenRoute, UserRoute}
import io.sommers.twodee.web.simplydoom.service.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.{Response, Status}
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.{Logger, LoggerFactory}

case class SimplyDoom()

object SimplyDoom extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

    resources()
      .use(runWith)
  }

  private def runWith(config: DoomConfig, transactor: Transactor[IO])(implicit
      loggerFactory: LoggerFactory[IO]
  ): IO[ExitCode] = {
    for {
      logger <- loggerFactory.fromClass(classOf[SimplyDoom])
      userService <- UserService(transactor)
      userLogic <- IO.pure(UserLogic(userService))
      tokenService <- TokenService(transactor)
      tokenLogic <- IO.pure(TokenLogic(config.auth, userLogic, tokenService))
      _ <- checkMaster(tokenLogic, userLogic)(logger)
      doomPoolService <- DoomPoolService(transactor)
      doomPoolLogic <- IO.pure(DoomPoolLogic(doomPoolService))
      sheetsService <- SheetsService(config)
      characterService <- CharacterService(transactor)
      characterLogic <- CharacterLogic(characterService, sheetsService)
      exitCode <- EmberServerBuilder
        .default[IO]
        .withHttpApp(
          Router(
            "/user" -> UserRoute(tokenLogic, userLogic),
            "/token" -> TokenRoute(tokenLogic, userLogic),
            "/doompool" -> DoomPoolRoute(tokenLogic, doomPoolLogic),
            "/character" -> CharacterRoute(tokenLogic, characterLogic, userLogic)
          ).orNotFound
        )
        .withErrorHandler {
          case MissingPermissionException(message) =>
            IO.pure(
              Response(
                status = Status.Forbidden,
                body = Stream.emits(message.getBytes().toSeq)
              )
            )
          case _: NotFoundException => NotFound()
          case InvalidTokenException(message) =>
            IO.pure(
              Response(
                status = Status.Forbidden,
                body = Stream.emits(message.getBytes().toSeq)
              )
            )
          case SheetException(message) =>
            IO.pure(
              Response(
                status = Status.InternalServerError,
                body = Stream.emits(message.getBytes().toSeq)
              )
            )
          case InvalidFieldException(message) => IO.pure(
            Response(
              status = Status.BadRequest,
              body = Stream.emits(message.getBytes().toSeq)
            )
          )
          case e: Exception => for {
            _ <- logger.error(e)("Ran into Issue")
            response <- InternalServerError()
          } yield response
        }
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield exitCode
  }

  private def resources()(implicit
      loggerFactory: LoggerFactory[IO]
  ): Resource[IO, (DoomConfig, Transactor[IO])] = for {
    config <- DoomConfig.loadResource()
    transactor <- HikariTransactor.fromHikariConfig(
      config.database.toHikari,
      Some(
        Log4CatsDebuggingLogHandler(loggerFactory.getLoggerFromName("database"))
      )
    )
  } yield (config, transactor)

  private def checkMaster(tokenLogic: TokenLogic, userLogic: UserLogic)(
      logger: Logger[IO]
  ): IO[Unit] = for {
    users <- userLogic.searchUsers(Map())
    token <-
      if (users.isEmpty) {
        for {
          user <- userLogic.createUser(
            "MASTER",
            AllowAllPermission,
            AllowAllPermission,
            AllowAllPermission,
            AllowAllPermission,
            "Default Master Token",
            None
          )
          token <- tokenLogic.createToken("MASTER", user.id)
        } yield Some(token)
      } else {
        IO.pure(None)
      }
    _ <- IO.whenA(token.isDefined)(
      logger.info(s"Created Master Token ${token.flatMap(_.jwt).mkString}")
    )
  } yield ()
}
