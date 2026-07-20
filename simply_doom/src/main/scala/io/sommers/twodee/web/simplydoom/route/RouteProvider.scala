package io.sommers.twodee.web.simplydoom.route

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.logic.LogicProvider
import org.http4s.HttpRoutes
import org.http4s.server.Router

trait RouteProvider {
  def asRouter: HttpRoutes[IO]
}

object RouteProvider {
  def apply(logicProvider: LogicProvider): IO[RouteProvider] = IO.pure(RouterProviderImpl(
    CharacterRoute(logicProvider.tokenLogic, logicProvider.characterLogic, logicProvider.userLogic),
    DoomPoolRoute(logicProvider.tokenLogic, logicProvider.doomPoolLogic),
    TokenRoute(logicProvider.tokenLogic, logicProvider.userLogic),
    UserRoute(logicProvider.tokenLogic, logicProvider.userLogic)
  ))
}

private case class RouterProviderImpl(
    characterRoutes: HttpRoutes[IO],
    doomPoolRoutes: HttpRoutes[IO],
    tokenRoutes: HttpRoutes[IO],
    userRoutes: HttpRoutes[IO]
) extends RouteProvider {
  override def asRouter: HttpRoutes[IO] = Router(
    "/character" -> characterRoutes,
    "/doompool" -> doomPoolRoutes,
    "/token" -> tokenRoutes,
    "/user" -> userRoutes
  )
}
