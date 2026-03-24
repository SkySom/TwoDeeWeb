package io.sommers.twodee.web.model.auth

case class TokenInfo (
    id: Long,
    kind: AuthTokenKind,
    active: Boolean,
    createdBy: Long
)
