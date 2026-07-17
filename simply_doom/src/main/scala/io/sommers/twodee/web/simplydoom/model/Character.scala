package io.sommers.twodee.web.simplydoom.model

case class Character(
    id: Long,
    name: String,
    sheet: String,
    ownerId: Long,
    plotPoints: Int,
    skills: Map[String, String]
) {}

object Character {
  def apply(character: (Long, String, String, Long)): Character =
    Character(character._1, character._2, character._3, character._4, 0, Map());
}

case class CharacterRow(
    id: Long,
    name: String,
    sheet: String,
    ownerId: Long
) {
  def toCharacter(plotPoints: Int, skills: Map[String, String]): Character =
    Character(id, name, sheet, ownerId, plotPoints, skills)
}

case class CharacterCreateRequest(
    name: String,
    sheet: String,
    ownerId: Option[Long]
)
