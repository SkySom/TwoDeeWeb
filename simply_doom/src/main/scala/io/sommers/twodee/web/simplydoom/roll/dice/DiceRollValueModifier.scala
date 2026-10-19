package io.sommers.twodee.web.simplydoom.roll.dice

import io.sommers.twodee.web.simplydoom.roll.value.DiceRollValue

trait DiceRollValueModifier {
  def letter: Char
  def modify(rollValues: Seq[DiceRollValue]): Seq[DiceRollValue]
}

object DiceRollValueModifier {
  val all: Seq[DiceRollValueModifier] = Seq(
    AssuredDiceRollValueModifier,
    ReducedDiceRollValueModifier
  )

  val letters: String = all.mkString
}

object AssuredDiceRollValueModifier extends DiceRollValueModifier {
  override def letter: Char = 'a'
  override def modify(rollValues: Seq[DiceRollValue]): Seq[DiceRollValue] =
    rollValues.map { rollValue =>
      rollValue.copy(
        value = rollValue.maxValue,
        toFormula = s"a${rollValue.toFormula}"
      )
    }
}

object ReducedDiceRollValueModifier extends DiceRollValueModifier {
  override def letter: Char = 'r'
  
  override def modify(rollValues: Seq[DiceRollValue]): Seq[DiceRollValue] =
    rollValues.map { rollValue =>
      rollValue.copy(
        value = 1,
        toFormula = s"a${rollValue.toFormula}"
      )
    }
}
