package io.sommers.twodee.web.frontend.page.game

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.*
import io.circe.generic.auto.*
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.sommers.twodee.web.frontend.model.LoggedInUser
import io.sommers.twodee.web.frontend.page.PageRouter
import io.sommers.twodee.web.frontend.util.BootstrapProps.labelFor
import io.sommers.twodee.web.model.game.Game
import io.sommers.twodee.web.model.request.game.{CreateGameRequest, CreateGameResponse}

import scala.collection.mutable

case object CreateGamePage extends GamePage("Create new game") {
  val route: Route.Total[CreateGamePage.type, Unit] =
    Route.static(CreateGamePage, root / "game" / "create")

  def render: HtmlElement = {
    val formStateVar: Var[GameFormState] = Var(GameFormState())

    val nameVar =
      formStateVar.zoomLazy(_.name)((state, name) => state.copy(name = name))

    val errorsSignal: Signal[List[String]] = formStateVar.signal.map(_.errors)

    val observer: Observer[Game] = Observer.apply(game => PageRouter.pushState(ViewGamePage(game.id)))



    div(
      cls := "d-flex justify-content-center align-items-center",
      height := "90vh",
      div(
        cls := "container align-content-center justify-content-center align-items-center",
        borderStyle := "solid",
        height := "50vh",
        h1(
          cls := "text-center",
          "Create new Game"
        ),
        form(
          div(
            cls := "form-group form-floating has-validation mb-3",
            input(
              cls <-- errorsSignal.map(errors =>
                if (!errors.contains("name")) "form-control"
                else "form-control is-invalid"
              ),
              idAttr := "gameName",
              placeholder := "New Game",
              required := true,
              typ := "text",
              aria.describedBy := "validationNameFeedback",
              controlled(
                value <-- nameVar,
                onInput.mapToValue --> nameVar
              )
            ),
            label(
              labelFor := "gameName",
              "Name"
            ),
            div(
              cls := "invalid-feedback",
              idAttr := "validationNameFeedback",
              "Name cannot be empty"
            )
          ),
          button(
            cls := "btn btn-primary",
            typ := "submit",
            disabled <-- errorsSignal.map(_.nonEmpty),
            "Create Game"
          ),
          onSubmit.preventDefault.flatMapStream(_ => {
            val userOpt: Option[LoggedInUser] = LoggedInUser.storageVar.signal
              .now()

            userOpt.map(user =>
              FetchStream
                .withCodec[CreateGameRequest, CreateGameResponse](
                  _.asJson.noSpaces,
                  response =>
                    EventStream
                      .fromJsPromise(response.text())
                      .flatMapSwitch(text =>
                        EventStream
                          .fromEither(decode[CreateGameResponse](text))
                      )
                )
                .post(
                  "/api/game",
                  _.headers(("Authorization", s"Bearer ${user.token}")),
                  _.body(CreateGameRequest(formStateVar.signal.now().name))
                )
                .map(_.game)
            ).getOrElse(EventStream.empty)
          }) --> observer
        )
      )
    )
  }
}

case class GameFormState(
    name: String = ""
) {
  lazy val errors: List[String] = {
    val list = new mutable.ArrayBuffer[String]()

    if (name.isEmpty) {
      list += "name"
    }

    list.toList
  }
}
