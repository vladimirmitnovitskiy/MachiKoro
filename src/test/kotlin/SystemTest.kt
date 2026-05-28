import application.*
import domain.*
import infrastructure.SQLiteMatchRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class SystemTest {

    private val testDbFile = "test_system.db"
    private val repository = SQLiteMatchRepository("jdbc:sqlite:$testDbFile")

    @AfterEach
    fun cleanUp() {
        val file = File(testDbFile)
        if (file.exists()) file.delete()
    }

    @Test
    fun `Full game simulation with Database integration until a player wins`() {
        // 1. Игроки создаются через БД, чтобы получить реальные UUID
        val p1 = repository.getOrCreatePlayer("Системный Игрок 1")
        val p2 = repository.getOrCreatePlayer("Системный Игрок 2")

        // Дадим много денег для скорости
        val player1 = Player(id = p1.id, name = p1.name, balance = 100)
        val player2 = Player(id = p2.id, name = p2.name, balance = 100)

        // 2. Создаем игру ОБЕРНУТУЮ В ДЕКОРАТОР
        val factory = ClassicGameFactory()
        val engine = EngineDbDecorator(factory.createGame(listOf(player1, player2)), repository)

        var safetyCounter = 0
        // 3. Симулируем ходы, пока кто-то не победит
        while (engine.stateFlow.value.winner == null && safetyCounter < 50) {
            val state = engine.stateFlow.value
            engine.rollDice()

            val unbuilt = state.activePlayer.landmarks.firstOrNull { !it.isBuilt }
            if (unbuilt != null) {
                engine.buildLandmark(unbuilt)
            } else {
                engine.passTurn()
            }
            safetyCounter++
        }

        // 4. ПРОВЕРКИ
        val finalState = engine.stateFlow.value
        assertNotNull(finalState.winner, "Игра должна закончиться победой")

        // РАСШИРЕНИЕ: Проверяем, что БД отработала корректно после победы!
        val history = repository.loadMatchHistory()
        assertEquals(1, history.size, "Матч должен быть сохранен в историю")
        assertFalse(history[0].isAborted, "Матч не прерван, а завершен победой")
        assertTrue(history[0].winnerName.contains(finalState.winner!!.name), "Имя победителя должно быть в логах")

        val updatedWinnerProfile = repository.getOrCreatePlayer(finalState.winner!!.name)
        assertEquals(1, updatedWinnerProfile.totalWins, "У победителя должна прибавиться 1 победа в базе")
    }
}