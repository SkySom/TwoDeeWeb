package io.sommers.twodee.web.simplydoom.roll

import io.sommers.twodee.web.simplydoom.model.{Character, DoomPool}

case class RollData(
    depth: Int,
    character: Option[Character]
)
