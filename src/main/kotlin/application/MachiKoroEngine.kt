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

        // Валидация покупки
        if (!market.availableCards.contains(card)) {
            println("❌ Этой карты нет на рынке!")
            return
        }
        if (activePlayer.balance < card.cost) {
            println("❌ Недостаточно монет! У ${activePlayer.name} только ${activePlayer.balance}, а нужно ${card.cost}")
            return
        }

        // Покупка
        activePlayer.deductCoins(card.cost)
        activePlayer.establishments.add(card)
        market.removeCard(card)

        println("✅ ${activePlayer.name} купил(а) предприятие: ${card.name}")

        nextTurn() // Передаем ход
    }

    override fun buildLandmark(landmark: Landmark) {
        val activePlayer = players[currentPlayerIndex]

        // Проверяем, есть ли уже такая постройка
        if (activePlayer.landmarks.any { it.name == landmark.name && it.isBuilt }) {
            println("❌ Достопримечательность ${landmark.name} уже построена!")
            return
        }

        // Проверяем деньги
        if (activePlayer.balance < landmark.cost) {
            println("❌ Недостаточно монет для постройки ${landmark.name}!")
            return
        }

        // Строим
        activePlayer.deductCoins(landmark.cost)
        val targetLandmark = activePlayer.landmarks.find { it.name == landmark.name }

        if (targetLandmark != null) {
            targetLandmark.isBuilt = true
        } else {
            // Если её не было в списке, добавляем построенную
            landmark.isBuilt = true
            activePlayer.landmarks.add(landmark)
        }

        println("🎉 УРА! Игрок ${activePlayer.name} построил достопримечательность: ${landmark.name}!")

        // Проверка условия победы
        if (activePlayer.hasWon()) {
            println("🏆 ИГРА ОКОНЧЕНА! Победитель: ${activePlayer.name}! 🏆")
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