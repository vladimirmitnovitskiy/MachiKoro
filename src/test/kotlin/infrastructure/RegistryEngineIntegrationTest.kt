package infrastructure

import application.ClassicGameFactory
import domain.Player
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID

class RegistryEngineIntegrationTest {

    private val testFileName = "test_integration_registry.csv"
    private val registry = LocalPlayerRegistry(testFileName)

    @AfterEach
    fun cleanUp() {
        val file = File(testFileName)
        if (file.exists()) file.delete()
    }

    @Test
    fun `Profiles from registry should successfully initialize the Game Engine`() {
        // 1. Создаем профили так, как это делает GUI при вводе имен
        val profile1 = PlayerProfile(UUID.randomUUID(), "Игрок Из Реестра 1", 10, 5)
        val profile2 = PlayerProfile(UUID.randomUUID(), "Игрок Из Реестра 2", 3, 0)

        registry.saveProfile(profile1)
        registry.saveProfile(profile2)

        // 2. Симулируем логику Лобби: читаем из файла и конвертируем в доменных Player
        val loadedProfiles = registry.loadProfiles()
        val gamePlayers = loadedProfiles.map { profile ->
            Player(id = profile.id, name = profile.name)
        }

        // 3. Передаем их Фабрике (как при нажатии кнопки "Старт" в GUI)
        val factory = ClassicGameFactory()
        val engine = factory.createGame(gamePlayers)

        // 4. Проверяем Интеграцию: Движок должен корректно "проглотить" игроков из файла
        val state = engine.stateFlow.value

        assertEquals(2, state.players.size, "В игре должно быть 2 игрока")

        // Проверяем, что ID и имена сохранились на всем пути от файла до Движка
        assertEquals(profile1.id, state.players[0].id)
        assertEquals("Игрок Из Реестра 1", state.players[0].name)
        assertEquals(3, state.players[0].balance, "Фабрика должна была выдать стартовые 3 монеты")

        assertEquals(profile2.id, state.players[1].id)

        // Убеждаемся, что игра готова к броску кубика первым игроком
        assertEquals(state.players[0], state.activePlayer)
    }
}