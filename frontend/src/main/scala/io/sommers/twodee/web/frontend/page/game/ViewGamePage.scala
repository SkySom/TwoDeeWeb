package io.sommers.twodee.web.frontend.page.game

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.*

sealed case class ViewGamePage(gameId: Long) extends GamePage("View Game") {
  
}

object ViewGamePage {
  val route: Route.Total[ViewGamePage, Long] = Route[ViewGamePage, Long](
    encode = viewPage => viewPage.gameId,
    decode = arg => ViewGamePage(arg),
    pattern = root / "game" / segment[Long]
  )

  def render(signal: Signal[ViewGamePage]): HtmlElement = div(
    h1(
      value <-- signal.map(gamePage => s"Game Id: ${gamePage.gameId}")
    )
  )
}
