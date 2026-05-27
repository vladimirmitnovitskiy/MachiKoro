package application

import domain.*
import kotlinx.coroutines.flow.StateFlow

// Состояние игры (будет автоматически обновлять GUI во 2-м задании)
data class GameState(
    val activePlayer: Player,
    val players: List<Player>,
    val market: Market,
    val lastDiceRoll: Int = 0,
    val winner: Player? = null
)

interface IGameEngine {
    val stateFlow: StateFlow<GameState>

    fun rollDice()
    fun buyEstablishment(card: Establishment)
    fun buildLandmark(landmark: Landmark)
    fun abortGame()
}