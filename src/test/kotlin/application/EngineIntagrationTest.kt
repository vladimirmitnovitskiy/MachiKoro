package application

import domain.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class EngineIntegrationTest {

    @Test
    fun `Engine should correctly process Red card priority and money transfer`() {
        // Arrange (Настройка)
        val alice = Player(name = "Алиса", balance = 3)
        val bob = Player(name = "Боб", balance = 1)

        // Алиса владеет Кафе (красная, забирает 2 монеты, работает на 3)
        alice.establishments.add(
            Establishment(
                "Кафе", 2, listOf(3), CardColor.RED, RedIncomeStrategy(2)
            )
        )
        // Боб владеет Пекарней (зеленая, дает 1 монету, работает на 3)
        bob.establishments.add(
            Establishment(
                "Пекарня", 1, listOf(3), CardColor.GREEN, GreenIncomeStrategy(1)
            )
        )

        val fakeDice = FakeDice(3)
        val engine = MachiKoroEngine(listOf(bob, alice), Market(emptyList()), fakeDice)


        engine.rollDice() // Боб бросает кубик, выпадает 3

        // Assert (Проверка результатов)
        // ЧТО ДОЛЖНО ПРОИЗОЙТИ ПО ПРАВИЛАМ:
        // 1. Сначала срабатывает Кафе Алисы (Красная, приоритет выше).
        // 2. Боб должен отдать 2 монеты, но у него только 1. Он отдает 1 (баланс Боба = 0, Алисы = 3+1=4).
        // 3. Затем срабатывает Пекарня Боба (Зеленая). Боб получает 1 монету из банка.
        // ИТОГ: У Боба 1 монета, у Алисы 4 монеты.

        val state = engine.stateFlow.value
        val stateBob = state.players.find { it.name == "Боб" }!!
        val stateAlice = state.players.find { it.name == "Алиса" }!!

        assertEquals(1, stateBob.balance, "Баланс Боба должен быть 1 (0 после штрафа + 1 от Пекарни)")
        assertEquals(4, stateAlice.balance, "Баланс Алисы должен быть 4 (получила 1 монету штрафа)")
    }
}