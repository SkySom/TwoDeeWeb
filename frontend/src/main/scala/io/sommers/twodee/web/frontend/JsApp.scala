package io.sommers.twodee.web.frontend

import com.raquo.laminar.api.L.*
import io.sommers.twodee.web.frontend.TwoDeeHtmlKeys.onSuccess
import org.scalajs.dom

object JsApp {
  def main(args: Array[String]): Unit = {
    lazy val container = dom.document.getElementById("root")

    println("-- Scala.js app start --")

    val googleStatus = Var("Google: Not Ready")

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
          cls := "g-signin2",
          onSuccess := "onSignIn"
        ),
        div(
          text <-- googleStatus
        )
      )
    }

    render(container, appElement)
  }
}
