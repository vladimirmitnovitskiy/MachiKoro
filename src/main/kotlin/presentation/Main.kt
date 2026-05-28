package presentation

import application.ClassicGameFactory
import application.IGameEngine
import domain.Player

// 1. Изолированный класс - все возможные команды
sealed class GameCommand {
    object RollDice : GameCommand()
    object PassTurn : GameCommand()
    object Exit : GameCommand()
    data class BuyEstablishment(val index: Int) : GameCommand()
    data class BuildLandmark(val index: Int) : GameCommand()
    data class Invalid(val reason: String) : GameCommand()
}

// 2. Валидатор запросов
class CommandValidator(
    private val marketCardsCount: Int,
    private val landmarksCount: Int
) {
    fun parse(input: String?): GameCommand {
        val raw = input?.trim()?.lowercase()
        if (raw.isNullOrEmpty()) return GameCommand.Invalid("Пустой ввод")

        // Базовые команды
        when (raw) {
            "1" -> return GameCommand.RollDice
            "0" -> return GameCommand.PassTurn
            "exit", "quit", "выход" -> return GameCommand.Exit
        }

        val number = raw.toIntOrNull() ?: return GameCommand.Invalid("Неизвестная команда: $raw")

        if (number in 1..marketCardsCount) {
            return GameCommand.BuyEstablishment(number - 1)
        }

        val landmarkIndex = number - marketCardsCount - 1
        if (landmarkIndex in 0 until landmarksCount) {
            return GameCommand.BuildLandmark(landmarkIndex)
        }

        return GameCommand.Invalid("Число вне диапазона доступных покупок")
    }
}

fun main() {
    println("=====================================")
    println("      ДОБРО ПОЖАЛОВАТЬ В МАЧИ КОРО     ")
    println("=====================================")

    // Фаза Лобби
    val players = mutableListOf<Player>()
    println("Введите имена игроков (введите 'start' для начала игры):")
    while (true) {
        print("Игрок ${players.size + 1}: ")
        val input = readlnOrNull()?.trim()

        if (input.equals("start", ignoreCase = true)) {
            if (players.size >= 2) break
            else println("Нужно хотя бы 2 игрока!")
        } else if (!input.isNullOrEmpty()) {
            players.add(Player(name = input))
        }
    }

    val factory = ClassicGameFactory()
    val engine = factory.createGame(players)

    // Игровой цикл
    while (true) {
        val state = engine.stateFlow.value
        if (state.winner != null) {
            println("\n🎉 ПАРТИЯ ЗАВЕРШЕНА! Поздравляем мэра ${state.winner.name}! 🎉")
            break
        }

        println("\n=====================================")
        println("Ход игрока: ${state.activePlayer.name} | Баланс: ${state.activePlayer.balance} монет")
        println("Достопримечательности: ${state.activePlayer.landmarks.count { it.isBuilt }}/4")
        println("Выберите действие:")
        println("1. Бросить кубик")
        println("0. Выйти из игры")
        print("> ")

        val validator = CommandValidator(marketCardsCount = 0, landmarksCount = 0)

        when (val command = validator.parse(readlnOrNull())) {
            is GameCommand.RollDice -> {
                engine.rollDice()
                showShopMenu(engine)
            }
            // В главном меню "0" и текстовые команды выхода ведут к завершению
            is GameCommand.PassTurn, is GameCommand.Exit -> {
                engine.abortGame()
                break
            }
            is GameCommand.Invalid -> println("❌ ${command.reason}")
            else -> println("❌ Сейчас это действие недоступно.")
        }
    }
}

fun showShopMenu(engine: IGameEngine) {
    val state = engine.stateFlow.value
    if (state.winner != null) return

    println("\n--- РЫНОК ---")
    val uniqueCards = state.market.uniqueAvailableCards
    uniqueCards.forEachIndexed { index, card ->
        println("${index + 1}. Купить '${card.name}' (Цена: ${card.cost}, Кубик: ${card.activationNumbers})")
    }

    val unbuiltLandmarks = state.activePlayer.landmarks.filter { !it.isBuilt }
    println("\n--- ДОСТОПРИМЕЧАТЕЛЬНОСТИ ---")
    unbuiltLandmarks.forEachIndexed { index, landmark ->
        val offset = uniqueCards.size + index + 1
        println("$offset. Построить '${landmark.name}' (Цена: ${landmark.cost})")
    }
    println("0. Ничего не покупать (передать ход)")

    print("Что берём? > ")

    val validator = CommandValidator(
        marketCardsCount = uniqueCards.size,
        landmarksCount = unbuiltLandmarks.size
    )

    when (val command = validator.parse(readlnOrNull())) {
        is GameCommand.BuyEstablishment -> {
            engine.buyEstablishment(uniqueCards[command.index])
        }
        is GameCommand.BuildLandmark -> {
            engine.buildLandmark(unbuiltLandmarks[command.index])
        }
        is GameCommand.PassTurn -> {
            engine.passTurn()
        }
        is GameCommand.Exit -> {
            engine.abortGame()
        }
        is GameCommand.Invalid -> {
            println("❌ ${command.reason}. Вы пропускаете ход из-за ошибки.")
            engine.passTurn()
        }
        else -> {
            println("❌ Неизвестная команда.")
            engine.passTurn()
        }
    }
}