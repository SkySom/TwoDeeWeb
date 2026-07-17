package io.sommers.twodee.web.simplydoom.service

import cats.effect.IO
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{
  GoogleAuthorizationCodeFlow,
  GoogleClientSecrets
}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import io.sommers.twodee.web.simplydoom.exception.SheetException
import io.sommers.twodee.web.simplydoom.{DoomConfig, GoogleConfig}

import java.io.{File, FileInputStream, InputStreamReader}
import java.util.Collections
import scala.jdk.CollectionConverters.*
import scala.util.Using

trait SheetsService {
  def getSectionByName(sheet: String, range: String): IO[SheetSection]

  def updateSection(sheetSection: SheetSection, values: List[List[Any]]): IO[SheetSection]
}

object SheetsService {
  def apply(doomConfig: DoomConfig): IO[SheetsService] = GoogleSheetsService(doomConfig.google)
}

case class GoogleSheetsService(
    sheets: Sheets
) extends SheetsService {
  override def getSectionByName(sheet: String, name: String): IO[SheetSection] =
    for {
      valueRange <- IO(
        sheets
          .spreadsheets()
          .values()
          .get(sheet, name)
          .execute()
      )
    } yield SheetSection(
      sheet,
      valueRange
    )

  override def updateSection(
      sheetSection: SheetSection,
      values: List[List[Any]]
  ): IO[SheetSection] = for {
    updatedPlotPoints <- IO.pure(sheetSection.valueRange.setValues(values.map(_.asJava).asJava))
    updatedValues <- IO(
      sheets
        .spreadsheets()
        .values()
        .update(sheetSection.sheet, sheetSection.range, updatedPlotPoints)
        .setIncludeValuesInResponse(true)
        .setValueInputOption("RAW")
        .execute()
    )
  } yield sheetSection.copy(valueRange = updatedValues.getUpdatedData)
}

case class SheetSection(
    sheet: String,
    valueRange: ValueRange
) {

  lazy val range: String = valueRange.getRange

  lazy val rows: List[SheetRow] = valueRange.getValues.asScala.map(row => SheetRow(row.asScala.toList)).toList

  def getCell(row: Int, column: Int): IO[SheetCell] = {
    if (row < rows.size) {
      rows(row).getCell(column)
    } else {
      IO.raiseError(SheetException(s"row $row too big vs ${rows.size}"))
    }
  }
}

case class SheetRow(columns: List[Any]) {
  def getCell(column: Int): IO[SheetCell] = if (column < columns.size) {
    IO.pure(SheetCell(columns(column)))
  } else {
    IO.raiseError(SheetException(s"column $column too big vs ${columns.size}"))
  }

  def tuple2[T1, T2](_1: SheetCell => IO[T1], _2: SheetCell => IO[T2]): IO[Option[(T1, T2)]] = columns match {
    case List(columnA, columnB) => IO.both(_1(SheetCell(columnA)), _2(SheetCell(columnB))).option
    case list => IO.pure(None)
  }

  def length: Int = columns.size
}

case class SheetCell(cellValue: Any) {
  def asInt: IO[Int] = for {
    parsed <- cellValue match {
      case s: String => IO(s.toInt)
      case i: Number => IO.pure(i.intValue())
      case a: Any => IO.raiseError(SheetException(s"${a.toString} don't know how to covert to Int"))
    }
  } yield parsed
  
  def asString: IO[String] = IO.pure(cellValue.toString)
}

object GoogleSheetsService {
  def apply(googleConfig: GoogleConfig): IO[GoogleSheetsService] = for {
    httpTransport <- IO.pure(
      NetHttpTransport
        .Builder()
        .build()
    )
    clientSecrets <- loadClientSecrets(googleConfig)
    flow <- IO.pure(
      new GoogleAuthorizationCodeFlow.Builder(
        httpTransport,
        GsonFactory.getDefaultInstance,
        clientSecrets,
        Collections.singletonList(SheetsScopes.SPREADSHEETS)
      ).setDataStoreFactory(new FileDataStoreFactory(new File(googleConfig.datastorePath)))
        .setAccessType("offline")
        .build
    )
    credentials <- IO(
      new AuthorizationCodeInstalledApp(
        flow,
        new LocalServerReceiver()
      ).authorize("user")
    )
    sheets <- IO.pure(
      new Sheets.Builder(
        httpTransport,
        GsonFactory.getDefaultInstance,
        credentials
      )
        .setApplicationName("Skill Lookup")
        .build
    )
  } yield new GoogleSheetsService(sheets)

  private def loadClientSecrets(
      googleConfig: GoogleConfig
  ): IO[GoogleClientSecrets] = for {
    clientSecrets <- IO.fromTry(Using.Manager { use =>
      val inputStream = use(new FileInputStream(new File(googleConfig.credentialPath)))
      val streamReader = use(new InputStreamReader(inputStream))
      GoogleClientSecrets.load(GsonFactory.getDefaultInstance, streamReader)
    })
  } yield clientSecrets
}
