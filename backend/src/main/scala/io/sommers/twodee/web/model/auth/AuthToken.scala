package io.sommers.twodee.web.model.auth

import doobie.{Get, Put}

trait AuthToken {
  val id: Long
  
  val kind: AuthTokenKind
}

enum AuthTokenKind {
  case USER
  case BOT
  case APP
}

object AuthTokenKind:
  given Get[AuthTokenKind] = Get.deriveEnumString[AuthTokenKind]
  given Put[AuthTokenKind] = Put.deriveEnumString[AuthTokenKind]