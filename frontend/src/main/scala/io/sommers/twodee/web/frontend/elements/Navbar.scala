package io.sommers.twodee.web.frontend.elements

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.sommers.twodee.web.model.user.{LoggedInUser, User}
import org.scalajs.dom.HTMLElement

object Navbar {
  def apply(userVar: Var[Option[LoggedInUser]]): ReactiveHtmlElement[HTMLElement] = navTag(
    cls := "navbar navbar-expand-lg navbar-dark bg-dark",
    div(
      cls := "container-fluid",
      a(
        cls := "navbar-brand",
        href := "#",
        "TwoDee"
      ),
      ul(
        cls := "navbar-nav ms-auto mb-lg-0 profile-menu",
        li(
          cls <-- userVar.signal.map(_.fold("nav-item")(_ => "nav-item dropdown")),
          a(
            cls <-- userVar.signal.map(_.fold("nav-link")(_ => "nav-link dropdown-toggle")),
            div(
              cls := "profile-pic",
              img(
                src <-- userVar.signal.map(_.flatMap(_.image).getOrElse("/user_icon.png")),
                alt := "Profile Picture"
              )
            )
          )
        )
      )
    )
  )
}
