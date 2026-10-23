package io.sommers.twodee.web.simplydoom.roll.cost

import io.circe.Encoder

case class RollCost(
    resource: RollResource,
    amount: Int
) derives Encoder.AsObject {}
