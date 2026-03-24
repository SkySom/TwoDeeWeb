package io.sommers.twodee.web.frontend.page

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.waypoint.SplitRender
import io.sommers.twodee.web.frontend.page.FrontPage
import io.sommers.twodee.web.frontend.page.game.GamePage
import org.scalajs.dom
import org.scalajs.dom.HTMLElement
import sttp.client4.WebSocketBackend
import sttp.client4.fetch.FetchBackend

import scala.concurrent.Future

abstract class Page(val title: String)

object Page {
  private implicit val backend: WebSocketBackend[Future] = FetchBackend()

  val pageSplitter: SplitRender[Page, HtmlElement] =
    SplitRender[Page, HtmlElement](PageRouter.currentPageSignal)
      .collectSignal[GamePage](gamePageSignal =>
        GamePage.renderGamePage(gamePageSignal)
      )
      .collectStatic(LoginPage) { LoginPage.render() }
      .collectStatic(FrontPage) { FrontPage.render }
}
