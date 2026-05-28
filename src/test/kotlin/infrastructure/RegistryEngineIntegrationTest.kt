package infrastructure

import application.ClassicGameFactory
import application.EngineDbDecorator
import domain.Player
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class RegistryEngineIntegrationTest {

    private val testDbFile = "test_integration.db"
    private val repository = SQLiteMatchRepository("jdbc:sqlite:$testDbFile")

    @AfterEach
    fun cleanUp() {
        val file = File(testDbFile)
        if (file.exists()) file.delete()
    }

    @Test
    fun `Profiles from DB should successfully initialize the Game Engine`() {
        val profile1 = repository.getOrCreatePlayer("Игрок Из БД 1")
        val profile2 = repository.getOrCreatePlayer("Игрок Из БД 2")

        val gamePlayers = listOf(
            Player(id = profile1.id, name = profile1.name),
            Player(id = profile2.id, name = profile2.name)
        )

        val factory = ClassicGameFactory()
        val engine = EngineDbDecorator(factory.createGame(gamePlayers), repository)

        val state = engine.stateFlow.value
        assertEquals(2, state.players.size)
        assertEquals(profile1.id, state.players[0].id)
        assertEquals("Игрок Из БД 1", state.players[0].name)
    }
}