package io.sommers.twodee.web.simplydoom.route

import cats.effect.IO
import io.circe.generic.auto.*
import io.sommers.twodee.web.simplydoom.exception.MissingPermissionException
import io.sommers.twodee.web.simplydoom.logic.{TokenLogic, UserLogic}
import io.sommers.twodee.web.simplydoom.model.{Token, TokenRequest, User, UserRequest}
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*
import org.http4s.{AuthedRequest, AuthedRoutes, EntityDecoder, HttpRoutes, Response}

private case class TokenRoute(
  tokenLogic: TokenLogic,
  userLogic: UserLogic
) {
  implicit val tokenRequestDecoder: EntityDecoder[IO, TokenRequest] =
    jsonOf[IO, TokenRequest]

  private def routes: HttpRoutes[IO] =
    tokenLogic.middleware(AuthedRoutes.of[Token, IO] {
      case GET -> Root as token                  => getToken(token.id)(token)
      case GET -> Root / LongVar(id) as token    => getToken(id)(token)
      case DELETE -> Root as token               => deleteToken(token.id)(token)
      case DELETE -> Root / LongVar(id) as token => deleteToken(id)(token)
      case req @ POST -> Root as token           => createToken(req)
    })

  private def getToken(id: Long)(authToken: Token): IO[Response[IO]] = for {
    _ <- IO.raiseWhen(
      id != authToken.id && !authToken.user.tokenPermission.isValid(id.toString)
    )(MissingPermissionException(s"Cannot read id $id"))
    token <- userLogic.getUser(id)
    response <- Ok(token)
  } yield response

  private def deleteToken(id: Long)(authToken: Token): IO[Response[IO]] = for {
    _ <- IO.raiseWhen(
      id != authToken.id && !authToken.user.tokenPermission.isValid(id.toString)
    )(MissingPermissionException(s"Cannot delete id $id"))
    _ <- tokenLogic.deleteToken(id)
    response <- NoContent()
  } yield response

  private def createToken(value: AuthedRequest[IO, Token]): IO[Response[IO]] =
    for {
      _ <- IO.raiseWhen(value.context.user.tokenPermission.isValid("*"))(
        MissingPermissionException("Cannot create token")
      )
      tokenRequest <- value.req.as[TokenRequest]
      token <- tokenLogic.createToken(
        tokenRequest.name,
        tokenRequest.userId
      )
      response <- Ok(token)
    } yield response
}

object TokenRoute {
  def apply(tokenLogic: TokenLogic, userLogic: UserLogic): HttpRoutes[IO] =
    new TokenRoute(
      tokenLogic,
      userLogic
    ).routes
}
