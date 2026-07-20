package io.sommers.twodee.web.simplydoom.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.simplydoom.exception.MissingPermissionException
import io.sommers.twodee.web.simplydoom.logic.{DoomPoolLogic, TokenLogic}
import io.sommers.twodee.web.simplydoom.model.{
  DoomPoolRequest,
  DoomUpdateRequest,
  DoomUpdateResponse,
  Token
}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*
import org.http4s.{AuthedRequest, AuthedRoutes, EntityDecoder, HttpRoutes, Response}

private case class DoomPoolRoute(
    tokenLogic: TokenLogic,
    doomPoolLogic: DoomPoolLogic
) {
  implicit val doomRequestDecoder: EntityDecoder[IO, DoomPoolRequest] =
    jsonOf[IO, DoomPoolRequest]

  implicit val doomUpdateRequestDecode: EntityDecoder[IO, DoomUpdateRequest] =
    jsonOf[IO, DoomUpdateRequest]

  private def routes: HttpRoutes[IO] =
    tokenLogic.middleware(AuthedRoutes.of[Token, IO] {
      case req @ GET -> Root as token         => listDoomPools(req.req.params)(token)
      case GET -> Root / LongVar(id) as token => getDoomPool(id)(token)
      case req @ POST -> Root as token        => createDoomPool(req)
      case req @ POST -> Root / LongVar(id) / "doom" as token => updateDoom(id, req)
    })

  private def listDoomPools(
      filters: Map[String, String]
  )(token: Token): IO[Response[IO]] = for {
    doomPools <- doomPoolLogic.list(filters)
    allowedDoomPools <- IO.pure(
      doomPools.filter(doomPool => token.user.doomPermission.isValid(doomPool.id.toString))
    )
    response <- Ok(allowedDoomPools)
  } yield response

  private def getDoomPool(id: Long)(token: Token): IO[Response[IO]] = for {
    _ <- IO.raiseWhen(
      !token.user.doomPermission.isValid(id.toString)
    )(MissingPermissionException(s"Cannot read id $id"))
    doomPool <- doomPoolLogic.getById(id)
    response <- Ok(doomPool)
  } yield response

  private def createDoomPool(
      value: AuthedRequest[IO, Token]
  ): IO[Response[IO]] =
    for {
      _ <- IO.raiseWhen(!value.context.user.doomPermission.isValid("*"))(
        MissingPermissionException("Cannot create doom pool")
      )
      doomPoolRequest <- value.req.as[DoomPoolRequest]
      user <- doomPoolLogic.create(
        doomPoolRequest.name,
        doomPoolRequest.doom.getOrElse(0)
      )
      response <- Ok(user)
    } yield response

  private def updateDoom(id: Long, req: AuthedRequest[IO, Token]): IO[Response[IO]] =
    for {
      _ <- IO.raiseWhen(!req.context.user.doomPermission.isValid(id.toString))(
        MissingPermissionException("Cannot update doom")
      )
      doomUpdateRequest <- req.req.as[DoomUpdateRequest]
      doomPool <- doomPoolLogic.getById(id)
      _ <- doomPoolLogic.changeDoomAmount(id, doomUpdateRequest.amount)
      response <- Ok(
        DoomUpdateResponse(
          doomPool.doom,
          doomPool.doom + doomUpdateRequest.amount
        )
      )
    } yield response
}

object DoomPoolRoute {
  def apply(
      tokenLogic: TokenLogic,
      doomPoolLogic: DoomPoolLogic
  ): HttpRoutes[IO] =
    new DoomPoolRoute(tokenLogic, doomPoolLogic).routes
}
