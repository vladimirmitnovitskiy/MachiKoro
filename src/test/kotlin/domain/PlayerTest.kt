package domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.* // Импорт функций проверок

class PlayerTest {

    @Test
    fun `addCoins should increase balance`() {
        // Arrange (Подготовка)
        val player = Player(name = "Тестер", balance = 3)

        // Act (Действие)
        player.addCoins(2)

        // Assert (Проверка)
        assertEquals(5, player.balance, "Баланс должен увеличиться на 2")
    }

    @Test
    fun `deductCoins should return exact amount if balance is sufficient`() {
        val player = Player(name = "Тестер", balance = 5)

        val deducted = player.deductCoins(3)

        assertEquals(3, deducted)
        assertEquals(2, player.balance)
    }

    @Test
    fun `deductCoins should not make balance negative and return what is left`() {
        // Это самое важное правило игры: нельзя забрать больше, чем есть!
        val player = Player(name = "Тестер", balance = 2)

        val deducted = player.deductCoins(5) // Пытаемся забрать 5, но есть только 2

        assertEquals(2, deducted, "Должно списаться только 2 монеты")
        assertEquals(0, player.balance, "Баланс не должен уйти в минус")
    }
}