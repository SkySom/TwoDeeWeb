package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.exception.NotFoundException
import io.sommers.twodee.web.simplydoom.model.{Character, CharacterRow, User}
import io.sommers.twodee.web.simplydoom.service.{CharacterService, SheetsService}

trait CharacterLogic {
  def create(name: String, sheet: String, owner: User): IO[Character]

  def getById(id: Long): IO[Character]
  
  def list(filters: Map[String, String]): IO[List[Character]]
}

object CharacterLogic {
  def apply(characterService: CharacterService, sheetsService: SheetsService): CharacterLogic = 
    CharacterLogicImpl(characterService, sheetsService)
}

case class CharacterLogicImpl(
    characterService: CharacterService,
    sheetsService: SheetsService
) extends CharacterLogic {

  override def create(name: String, sheet: String, owner: User): IO[Character] =
    for {
      id <- characterService.createCharacter(name, sheet, owner.id)
      character <- this.getById(id)
    } yield character

  override def getById(id: Long): IO[Character] = for {
    row <- characterService
      .getCharacter(id)
      .flatMap(
        _.fold[IO[CharacterRow]](IO.raiseError(NotFoundException(s"No character with id $id")))(
          row => IO.pure(row)
        )
      )
    plotPoints <- this.getPlotPoints(row.sheet)
  } yield row.toCharacter(plotPoints)

  private def getPlotPoints(sheet: String): IO[Int] = for {
    sheetSection <- sheetsService.getSectionByName(sheet, "PlotPoints")
    plotPoints <- sheetSection.getInt(0, 0)
  } yield plotPoints

  override def list(filters: Map[String, String]): IO[List[Character]] = IO.pure(List())
}
