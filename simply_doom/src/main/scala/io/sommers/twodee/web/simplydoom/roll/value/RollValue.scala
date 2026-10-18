package io.sommers.twodee.web.simplydoom.roll.value

import io.sommers.twodee.web.simplydoom.roll.cost.RollCost

trait RollValue {
  def category: RollValueCategory
  
  def value: Int
  
  def toFormula: String
  
  def rollCost: Option[RollCost]
}
