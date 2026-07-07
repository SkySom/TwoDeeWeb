package io.sommers.twodee.web.simplydoom.model

import java.time.Instant

case class Token(
  jwt: Option[String],
  id: Long,
  name: String,
  user: User,
  active: Boolean,
  createdAt: Instant
)

case class TokenRequest(
    name: String,
    userId: Long
)
