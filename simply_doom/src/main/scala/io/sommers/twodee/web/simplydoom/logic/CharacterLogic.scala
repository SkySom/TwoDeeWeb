package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import cats.implicits.*
import io.sommers.twodee.web.simplydoom.exception.NotFoundException
import io.sommers.twodee.web.simplydoom.model.{Character, CharacterRow, User}
import io.sommers.twodee.web.simplydoom.service.{CharacterService, SheetSection, SheetsService}

trait CharacterLogic {
  def create(name: String, sheet: String, owner: User): IO[Character]

  def getById(id: Long): IO[Character]

  def list(filters: Map[String, String]): IO[List[Character]]

  def changePlotPoints(id: Long, change: Int): IO[Unit]
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
    sheetAdditions <- IO.both(this.getPlotPoints(row.sheet), this.getSkills(row.sheet))
  } yield row.toCharacter(sheetAdditions._1, sheetAdditions._2)

  override def list(filters: Map[String, String]): IO[List[Character]] = IO.pure(List())

  override def changePlotPoints(id: Long, change: Int): IO[Unit] = for {
    sheet <- characterService
      .getCharacter(id)
      .flatMap(
        _.fold[IO[String]](IO.raiseError(NotFoundException(s"No character with id $id")))(row =>
          IO.pure(row.sheet)
        )
      )
    plotPointsSheetSection <- getPlotPointsSheetSection(sheet)
    _ <- setPlotPoints(plotPointsSheetSection, change)
  } yield ()

  private def setPlotPoints(sheetSection: SheetSection, change: Int): IO[Unit] = for {
    plotPointsCell <- sheetSection.getCell(0, 0)
    current <- plotPointsCell.asInt
    updatedSheetSection <- sheetsService.updateSection(sheetSection, List(List(current + change)))
  } yield ()

  private def getPlotPoints(sheet: String): IO[Int] = for {
    sheetSection <- getPlotPointsSheetSection(sheet)
    plotPointsCell <- sheetSection.getCell(0, 0)
    plotPoints <- plotPointsCell.asInt
  } yield plotPoints

  private def getPlotPointsSheetSection(sheet: String): IO[SheetSection] = for {
    sheetSection <- sheetsService.getSectionByName(sheet, "PlotPoints")
  } yield sheetSection

  private def getSkills(sheetId: String): IO[Map[String, String]] = for {
    sheetSection <- sheetsService.getSectionByName(sheetId, "AllEverything")
    skillRows <- sheetSection.rows
      .map(_.tuple2(_.asString, _.asString))
      .sequence
    skills <- IO.pure(skillRows.flatten.toMap)
  } yield skills
}
