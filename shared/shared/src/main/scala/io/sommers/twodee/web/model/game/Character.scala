package io.sommers.twodee.web.model.game

case class Character(
    id: Long,
    name: String,
    partyId: Long,
    ownerId: Long,
    createdBy: Long
) {

}
