package io.sommers.twodee.web.frontend.page.game

import com.raquo.airstream.core.Signal
import com.raquo.laminar.api.L.*
import com.raquo.waypoint.{Route, SplitRender}
import io.sommers.twodee.web.frontend.page.Page

abstract class GamePage(override val title: String) extends Page(title) {}

object GamePage {

  val routes: List[Route[_ <: GamePage, _]] = List(
    CreateGamePage.route,
    ViewGamePage.route
  )

  def renderGamePage(gamePageSignal: Signal[GamePage]): HtmlElement = {
    val gamePageSplitter = SplitRender[GamePage, HtmlElement](gamePageSignal)
      .collectSignal[ViewGamePage] { viewGamePageSignal => ViewGamePage.render(viewGamePageSignal) }
      .collectStatic(CreateGamePage) { CreateGamePage.render }

    div(
      idAttr := "game",
      child <-- gamePageSplitter.signal
    )
  }
}
