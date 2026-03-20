package io.sommers.twodee.web.logic

import cats.effect.IO
import com.google.api.client.googleapis.apache.v2.GoogleApacheHttpTransport
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import io.sommers.twodee.web.config.GoogleOAuthConfig
import io.sommers.twodee.web.exception.InvalidTokenException

import java.util.Collections
import scala.util.Try

trait GoogleLogic {
  def validateToken(token: String): IO[GoogleToken]
}

object GoogleLogic {
  def apply(googleOAuthConfig: GoogleOAuthConfig): GoogleLogicImpl =
    GoogleLogicImpl(googleOAuthConfig)
}

case class GoogleToken(
    sub: String,
    image: Option[String]
)

case class GoogleLogicImpl(
    googleOAuthConfig: GoogleOAuthConfig
) extends GoogleLogic {
  private val httpTransport: HttpTransport =
    GoogleApacheHttpTransport.newTrustedTransport()
  private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance
  private val tokenVerifier: GoogleIdTokenVerifier =
    new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
      .setAudience(Collections.singleton(googleOAuthConfig.clientId))
      .build()

  override def validateToken(token: String): IO[GoogleToken] = {
    IO.fromTry(Try {
      val googleIdToken = tokenVerifier.verify(token)

      if (googleIdToken != null) {
        val payload = googleIdToken.getPayload
        GoogleToken(
          payload.getSubject,
          Option(payload.get("picture"))
            .map(_.toString)
        )
      } else {
        throw InvalidTokenException("Invalid Google Token")
      }
    })
  }
}
