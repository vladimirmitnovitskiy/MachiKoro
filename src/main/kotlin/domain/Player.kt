package domain

import java.util.UUID

class Player(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    var balance: Int = 3 // По правилам на старте дают 3 монеты
) {
    // Карты, которые уже построил игрок
    val establishments = mutableListOf<Establishment>()
    val landmarks = mutableListOf<Landmark>()

    fun addCoins(amount: Int) {
        if (amount > 0) balance += amount
    }

    // Возвращает, сколько реально монет удалось списать
    // (по правилам баланс не может стать меньше 0)
    fun deductCoins(amount: Int): Int {
        val actualDeduction = if (balance >= amount) amount else balance
        balance -= actualDeduction
        return actualDeduction
    }

    // Проверка победы: если построено 4 достопримечательности
    fun hasWon(): Boolean {
        return landmarks.count { it.isBuilt } >= 4
    }
}