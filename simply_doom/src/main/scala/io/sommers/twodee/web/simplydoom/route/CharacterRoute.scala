package io.sommers.twodee.web.simplydoom.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.simplydoom.exception.MissingPermissionException
import io.sommers.twodee.web.simplydoom.logic.{CharacterLogic, DoomPoolLogic, TokenLogic, UserLogic}
import io.sommers.twodee.web.simplydoom.model.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*
import org.http4s.{AuthedRequest, AuthedRoutes, EntityDecoder, HttpRoutes, Response}

private case class CharacterRoute(
    tokenLogic: TokenLogic,
    characterLogic: CharacterLogic,
    userLogic: UserLogic
) {
  implicit val characterCreateRequestDecoder: EntityDecoder[IO, CharacterCreateRequest] =
    jsonOf[IO, CharacterCreateRequest]

  private def routes: HttpRoutes[IO] =
    tokenLogic.middleware(AuthedRoutes.of[Token, IO] {
      case GET -> Root as token                               => listCharacters(Map())(token)
      case GET -> Root / LongVar(id) as token                 => getCharacter(id)(token)
      case req @ POST -> Root as token                        => createCharacter(req)
      //case req @ POST -> Root / LongVar(id) / "doom" as token => updateDoom(id, req)
    })

  private def listCharacters(
      filters: Map[String, String]
  )(token: Token): IO[Response[IO]] = for {
    characters <- characterLogic.list(filters)
    allowedCharacters <- IO.pure(
      characters.filter(character => token.user.characterPermissions.isValid(character.id.toString))
    )
    response <- Ok(allowedCharacters)
  } yield response

  private def getCharacter(id: Long)(token: Token): IO[Response[IO]] = for {
    _ <- IO.raiseWhen(
      !token.user.characterPermissions.isValid(id.toString)
    )(MissingPermissionException(s"Cannot read id $id"))
    character <- characterLogic.getById(id)
    response <- Ok(character)
  } yield response

  private def createCharacter(
      request: AuthedRequest[IO, Token]
  ): IO[Response[IO]] =
    for {
      _ <- IO.raiseWhen(!request.context.user.characterPermissions.isValid("*"))(
        MissingPermissionException("Cannot create character")
      )
      characterCreateRequest <- request.req.as[CharacterCreateRequest]
      owner <- characterCreateRequest.ownerId.fold(IO.pure(request.context.user))(
        userLogic.getUser
      )
      character <- characterLogic.create(
        characterCreateRequest.name,
        characterCreateRequest.sheet,
        owner
      )
      response <- Ok(character)
    } yield response

  /*
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
   */
}

object CharacterRoute {
  def apply(
      tokenLogic: TokenLogic,
      characterLogic: CharacterLogic,
      userLogic: UserLogic
  ): HttpRoutes[IO] =
    new CharacterRoute(tokenLogic, characterLogic, userLogic).routes
}
