import application.*
import domain.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SystemTest {

    @Test
    fun `Full game simulation until a player wins`() {
        val player1 = Player(name = "Игрок 1", balance = 100)
        val player2 = Player(name = "Игрок 2", balance = 100)

        val factory = ClassicGameFactory()
        val engine = factory.createGame(listOf(player1, player2))

        // Симулируем ходы, пока кто-то не победит
        // Игрок 1 просто строит достопримечательности каждый свой ход

        var safetyCounter = 0
        while (engine.stateFlow.value.winner == null && safetyCounter < 50) {
            val state = engine.stateFlow.value
            engine.rollDice()

            // Пытаемся найти непостроенную достопримечательность
            val unbuilt = state.activePlayer.landmarks.firstOrNull { !it.isBuilt }
            if (unbuilt != null) {
                engine.buildLandmark(unbuilt)
            } else {
                engine.passTurn()
            }
            safetyCounter++
        }

        val finalState = engine.stateFlow.value
        assertNotNull(finalState.winner, "Игра должна закончиться победой одного из игроков")
        assertEquals(4, finalState.winner?.landmarks?.count { it.isBuilt }, "У победителя должно быть 4 достопримечательности")
        println("Системный тест пройден: Победил ${finalState.winner?.name} за $safetyCounter полуходов.")
    }
}