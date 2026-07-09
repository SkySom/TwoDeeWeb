package io.sommers.twodee.web.simplydoom.service

import cats.effect.IO
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import io.sommers.twodee.web.simplydoom.exception.SheetException
import io.sommers.twodee.web.simplydoom.{DoomConfig, GoogleConfig}

import java.io.{File, FileInputStream, InputStreamReader}
import java.util.Collections
import scala.jdk.CollectionConverters.*
import scala.util.Using

trait SheetsService {
  def getSectionByName(sheet: String, range: String): IO[SheetSection]
}

object SheetsService {
  def apply(doomConfig: DoomConfig): IO[SheetsService] = GoogleSheetsService(doomConfig.google)
}

case class GoogleSheetsService(
    sheets: Sheets
) extends SheetsService {
  override def getSectionByName(sheet: String, name: String): IO[SheetSection] =
    for {
      range <- IO(
        sheets
          .spreadsheets()
          .values()
          .get(sheet, name)
          .execute()
          .getValues
      )
    } yield SheetSection(
      range.asScala
        .map(_.toArray)
        .toArray
    )
}

case class SheetSection(
    arrays: Array[Array[Any]]
) {
  def getInt(xPos: Int, yPos: Int): IO[Int] = for {
    value <- getValue(xPos, yPos)
    parsed <- value match {
      case s: String => IO(s.toInt)
      case i: Number => IO.pure(i.intValue())
      case a: Any => IO.raiseError(SheetException(s"${a.toString} don't know how to covert to Int"))
    }
  } yield parsed

  private def getValue(xPos: Int, yPos: Int): IO[Any] = {
    if (xPos < arrays.length) {
      val array = arrays(xPos)
      if (yPos < array.length) {
        IO.pure(array(yPos))
      } else {
        IO.raiseError(SheetException(s"yPos $yPos too big vs ${array.length}"))
      }
    } else {
      IO.raiseError(SheetException(s"xPos $xPos too big vs ${arrays.length}"))
    }
  }
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
