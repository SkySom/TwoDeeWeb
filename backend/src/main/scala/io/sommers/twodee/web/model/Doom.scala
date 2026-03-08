package io.sommers.twodee.web.model

case class DoomPoolCreate(
  name: String
)

case class DoomPoolUpdate(
  name: Option[String],
  softLock: Option[String]
) {
  def isChanged(doomPool: DoomPool): Boolean = {
    !name.contains(doomPool.name)
  }
}

case class DoomTransactionCreate(
    amount: Int,
    note: Option[String]
)

case class DoomPool(
  id: Long,
  name: String,
  doom: Int
)