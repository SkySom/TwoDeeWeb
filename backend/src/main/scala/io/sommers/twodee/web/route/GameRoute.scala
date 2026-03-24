package io.sommers.twodee.web.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.logic.{AuthLogic, GameLogic}
import io.sommers.twodee.web.model.auth.{AuthToken, UserAuthToken}
import io.sommers.twodee.web.model.request.game.{CreateGameRequest, CreateGameResponse}
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*

case class GameRoute(
    authLogic: AuthLogic,
    gameLogic: GameLogic
) {
  implicit val gameCreateDecoder: EntityDecoder[IO, CreateGameRequest] =
    jsonOf[IO, CreateGameRequest]

  def routes: HttpRoutes[IO] =
    authLogic.middleware(AuthedRoutes.of[AuthToken, IO] {
      case req @ POST -> Root as authToken        => createGame(req)
      case GET -> Root / LongVar(id) as authToken => getGame(id)
    })

  private def createGame(
      request: AuthedRequest[IO, AuthToken]
  ): IO[Response[IO]] = for {
    body <- request.req.as[CreateGameRequest]
    user <- UserAuthToken
      .fromTokenIO(request.context)
      .map(_.user)
    game <- gameLogic.createGame(body.name, user.id)
    response <- Ok(CreateGameResponse(game))
  } yield response

  private def getGame(id: Long): IO[Response[IO]] = for {
    gameOpt <- gameLogic.getGameById(id)
    response <- gameOpt.fold(NotFound())(Ok(_))
  } yield response
}
