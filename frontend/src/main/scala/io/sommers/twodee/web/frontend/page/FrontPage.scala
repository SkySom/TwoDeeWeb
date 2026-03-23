package io.sommers.twodee.web.frontend.page

import com.raquo.waypoint.*
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLDivElement

case object FrontPage extends Page("TwoDee Web") {
  val route: Route.Total[FrontPage.type, Unit] = Route.static(FrontPage, root / endOfSegments)
  
  def render: ReactiveHtmlElement[HTMLDivElement] = div(
    cls := "d-flex justify-content-center align-items-center",
    height := "90vh",
    div(
      cls := "container d-flex flex-column justify-content-center align-items-center",
      borderStyle := "solid",
      height := "50vh",
      h1(
        cls := "text-center",
        "Welcome to TwoDee Web"
      )
    )
  )
}
