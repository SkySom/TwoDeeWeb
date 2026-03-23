package io.sommers.twodee.web.frontend.elements

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.sommers.twodee.web.frontend.model.LoggedInUser
import io.sommers.twodee.web.frontend.page.{LoginPage, PageRouter}
import io.sommers.twodee.web.frontend.util.BootstrapProps.dataToggle
import org.scalajs.dom.HTMLElement

object Navbar {
  def apply(): ReactiveHtmlElement[HTMLElement] = {

    navTag(
      cls := "navbar navbar-expand-lg navbar-dark bg-dark",
      padding := "0",
      div(
        cls := "container-fluid",
        a(
          cls := "navbar-brand",
          href := "#",
          "TwoDee"
        ),
        ul(
          cls := "nav-item dropdown-menu-end ms-auto mb-lg-0 profile-menu",
          child <-- LoggedInUser.storageVar.signal
            .map(
              _.fold(
                li(
                  cls := "nav-item",
                  a(
                    cls := "nav-item",
                    div(
                      cls := "profile-pic",
                      img(
                        src := "/user_icon.png",
                        alt := "Profile Picture"
                      )
                    )
                  )
                )
              ) { loggedInUser =>
                li(
                  cls := "nav-item dropdown",
                  a(
                    cls := "nav-link dropdown-toggle",
                    dataToggle := "dropdown",
                    div(
                      cls := "profile-pic",
                      img(
                        src := loggedInUser.image.getOrElse("/user_icon.png"),
                        alt := "Profile Picture"
                      )
                    ),
                    href := "#",
                    role := "button",
                    aria.expanded := false
                  ),
                  ul(
                    cls := "dropdown-menu",
                    li(
                      a(
                        cls := "dropdown-item",
                        i(
                          cls := "fas fa-sign-out-alt fa-fw"
                        ),
                        onClick.preventDefault --> (_ => {
                          LoggedInUser.storageVar.set(None)
                          PageRouter.pushState(LoginPage)
                        }),
                        "Log Out"
                      )
                    )
                  )
                )
              }
            )
        )
      )
    )
  }
}
