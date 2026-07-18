package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.DoomConfig
import io.sommers.twodee.web.simplydoom.service.ServiceProvider

trait LogicProvider {
  def characterLogic: CharacterLogic
  def doomPoolLogic: DoomPoolLogic
  def tokenLogic: TokenLogic
  def userLogic: UserLogic
}

object LogicProvider {
  def apply(serviceProvider: ServiceProvider, doomConfig: DoomConfig): IO[LogicProvider] = for {
    characterLogic <- CharacterLogic(
      serviceProvider.characterService,
      serviceProvider.sheetsService
    )
    doomPoolLogic <- DoomPoolLogic(serviceProvider.doomPoolService)
    userLogic <- UserLogic(serviceProvider.userService)
    tokenLogic <- TokenLogic(doomConfig.auth, userLogic, serviceProvider.tokenService)
  } yield LogicProviderImpl(
    characterLogic,
    doomPoolLogic,
    tokenLogic,
    userLogic
  )
}

private case class LogicProviderImpl(
    characterLogic: CharacterLogic,
    doomPoolLogic: DoomPoolLogic,
    tokenLogic: TokenLogic,
    userLogic: UserLogic
) extends LogicProvider
