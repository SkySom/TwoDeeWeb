package io.sommers.twodee.web.frontend

import com.raquo.laminar.api.L.*
import org.scalajs.dom

object JsApp {
  def main(args: Array[String]): Unit = {
    lazy val container = dom.document.getElementById("root")

    val eventsVar = Var("")

    val tickStream = EventStream.periodic(10000)
      .flatMapTo(FetchStream.get("/api/doom"))

    lazy val appElement = {
      div(
        cls := "JsApp",
        div(
          cls := "content",
          h1("Hello World")
        ),
        div(
          h2("Also Hello World?")
        ),
        div(
          text <-- tickStream
        )
      )
    }

    render(container, appElement)
  }
}
