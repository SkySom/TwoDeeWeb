package io.sommers.twodee.web.simplydoom.model

case class DoomPool(
    id: Long,
    name: String,
    doom: Int
)

case class DoomPoolRequest(
    name: String,
    doom: Option[Int]
)

case class DoomUpdateRequest(
    amount: Int
)

case class DoomUpdateResponse(
  old: Int,
  current: Int
)