package application

import domain.Establishment
import domain.Landmark
import infrastructure.IMatchRepository
import kotlinx.coroutines.flow.StateFlow

class EngineDbDecorator(
    private val inner: IGameEngine,
    private val repository: IMatchRepository
) : IGameEngine {

    override val stateFlow: StateFlow<GameState> = inner.stateFlow

    override fun rollDice() {
        inner.rollDice()
    }

    override fun buyEstablishment(card: Establishment) {
        inner.buyEstablishment(card)
    }

    override fun passTurn() {
        inner.passTurn()
    }

    override fun buildLandmark(landmark: Landmark) {
        inner.buildLandmark(landmark)

        val state = inner.stateFlow.value
        if (state.winner != null) {
            val allIds = state.players.map { it.id }
            repository.saveMatchResult(
                winnerId = state.winner.id,
                isAborted = false,
                allParticipants = allIds
            )
            println("💾 Ура! Результаты партии сохранены в базу данных SQLite!")
        }
    }

    override fun abortGame() {
        inner.abortGame()
        val state = inner.stateFlow.value
        val allIds = state.players.map { it.id }
        repository.saveMatchResult(
            winnerId = null,
            isAborted = true,
            allParticipants = allIds
        )
        println("💾 Прерванная партия записана в историю БД.")
    }
}