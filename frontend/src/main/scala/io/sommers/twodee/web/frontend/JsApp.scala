package io.sommers.twodee.web.frontend

import com.raquo.laminar.api.L.*
import io.circe.generic.auto.*
import io.circe.parser
import io.sommers.twodee.web.frontend.elements.Navbar
import io.sommers.twodee.web.frontend.login.LoginElement
import io.sommers.twodee.web.frontend.storage.LoginInfoStorage
import io.sommers.twodee.web.model.response.WhoAmIResponse
import io.sommers.twodee.web.model.user.{LoggedInUser, User}
import org.scalajs.dom
import sttp.client4.fetch.FetchBackend
import sttp.client4.{Backend, UriContext, WebSocketBackend, basicRequest}

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
import scala.scalajs.js.await
import scala.util.Try

object JsApp {
  def main(args: Array[String]): Unit = {

    implicit val backend: WebSocketBackend[Future] = FetchBackend()

    implicit val loggedInUser: Var[Option[LoggedInUser]] =
      LoginInfoStorage.getLoggedInUserVar()

    renderApp()
  }

  private def renderApp()(implicit
      maybeUser: Var[Option[LoggedInUser]],
      backend: WebSocketBackend[Future]
  ): Unit = {
    lazy val container = dom.document.getElementById("root")

    lazy val appElement = {
      div(
        onMountCallback { ctx => LoginInfoStorage.mount(ctx.owner) },
        cls := "JsApp",
        Navbar(maybeUser),
        div(
          child <-- maybeUser.signal.map(
            _.fold(LoginElement.getLoginElement())(_.username)
          )
        )
      )
    }

    render(container, appElement)
  }
}
