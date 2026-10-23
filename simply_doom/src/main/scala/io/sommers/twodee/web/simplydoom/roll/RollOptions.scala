package io.sommers.twodee.web.simplydoom.roll

case class RollOptions (
    kept: Int = 2,
    test: Boolean = false
) {
  
}

object RollOptions {
  val default: RollOptions = RollOptions()
  
  def apply(): RollOptions = default
}
