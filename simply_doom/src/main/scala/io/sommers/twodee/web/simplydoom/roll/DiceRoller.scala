package io.sommers.twodee.web.simplydoom.roll

import cats.effect.IO
import cats.effect.std.Random

trait DiceRoller {
  def roll(faces: Int): IO[Int]
}

case class StaticDiceRoller(
    result: Int
) extends DiceRoller {
  override def roll(faces: Int): IO[Int] = IO.pure(Math.max(1, Math.min(result, faces)))
}

case class RandomDiceRoller(
    random: Random[IO]
) extends DiceRoller {
  override def roll(faces: Int): IO[Int] = for {
    roll <- random.nextIntBounded(faces)
  } yield roll + 1
}

object RandomDiceRoller {
  def apply(): IO[RandomDiceRoller] = for {
    random <- Random.scalaUtilRandom[IO]
  } yield RandomDiceRoller(random)
}
