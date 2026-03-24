package io.sommers.twodee.web.logic

import cats.effect.IO
import io.sommers.twodee.web.model.game.Game
import io.sommers.twodee.web.service.GameService

trait GameLogic {
  def createGame(name: String, createdBy: Long): IO[Game]
  
  def getGameById(id: Long): IO[Option[Game]]
}

object GameLogic {
  def apply(gameService: GameService): GameLogic = GameLogicImpl(gameService)
}

case class GameLogicImpl(
    gameService: GameService
) extends GameLogic {

  override def createGame(name: String, createdBy: Long): IO[Game] = gameService.createGame(name, createdBy)
    .map(id => Game(id, name, createdBy))

  override def getGameById(id: Long): IO[Option[Game]] = gameService.getGameById(id)
    .map(_.map(Game.apply))
}
