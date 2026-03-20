package io.sommers.twodee.web.frontend.storage

import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.state.Var
import com.raquo.airstream.web.WebStorageVar
import io.circe.generic.auto.deriveDecoder
import io.circe.parser
import io.sommers.twodee.web.model.response.WhoAmIResponse
import io.sommers.twodee.web.model.user.LoggedInUser
import sttp.client4.{UriContext, WebSocketBackend, basicRequest}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Try

object LoginInfoStorage {
  private val tokenStorage: WebStorageVar[String] = WebStorageVar
    .localStorage(key = "auth_token", syncOwner = None)
    .text(default = "")

  def mount(owner: Owner): Unit = {
    tokenStorage.syncFromExternalUpdates(owner)
  }

  def getLoggedInUserVar()(implicit
      backend: WebSocketBackend[Future]
  ): Var[Option[LoggedInUser]] =
    LoginInfoStorage.tokenStorage.bimap[Option[LoggedInUser]](token =>
      Try(
        Await
          .result(
            basicRequest
              .post(uri"/api/user/whoami")
              .send(backend),
            5.seconds
          )
          .body
          .fold(
            error =>
              throw new IllegalArgumentException(s"Invalid Token $error"),
            response =>
              parser
                .decode[WhoAmIResponse](response)
                .fold(
                  error => throw error,
                  whoami => LoggedInUser(token, whoami.user)
                )
          )
      ).fold(
        throwable => {
          println(throwable.getMessage)
          None
        },
        loggedInUser => Some(loggedInUser)
      )
    )(loggedInUser => loggedInUser.fold("")(_.token))
}
