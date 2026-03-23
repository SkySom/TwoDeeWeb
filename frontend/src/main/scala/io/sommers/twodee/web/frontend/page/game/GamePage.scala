package io.sommers.twodee.web.frontend.page.game

import com.raquo.airstream.core.Signal
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.waypoint.{Route, SplitRender}
import io.sommers.twodee.web.frontend.page.Page
import org.scalajs.dom.HTMLElement
import org.scalajs.dom.html.Div

abstract class GamePage(override val title: String) extends Page(title) {

}

object GamePage {
  
  val routes: List[Route[_ <: GamePage, _]] = List(
    CreateGamePage.route
  )
  
  def renderGamePage(gamePageSignal: Signal[GamePage]): ReactiveHtmlElement[Div] = {
    val gamePageSplitter = SplitRender[GamePage, ReactiveHtmlElement[_]](gamePageSignal)
      .collectStatic(CreateGamePage) { CreateGamePage.render }
    
    div(
      idAttr := "game"
    )
  }
}
