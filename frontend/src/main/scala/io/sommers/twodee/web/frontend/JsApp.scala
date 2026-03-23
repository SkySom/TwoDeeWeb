package io.sommers.twodee.web.frontend

import com.raquo.laminar.api.L.*
import io.sommers.twodee.web.frontend.elements.{LoginElement, Navbar}
import io.sommers.twodee.web.frontend.model.LoggedInUser
import org.scalajs.dom
import sttp.client4.WebSocketBackend
import sttp.client4.fetch.FetchBackend

import scala.concurrent.Future

object JsApp {
  def main(args: Array[String]): Unit = {

    implicit val backend: WebSocketBackend[Future] = FetchBackend()

    renderApp()
  }

  private def renderApp()(implicit
      backend: WebSocketBackend[Future]
  ): Unit = {
    lazy val container = dom.document.getElementById("root")

    lazy val appElement = {
      div(
        cls := "JsApp",
        Navbar(),
        div(
          child <-- LoggedInUser.storageVar.signal.map(
            _.fold(LoginElement.getLoginElement())(_.username)
          )
        )
      )
    }

    render(container, appElement)
  }
}
