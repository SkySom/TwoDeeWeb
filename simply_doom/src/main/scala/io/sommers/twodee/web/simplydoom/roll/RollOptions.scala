package io.sommers.twodee.web.simplydoom.roll

case class RollOptions (
    kept: Int
) {
  
}

object RollOptions {
  def apply(): RollOptions = RollOptions(2)
}
