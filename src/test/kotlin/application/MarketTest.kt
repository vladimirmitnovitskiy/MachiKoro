package application

import domain.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MarketTest {

    @Test
    fun `Market should remove card correctly`() {
        val card = Establishment("Тест", 1, listOf(1), CardColor.BLUE, BlueIncomeStrategy(1))
        val market = Market(listOf(card))

        assertEquals(1, market.availableCards.size)

        market.removeCard(card)

        assertTrue(market.availableCards.isEmpty(), "Рынок должен стать пустым после удаления карты")
    }

    @Test
    fun `Market availableCards should be immutable from outside`() {
        val market = Market(emptyList())

        val cards = market.availableCards
        assertThrows(ClassCastException::class.java) {
            (cards as MutableList).add(
                Establishment("Хак", 1, listOf(1), CardColor.BLUE, BlueIncomeStrategy(1))
            )
        }
    }
}