package infrastructure

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID

class LocalPlayerRegistryTest {

    // Используем временный файл для тестов, чтобы не портить настоящую стату
    private val testFileName = "test_registry.csv"
    private val registry = LocalPlayerRegistry(testFileName)

    @AfterEach
    fun cleanUp() {
        // Удаляем тестовый файл после каждого теста
        val file = File(testFileName)
        if (file.exists()) {
            file.delete()
        }
    }

    @Test
    fun `Registry should save and load new profile correctly`() {
        val newProfile = PlayerProfile(UUID.randomUUID(), "Тестер", 5, 2)

        registry.saveProfile(newProfile)

        val loadedProfiles = registry.loadProfiles()
        assertEquals(1, loadedProfiles.size)
        assertEquals("Тестер", loadedProfiles[0].name)
        assertEquals(5, loadedProfiles[0].totalGames)
        assertEquals(2, loadedProfiles[0].totalWins)
    }

    @Test
    fun `Registry should update existing profile instead of duplicating`() {
        val id = UUID.randomUUID()
        val initialProfile = PlayerProfile(id, "Игрок", 1, 0)
        registry.saveProfile(initialProfile)

        // Игрок сыграл еще одну игру и победил
        val updatedProfile = PlayerProfile(id, "Игрок", 2, 1)
        registry.saveProfile(updatedProfile)

        val loadedProfiles = registry.loadProfiles()

        // В файле все еще должен быть только 1 человек
        assertEquals(1, loadedProfiles.size, "Профиль должен обновиться, а не дублироваться")
        assertEquals(2, loadedProfiles[0].totalGames)
        assertEquals(1, loadedProfiles[0].totalWins)
    }
}