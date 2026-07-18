package io.sommers.twodee.web.simplydoom.service

import cats.effect.IO
import doobie.util.transactor.Transactor
import io.sommers.twodee.web.simplydoom.DoomConfig

trait ServiceProvider {
  def characterService: CharacterService
  def doomPoolService: DoomPoolService
  def sheetsService: SheetsService
  def tokenService: TokenService
  def userService: UserService
}

object ServiceProvider {
  def apply(transactor: Transactor[IO], doomConfig: DoomConfig): IO[ServiceProvider] = for {
    characterService <- CharacterService(transactor)
    doomPoolService <- DoomPoolService(transactor)
    sheetsService <- SheetsService(doomConfig)
    tokenService <- TokenService(transactor)
    userService <- UserService(transactor)
  } yield ServiceProviderImpl(
    characterService, 
    doomPoolService, 
    sheetsService, 
    tokenService, 
    userService
  )
}

private case class ServiceProviderImpl(
    characterService: CharacterService,
    doomPoolService: DoomPoolService,
    sheetsService: SheetsService,
    tokenService: TokenService,
    userService: UserService
) extends ServiceProvider
