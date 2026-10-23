package io.sommers.twodee.web.simplydoom.logic

import cats.effect.IO
import io.sommers.twodee.web.simplydoom.DoomConfig
import io.sommers.twodee.web.simplydoom.service.ServiceProvider
import org.typelevel.log4cats.LoggerFactory

trait LogicProvider {
  def characterLogic: CharacterLogic
  def doomPoolLogic: DoomPoolLogic
  def rollLogic: RollLogic
  def tokenLogic: TokenLogic
  def userLogic: UserLogic
}

object LogicProvider {
  def apply(serviceProvider: ServiceProvider, doomConfig: DoomConfig)(implicit
      loggerFactory: LoggerFactory[IO]
  ): IO[LogicProvider] = for {
    userLogic <- UserLogic(serviceProvider.userService)
    characterLogic <- CharacterLogic(
      serviceProvider.characterService,
      serviceProvider.sheetsService,
      userLogic
    )
    doomPoolLogic <- DoomPoolLogic(serviceProvider.doomPoolService)
    rollLogic <- RollLogic(characterLogic, doomPoolLogic)
    tokenLogic <- TokenLogic(doomConfig.auth, userLogic, serviceProvider.tokenService)
  } yield LogicProviderImpl(
    characterLogic,
    doomPoolLogic,
    rollLogic,
    tokenLogic,
    userLogic
  )
}

private case class LogicProviderImpl(
    characterLogic: CharacterLogic,
    doomPoolLogic: DoomPoolLogic,
    rollLogic: RollLogic,
    tokenLogic: TokenLogic,
    userLogic: UserLogic
) extends LogicProvider
