package io.sommers.twodee.web.util

import cats.effect.IO

object TwoDeeIO {
  def when[T](when: Boolean)(call: => IO[T], default: T): IO[T] = {
    if (when) call else IO.pure(default)
  }
  
  def getOrElse[T, U](opt: Option[T])(map: T => IO[U], default: U): IO[U] = {
    opt.map(map)
      .getOrElse(IO.pure(default))
  }
}
