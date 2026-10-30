package io.sommers.twodee.web.simplydoom.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.simplydoom.exception.MissingPermissionException
import io.sommers.twodee.web.simplydoom.logic.{CharacterLogic, DoomPoolLogic, RollLogic, TokenLogic}
import io.sommers.twodee.web.simplydoom.model.{RollRequest, Token}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*
import org.http4s.{AuthedRequest, AuthedRoutes, EntityDecoder, HttpRoutes, Response}

private case class RollRoute(
    tokenLogic: TokenLogic,
    characterLogic: CharacterLogic,
    doomPoolLogic: DoomPoolLogic,
    rollLogic: RollLogic
) {
  implicit val rollRequest: EntityDecoder[IO, RollRequest] = jsonOf[IO, RollRequest]

  private def routes: HttpRoutes[IO] = tokenLogic.middleware(AuthedRoutes.of[Token, IO] {
    case req @ POST -> Root as token => postRoll(req)
  })

  private def postRoll(authRequest: AuthedRequest[IO, Token]): IO[Response[IO]] = for {
    rollRequest <- authRequest.req.as[RollRequest]
    character <- rollRequest.characterId
      .map(id =>
        if (authRequest.context.user.characterPermissions.isValid(id.toString)) {
          characterLogic
            .getById(id, true)
            .map(Some(_))
        } else IO.raiseError(MissingPermissionException(s"Cannot get character $id"))
      )
      .getOrElse(IO.pure(None))
    doomPool <- rollRequest.doomPoolId
      .map(id =>
        if (authRequest.context.user.doomPermission.isValid(id.toString)) {
          doomPoolLogic
            .getById(id)
            .map(Some(_))
        } else IO.raiseError(MissingPermissionException(s"Cannot get doom $id"))
      )
      .getOrElse(IO.pure(None))
    rollResult <- rollLogic.roll(
      rollRequest.formula,
      rollRequest.options,
      character,
      doomPool
    )
    response <- Ok(rollResult)
  } yield response
}

object RollRoute {
  def apply(
      tokenLogic: TokenLogic,
      characterLogic: CharacterLogic,
      doomPoolLogic: DoomPoolLogic,
      rollLogic: RollLogic
  ): HttpRoutes[IO] =
    new RollRoute(tokenLogic, characterLogic, doomPoolLogic, rollLogic).routes
}
