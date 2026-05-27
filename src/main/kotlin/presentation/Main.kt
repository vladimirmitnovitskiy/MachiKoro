package presentation

import application.ClassicGameFactory
import domain.Player

fun main() {
    println("=====================================")
    println("      ДОБРО ПОЖАЛОВАТЬ В МАЧИ КОРО     ")
    println("=====================================")

    // Фаза Лобби (ввод игроков)
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

        val choice = readlnOrNull()?.trim()

        when (choice) {
            "1" -> {
                engine.rollDice()
                showShopMenu(engine)
            }
            "0", "exit", "quit", "выход" -> {
                engine.abortGame()
                break
            }
            else -> println("Неверная команда.")
        }
    }
}

// Меню магазина после броска
fun showShopMenu(engine: application.IGameEngine) {
    val state = engine.stateFlow.value
    if (state.winner != null) return

    println("\n--- РЫНОК ---")
    val uniqueCards = state.market.availableCards.distinctBy { it.name }
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
    val choice = readlnOrNull()?.toIntOrNull()

    if (choice == 0 || choice == null) {
        engine.passTurn()
        return
    }

    if (choice <= uniqueCards.size) {
        val cardToBuy = uniqueCards[choice - 1]
        engine.buyEstablishment(cardToBuy)
    } else {
        val landmarkIndex = choice - uniqueCards.size - 1
        if (landmarkIndex < unbuiltLandmarks.size) {
            engine.buildLandmark(unbuiltLandmarks[landmarkIndex])
        } else {
            println("❌ Неверный номер достопримечательности.")
        }
    }
}