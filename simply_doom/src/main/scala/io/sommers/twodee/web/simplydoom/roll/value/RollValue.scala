package io.sommers.twodee.web.simplydoom.roll.value

import io.sommers.twodee.web.simplydoom.roll.cost.RollResourceCost

trait RollValue {
  def category: RollValueCategory
  
  def value: Int
  
  def maxValue: Int
  
  def toFormula: String
  
  def rollCost: Option[RollResourceCost]
}
