package domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StrategyIntegrationTest {

    @Test
    fun `Blue strategy gives coins in ANY turn, Green only in OWN turn`() {
        val activePlayer = Player(name = "Активный", balance = 0)
        val passivePlayer = Player(name = "Пассивный", balance = 0)

        val context = TurnContext(diceRoll = 2, activePlayer = activePlayer, allPlayers = listOf(activePlayer, passivePlayer))

        val blueCard = BlueIncomeStrategy(1)
        val greenCard = GreenIncomeStrategy(2)

        // Эмулируем, что выпала нужная цифра.
        // 1. Пассивный игрок владеет синей картой (должен получить деньги, хоть ход и не его)
        blueCard.calculateIncome(context, passivePlayer)

        // 2. Пассивный игрок владеет зеленой картой (НЕ должен получить деньги, ход не его)
        greenCard.calculateIncome(context, passivePlayer)

        assertEquals(1, passivePlayer.balance, "Пассивный игрок должен получить только 1 монету от синей карты")

        // 3. Активный игрок владеет зеленой картой (должен получить, т.к. это его ход)
        greenCard.calculateIncome(context, activePlayer)
        assertEquals(2, activePlayer.balance, "Активный игрок должен получить 2 монеты от зеленой карты")
    }
}