package io.sommers.twodee.web.frontend.elements

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.circe.generic.auto.*
import io.circe.parser.decode
import io.circe.syntax.*
import io.sommers.twodee.web.frontend.model.LoggedInUser
import io.sommers.twodee.web.model.GoogleInfo
import io.sommers.twodee.web.model.request.LoginRequest
import io.sommers.twodee.web.model.response.LoginResponse
import io.sommers.twodee.web.model.user.User
import org.scalajs.dom.{BodyInit, HTMLDivElement, Response}
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*
import sttp.client4.*
import sttp.client4.circe.asJson
import sttp.client4.fetch.FetchBackend
import typings.googleAccounts.global.google
import typings.googleAccounts.google.accounts.id.{
  CredentialResponse,
  GsiButtonConfiguration,
  IdConfiguration
}
import typings.googleAccounts.googleAccountsStrings

import scala.concurrent.Future
import scala.scalajs.js.JSON

object LoginElement {
  def getLoginElement()(implicit
      backend: WebSocketBackend[Future]
  ): ReactiveHtmlElement[HTMLDivElement] = {
    implicit val buttonDiv: ReactiveHtmlElement[HTMLDivElement] = div(
      idAttr := "sign_in_button"
    )

    div(
      cls := "d-flex justify-content-center align-items-center",
      height := "90vh",
      div(
        cls := "container d-flex flex-column justify-content-center align-items-center",
        borderStyle := "solid",
        height := "50vh",
        h1(
          cls := "text-center",
          "Login to Continue"
        ),
        buttonDiv,
        div(
          child <-- FetchStream
            .get("/api/google/info")
            .map(info => decode[GoogleInfo](info))
            .map(handleGoogleInfo)
            .recover { case throwable: Throwable =>
              Some(
                div(
                  s"Found Error $throwable"
                )
              )
            }
        )
      )
    )
  }

  private def handleGoogleInfo(
      googleInfo: Either[io.circe.Error, GoogleInfo]
  )(implicit
      buttonDiv: ReactiveHtmlElement[HTMLDivElement],
      backend: WebSocketBackend[Future]
  ) = {
    googleInfo.fold(
      error =>
        div(
          s"Error found $error"
        ),
      info =>
        val configuration = IdConfiguration(info.clientId)
        configuration.callback = (response: CredentialResponse) => {
          println(JSON.stringify(response))
          basicRequest
            .body(asJson(LoginRequest(response.credential)))
            .post(uri"/api/google/login")
            .send(backend)
            .foreach(loginResponse => {
              loginResponse.body.fold(
                left => println(s"Left $left"),
                right => {
                  decode[LoginResponse](right).fold(
                    error => println(error.getMessage),
                    loginResponse => {
                      LoggedInUser.storageVar.set(Some(LoggedInUser(loginResponse.token, loginResponse.user)))
                    }
                  )
                }
              )
            })
        }
        google.accounts.id.initialize(configuration)
        google.accounts.id.renderButton(
          buttonDiv.ref,
          GsiButtonConfiguration(googleAccountsStrings.standard)
        )
        div(
          idAttr := "google_loaded"
        )
    )
  }

  private def encodeLogin(login: LoginRequest): BodyInit = {
    login.asJson.noSpaces
  }

  private def decodeLogin(response: Response): EventStream[LoginResponse] = {
    if (response.ok) {
      println(response)
      EventStream
        .fromJsPromise[String](response.text())
        .flatMapSwitch((text: String) =>
          println(text)
          EventStream.fromEither(decode[LoginResponse](text), true)
        )
    } else {
      if (response.body != null) {
        EventStream
          .fromJsPromise(response.text())
          .flatMapSwitch(body =>
            EventStream.fromEither(
              Left(
                new IllegalStateException(
                  s"Received ${response.status} with body $body"
                )
              )
            )
          )
      } else {
        EventStream.fromEither(
          Left(
            new IllegalStateException(
              s"Received ${response.status} but no body"
            )
          )
        )
      }
    }
  }

  private case class Login(
      created: Boolean,
      user: User
  )
}
