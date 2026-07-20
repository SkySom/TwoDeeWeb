package io.sommers.twodee.web.simplydoom.exception

import cats.effect.IO
import org.http4s.Response
import org.http4s.dsl.io.*


case class MissingPermissionException(message: String)
    extends Exception(message)
