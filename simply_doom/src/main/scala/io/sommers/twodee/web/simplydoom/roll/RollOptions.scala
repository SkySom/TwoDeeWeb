package io.sommers.twodee.web.simplydoom.roll

case class RollOptions (
    kept: Int
) {
  
}

object RollOptions {
  val default: RollOptions = RollOptions(2)
  
  def apply(): RollOptions = default
}
