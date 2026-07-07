package io.sommers.twodee.web.simplydoom.model

import doobie.Put
import doobie.util.Get
import io.circe.{Decoder, Encoder}

trait Permission {
  def isValid(value: String): Boolean

  def encode: String
}

object Permission {
  def apply(values: String): Permission = values match {
    case value if value.isEmpty               => NonePermission
    case value if value.equalsIgnoreCase("*") => AllowAllPermission
    case value => ListPermissions(value.split(",").map(_.trim).toList)
  }
  
  given Get[Permission] = Get[String].map(Permission(_))
  given Put[Permission] = Put[String].contramap(_.encode)
  
  implicit val encodePermission: Encoder[Permission] = Encoder.encodeString.contramap[Permission](_.encode)
  implicit val decodePermission: Decoder[Permission] = Decoder.decodeString.map(Permission(_))
}

object AllowAllPermission extends Permission {
  override def isValid(value: String): Boolean = true

  override def encode: String = "*"
}

object NonePermission extends Permission {
  override def isValid(value: String): Boolean = false

  override def encode: String = ""
}

case class ListPermissions(permissions: List[String]) extends Permission {
  override def isValid(value: String): Boolean = permissions.contains(value)

  override def encode: String = permissions.mkString(",")
}
