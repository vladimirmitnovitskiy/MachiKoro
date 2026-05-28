package application

import domain.Player
import infrastructure.SQLiteMatchRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class EngineDbDecoratorTest {

    private val testDbFile = "test_decorator.db"
    private val repository = SQLiteMatchRepository("jdbc:sqlite:$testDbFile")

    @AfterEach
    fun cleanUp() {
        val file = File(testDbFile)
        if (file.exists()) file.delete()
    }

    @Test
    fun `abortGame should be intercepted by decorator and save aborted match to DB`() {
        // 1. Создаем игроков в БД
        val p1 = repository.getOrCreatePlayer("Игрок 1")
        val p2 = repository.getOrCreatePlayer("Игрок 2")

        // 2. Инициализируем игру с Декоратором
        val domainPlayers = listOf(Player(id = p1.id, name = p1.name), Player(id = p2.id, name = p2.name))
        val baseEngine = ClassicGameFactory().createGame(domainPlayers)
        val decoratedEngine = EngineDbDecorator(baseEngine, repository)

        // 3. СИМУЛИРУЕМ ВЫХОД ИЗ ИГРЫ
        decoratedEngine.abortGame()

        // 4. Проверяем новую функциональность: История матчей
        val history = repository.loadMatchHistory()

        assertEquals(1, history.size, "В истории должна появиться одна запись")
        assertTrue(history[0].isAborted, "Запись должна быть помечена как прерванная (aborted = true)")
        assertEquals("Прервана", history[0].winnerName, "Статус должен быть 'Прервана'")
    }
}