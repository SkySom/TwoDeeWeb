package io.sommers.twodee.web.frontend.page.game

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.waypoint.*
import org.scalajs.dom.HTMLDivElement

case object CreateGamePage extends GamePage("Create new game") {
  val route: Route.Total[CreateGamePage.type, Unit] =
    Route.static(CreateGamePage, root / "game" / "create")

  def render: ReactiveHtmlElement[HTMLDivElement] = {
    div(
      idAttr := "create_game"
    )
  }
}
