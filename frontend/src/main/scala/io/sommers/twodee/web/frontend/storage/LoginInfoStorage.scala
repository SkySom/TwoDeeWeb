package io.sommers.twodee.web.frontend.storage

import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.state.Var
import com.raquo.airstream.web.WebStorageVar
import io.circe.generic.auto.deriveDecoder
import io.circe.parser
import io.sommers.twodee.web.model.response.WhoAmIResponse
import io.sommers.twodee.web.model.user.LoggedInUser
import sttp.client4.{Response, UriContext, WebSocketBackend, basicRequest}

import scala.concurrent.Future
import scala.util.Try
object LoginInfoStorage {
  val tokenStorage: WebStorageVar[String] = WebStorageVar
    .localStorage(key = "auth_token", syncOwner = None)
    .text(default = "")

  def mount(owner: Owner): Unit = {
    tokenStorage.syncFromExternalUpdates(owner)
  }
}
