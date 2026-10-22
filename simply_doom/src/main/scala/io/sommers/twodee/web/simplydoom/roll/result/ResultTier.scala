package io.sommers.twodee.web.simplydoom.roll.result

import io.sommers.twodee.web.simplydoom.roll.result

enum ResultTier {
  case None
  case Easy
  case Average
  case Hard
  case Formidable
  case Heroic
  case Incredible
  case Ridiculous
  case Impossible
}

object ResultTier {
  def fromTotal(total: Int): ResultTier = total match {
    case value if value < 3 => None
    case value if value < 7 => Easy
    case value if value < 11 => Average
    case value if value < 15 => Hard
    case value if value < 19 => Formidable
    case value if value < 23 => Heroic
    case value if value < 27 => Incredible
    case value if value < 31 => Ridiculous
    case _ => Impossible
  }
}