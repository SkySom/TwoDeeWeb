package io.sommers.twodee.web.simplydoom.roll.dice

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.roll.StaticDiceRoller
import io.sommers.twodee.web.simplydoom.roll.cost.RollCost
import io.sommers.twodee.web.simplydoom.roll.cost.RollResource.PlotPoints
import io.sommers.twodee.web.simplydoom.roll.dice.PlotDiceRollValueCreatorSuite.{exists, expect}
import io.sommers.twodee.web.simplydoom.roll.value.DiceRollValue
import weaver.SimpleIOSuite

object PlotDiceRollValueCategoryTestSuite extends SimpleIOSuite  {
  test("A single plot dice less than half should return half") {
    for {
      dice <- PlotDiceRollValueCreator.create(10, StaticDiceRoller(1))
      selected <- IO.pure(PlotDiceRollValueCategory.pickValues(Seq(dice)))
    } yield expect(selected.size == 1) and exists(selected) { diceRollValue =>
      expect(diceRollValue.value == 5)
      expect(diceRollValue.toFormula == "pd10")
    }
  }

  test("A single plot dice greater than half should return value") {
    for {
      dice <- PlotDiceRollValueCreator.create(10, StaticDiceRoller(8))
      selected <- IO.pure(PlotDiceRollValueCategory.pickValues(Seq(dice)))
    } yield expect(selected.size == 1) and exists(selected) { diceRollValue =>
      expect(diceRollValue.value == 8)
      expect(diceRollValue.toFormula == "pd10")
    }
  }

  test("A single plot dice less than half should return half") {
    for {
      dice <- PlotDiceRollValueCreator.create(10, StaticDiceRoller(1))
      selected <- IO.pure(PlotDiceRollValueCategory.pickValues(Seq(dice)))
    } yield expect(selected.size == 1) and exists(selected) { diceRollValue =>
      expect(diceRollValue.value == 5)
      expect(diceRollValue.toFormula == "pd10")
    }
  }

  test("Multiple plot die should return their value always") {
    for {
      dice <- PlotDiceRollValueCreator.create(10, StaticDiceRoller(2))
      dice2 <- PlotDiceRollValueCreator.create(10, StaticDiceRoller(2))
      selected <- IO.pure(PlotDiceRollValueCategory.pickValues(Seq(dice, dice2)))
    } yield expect(selected.size == 2) and exists(selected) { diceRollValue =>
      expect(diceRollValue.value == 2)
      expect(diceRollValue.toFormula == "pd10")
    }
  }
}

object PlotDiceRollValueCreatorSuite extends SimpleIOSuite {
  test("A plot dice should cost half its value in plot points") {
    for {
      dice <- PlotDiceRollValueCreator.create(10, StaticDiceRoller(1))
    } yield exists(dice.rollCost)(rollCost => expect.same(RollCost(PlotPoints, 5), rollCost))
  }

  test("A plot dice should have formula pd<faces>") {
    for {
      dice <- PlotDiceRollValueCreator.create(10, StaticDiceRoller(1))
    } yield expect.eql("pd10", dice.toFormula)
  }
}

object EnhancedDiceRollValueCreatorSuite extends SimpleIOSuite {
  test("An enhanced dice should not cost anything") {
    for {
      dice <- EnhancedDiceRollValueCreator.create(10, StaticDiceRoller(1))
    } yield expect.same(None, dice.rollCost)
  }

  test("An enhanced dice should not cost anything") {
    for {
      dice <- EnhancedDiceRollValueCreator.create(10, StaticDiceRoller(1))
    } yield expect.eql("ed10", dice.toFormula)
  }
}
