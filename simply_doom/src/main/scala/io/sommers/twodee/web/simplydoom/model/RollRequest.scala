package io.sommers.twodee.web.simplydoom.model

import io.sommers.twodee.web.simplydoom.roll.{RollData, RollOptions}

case class RollRequest(
    formula: String,
    options: RollOptions = RollOptions(),
    characterId: Option[Long] = None,
    doomPoolId: Option[Long] = None
) {}
