package io.sommers.twodee.web.model.game

case class Party (
    id: Long,
    name: String,
    gameId: Long,
    createdBy: Long
)
