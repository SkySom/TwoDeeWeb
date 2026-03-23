package io.sommers.twodee.web.frontend.model

import com.raquo.airstream.web.WebStorageVar
import com.raquo.laminar.api.L.unsafeWindowOwner
import io.circe.generic.auto.*
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.sommers.twodee.web.model.user.User

import scala.util.{Success, Try}

case class LoggedInUser(
    token: String,
    override val id: Long,
    override val username: String,
    override val image: Option[String],
    override val authId: String
) extends User

object LoggedInUser {
  def apply(token: String, user: User): LoggedInUser = {
    new LoggedInUser(
      token,
      user.id,
      user.username,
      user.image,
      user.authId
    )
  }

  val storageVar: WebStorageVar[Option[LoggedInUser]] = WebStorageVar
    .localStorage(key = "logged_in_user", syncOwner = Some(unsafeWindowOwner))
    .withCodec(
      encode = userOpt =>
        userOpt
          .map(_.asJson.noSpaces)
          .getOrElse(""),
      decode = string =>
        Try {
          if (string.nonEmpty) {
            decode[LoggedInUser](string)
              .fold(error => throw error, user => Some(user))
          } else {
            None
          }
        },
      default = Success(None)
    )
}
