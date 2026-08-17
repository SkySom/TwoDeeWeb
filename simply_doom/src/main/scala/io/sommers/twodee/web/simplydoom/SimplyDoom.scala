package io.sommers.twodee.web.simplydoom

import cats.effect.{ExitCode, IO, IOApp, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.log4cats.Log4CatsDebuggingLogHandler
import io.sommers.twodee.web.simplydoom.exception.*
import io.sommers.twodee.web.simplydoom.logic.{LogicProvider, TokenLogic, UserLogic}
import io.sommers.twodee.web.simplydoom.model.AllowAllPermission
import io.sommers.twodee.web.simplydoom.route.RouteProvider
import io.sommers.twodee.web.simplydoom.service.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.Origin
import org.http4s.server.middleware.{CORS, CORSPolicy}
import org.http4s.{Method, Response, Uri}
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt

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
      serviceProvider <- ServiceProvider(transactor, config)
      logicProvider <- LogicProvider(serviceProvider, config)
      _ <- checkMaster(logicProvider.tokenLogic, logicProvider.userLogic)(logger)
      routerProvider <- RouteProvider(logicProvider)
      exceptionHandler <- ExceptionHandler(loggerFactory)
      cors <- setupCors(config)
      exitCode <- EmberServerBuilder
        .default[IO]
        .withHttpApp(
          cors.httpApp(
            routerProvider.asRouter.orNotFound
          )
        )
        .withErrorHandler(exceptionHandler)
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
  
  private def setupCors(config: DoomConfig): IO[CORSPolicy] = {
    val corsWithoutHost = CORS.policy.withAllowHeadersAll
      .withAllowMethodsIn(Set(Method.GET, Method.POST, Method.DELETE))
      .withMaxAge(1.day)

    if (config.http.hosts.isEmpty) {
      IO.pure(corsWithoutHost.withAllowOriginAll)
    } else {
      for {
        originHosts <- IO.traverse(config.http.hosts.toList)(host => {
          if (host.startsWith("https://")) {
            IO.pure(Origin.Host(Uri.Scheme.https, Uri.RegName(host.replace("https://", "")), None))
          } else if (host.startsWith("http://")) {
            IO.pure(Origin.Host(Uri.Scheme.http, Uri.RegName(host.replace("http://", "")), None))
          } else {
            IO.raiseError(new IllegalArgumentException(s"$host is not valid"))
          }
        })
      } yield corsWithoutHost.withAllowOriginHost(originHosts.toSet)
    }
  }

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
            None,
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
