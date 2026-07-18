package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import cats.implicits.*
import io.chrisdavenport.mules.{MemoryCache, TimeSpec}
import io.sommers.twodee.web.simplydoom.exception.NotFoundException
import io.sommers.twodee.web.simplydoom.model.{Character, CharacterRow, User}
import io.sommers.twodee.web.simplydoom.service.{CharacterService, SheetSection, SheetsService}

import scala.concurrent.duration.DurationInt

trait CharacterLogic {
  def create(name: String, sheet: String, owner: User): IO[Character]

  def getById(id: Long, includeSkills: Boolean = false): IO[Character]

  def list(filters: Map[String, String], includeSkills: Boolean = false): IO[List[Character]]

  def changePlotPoints(id: Long, change: Int): IO[Unit]
}

object CharacterLogic {
  def apply(characterService: CharacterService, sheetsService: SheetsService, userLogic: UserLogic): IO[CharacterLogic] =
    for {
      rowCache <- MemoryCache.ofConcurrentHashMap[IO, Long, CharacterRow](
        TimeSpec.fromDuration(8.hour)
      )
      skillCache <- MemoryCache.ofConcurrentHashMap[IO, String, Map[String, String]](
        TimeSpec.fromDuration(8.hour)
      )
    } yield CharacterLogicImpl(characterService, sheetsService, userLogic, rowCache, skillCache)

}

case class CharacterLogicImpl(
    characterService: CharacterService,
    sheetsService: SheetsService,
    userLogic: UserLogic,
    rowCache: MemoryCache[IO, Long, CharacterRow],
    skillCache: MemoryCache[IO, String, Map[String, String]]
) extends CharacterLogic {

  override def create(name: String, sheet: String, owner: User): IO[Character] =
    for {
      id <- characterService.createCharacter(name, sheet, owner.id)
      character <- this.getById(id)
      _ <- userLogic.addCharacterPermission(owner.id, character.id)
    } yield character

  override def getById(id: Long, includeSkills: Boolean = false): IO[Character] = for {
    row <- getCharacterRow(id)
    sheetAdditions <- IO.both(
      this.getPlotPoints(row.sheet),
      if (includeSkills) this.getSkillsCached(row.sheet) else IO.pure(Map())
    )
  } yield row.toCharacter(sheetAdditions._1, sheetAdditions._2)

  override def list(
      filters: Map[String, String],
      includeSkills: Boolean = false
  ): IO[List[Character]] = IO.pure(List())

  override def changePlotPoints(id: Long, change: Int): IO[Unit] = for {
    row <- getCharacterRow(id)
    plotPointsSheetSection <- getPlotPointsSheetSection(row.sheet)
    _ <- setPlotPoints(plotPointsSheetSection, change)
  } yield ()

  private def getCharacterRow(id: Long): IO[CharacterRow] = for {
    cachedRow <- rowCache.lookup(id)
    row <- cachedRow.fold(pullCharacterRowFromDB(id))(IO.pure)
  } yield row

  private def pullCharacterRowFromDB(id: Long): IO[CharacterRow] = for {
    characterRow <- characterService
      .getCharacter(id)
      .flatMap(
        _.fold[IO[CharacterRow]](IO.raiseError(NotFoundException(s"No character with id $id")))(
          row => IO.pure(row)
        )
      )
    _ <- rowCache.insert(id, characterRow)
  } yield characterRow

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

  private def getSkillsCached(sheetId: String): IO[Map[String, String]] = for {
    existing <- skillCache.lookup(sheetId)
    skills <- existing.fold(getSkills(sheetId))(IO.pure)
  } yield skills
}
