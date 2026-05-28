package application

import domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MachiKoroEngine(
    private val players: List<Player>,
    private val market: Market,
    private val dice: Dice = RandomDice()
) : IGameEngine {

    private var currentPlayerIndex = 0

    // Инициализируем начальное состояние
    private val _stateFlow = MutableStateFlow(
        GameState(
            activePlayer = players[currentPlayerIndex],
            players = players,
            market = market
        )
    )
    override val stateFlow: StateFlow<GameState> = _stateFlow.asStateFlow()

    override fun rollDice() {
        if (_stateFlow.value.winner != null) return

        val roll = dice.roll()
        val activePlayer = players[currentPlayerIndex]
        val context = TurnContext(roll, activePlayer, players)

        println("\n🎲 Игрок ${activePlayer.name} бросил кубик: Выпало $roll!")

        // 1. Собираем все построенные предприятия ВСЕХ игроков
        val triggeredCards = mutableListOf<Pair<Establishment, Player>>()
        for (player in players) {
            for (card in player.establishments) {
                if (card.activationNumbers.contains(roll)) {
                    triggeredCards.add(Pair(card, player))
                }
            }
        }

        // 2. ПОДДЕРЖКА ИНВАРИАНТНОСТИ: сортируем по приоритету (Красные -> Зеленые/Синие -> Фиолетовые)
        triggeredCards.sortBy { it.first.color.priority }

        // 3. Выполняем эффекты карт (Паттерн Стратегия)
        for ((card, owner) in triggeredCards) {
            card.strategy.calculateIncome(context, owner)
        }

        // 4. Обновляем состояние
        updateState { it.copy(lastDiceRoll = roll) }
    }

    override fun buyEstablishment(card: Establishment) {
        val activePlayer = players[currentPlayerIndex]

        if (!market.availableCards.contains(card)) return
        if (activePlayer.balance < card.cost) return

        activePlayer.buyEstablishment(card)
        market.removeCard(card)

        nextTurn()
    }

    override fun buildLandmark(landmark: Landmark) {
        val activePlayer = players[currentPlayerIndex]

        if (!activePlayer.canBuildLandmark(landmark)) {
            return
        }

        activePlayer.buildLandmark(landmark)

        // Проверка победы
        if (activePlayer.hasWon()) {
            updateState { it.copy(winner = activePlayer) }
        } else {
            nextTurn()
        }
    }

    override fun abortGame() {
        println("Игра прервана.")
    }

    override fun passTurn() {
        val activePlayer = players[currentPlayerIndex]
        println("👉 Игрок ${activePlayer.name} решил ничего не покупать и передает ход.")
        nextTurn()
    }

    // Вспомогательный метод передачи хода
    private fun nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        updateState { it.copy(activePlayer = players[currentPlayerIndex], lastDiceRoll = 0) }
        println("👉 Ход переходит к игроку: ${players[currentPlayerIndex].name}")
    }

    private fun updateState(update: (GameState) -> GameState) {
        _stateFlow.value = update(_stateFlow.value)
    }
}