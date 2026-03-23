package io.sommers.twodee.web.frontend.page

import com.raquo.waypoint.Router
import io.sommers.twodee.web.frontend.page.game.GamePage

object PageRouter extends Router[Page](
  routes = List(FrontPage.route, LoginPage.route) ++ GamePage.routes,
  getPageTitle = _.title,
  serializePage = page => "",
  deserializePage = pageStr => LoginPage
)
