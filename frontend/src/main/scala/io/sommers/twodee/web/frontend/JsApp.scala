package io.sommers.twodee.web.frontend

import com.raquo.laminar.api.L.*
import io.sommers.twodee.web.frontend.elements.Navbar
import io.sommers.twodee.web.frontend.page.Page
import org.scalajs.dom

object JsApp {
  def main(args: Array[String]): Unit = {

    renderApp()
  }

  private def renderApp(): Unit = {
    lazy val container = dom.document.getElementById("root")

    lazy val appElement = {
      div(
        cls := "JsApp",
        Navbar(),
        div(
          child <-- Page.pageSplitter.signal
        )
      )
    }

    render(container, appElement)
  }
}
