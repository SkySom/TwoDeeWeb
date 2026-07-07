package io.sommers.twodee.web.simplydoom.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.simplydoom.exception.MissingPermissionException
import io.sommers.twodee.web.simplydoom.logic.{TokenLogic, UserLogic}
import io.sommers.twodee.web.simplydoom.model.{Token, User, UserRequest}
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*
import org.http4s.{AuthedRequest, AuthedRoutes, EntityDecoder, HttpRoutes, Response}

private case class UserRoute(
  tokenLogic: TokenLogic,
  userLogic: UserLogic
) {
  implicit val userRequestDecoder: EntityDecoder[IO, UserRequest] =
    jsonOf[IO, UserRequest]

  private def routes: HttpRoutes[IO] =
    tokenLogic.middleware(AuthedRoutes.of[Token, IO] {
      case GET -> Root as token                  => getUser(token.user.id)(token)
      case GET -> Root / LongVar(id) as token    => getUser(id)(token)
      case DELETE -> Root as token               => deleteUser(token.id)(token)
      case DELETE -> Root / LongVar(id) as token => deleteUser(id)(token)
      case req @ POST -> Root as token           => createUser(req)
    })

  private def getUser(id: Long)(token: Token): IO[Response[IO]] = for {
    _ <- IO.raiseWhen(
      id != token.user.id && !token.user.userPermission.isValid(id.toString)
    )(MissingPermissionException(s"Cannot read id $id"))
    user <- userLogic.getUser(id)
    response <- Ok(user)
  } yield response

  private def deleteUser(id: Long)(token: Token): IO[Response[IO]] = for {
    _ <- IO.raiseWhen(
      id != token.user.id && !token.user.userPermission.isValid(id.toString)
    )(MissingPermissionException(s"Cannot delete id $id"))
    _ <- userLogic.deleteUser(id)
    response <- NoContent()
  } yield response

  private def createUser(value: AuthedRequest[IO, Token]): IO[Response[IO]] =
    for {
      _ <- IO.raiseWhen(value.context.user.userPermission.isValid("*"))(
        MissingPermissionException("Cannot create token")
      )
      userRequest <- value.req.as[UserRequest]
      user <- userLogic.createUser(
        userRequest.name,
        userRequest.plotPermission,
        userRequest.doomPermission,
        userRequest.userPermission,
        userRequest.tokenPermission,
        userRequest.notes,
        Some(value.context.id)
      )
      response <- Ok(user)
    } yield response
}

object UserRoute {
  def apply(tokenLogic: TokenLogic, userLogic: UserLogic): HttpRoutes[IO] =
    new UserRoute(
      tokenLogic,
      userLogic
    ).routes
}
