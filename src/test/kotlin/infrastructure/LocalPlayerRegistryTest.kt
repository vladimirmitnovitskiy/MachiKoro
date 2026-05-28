package infrastructure

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID

class SQLiteMatchRepositoryTest {

    private val testDbFile = "test_machikoro.db"
    private val repository = SQLiteMatchRepository("jdbc:sqlite:$testDbFile")

    @AfterEach
    fun cleanUp() {
        val file = File(testDbFile)
        if (file.exists()) {
            file.delete()
        }
    }

    @Test
    fun `Repository should create new player and update stats after match`() {
        val profile = repository.getOrCreatePlayer("Тестер SQLite")
        assertEquals(0, profile.totalGames)
        assertEquals(0, profile.totalWins)

        repository.saveMatchResult(
            winnerId = profile.id,
            isAborted = false,
            allParticipants = listOf(profile.id)
        )

        val updatedProfile = repository.getOrCreatePlayer("Тестер SQLite")
        assertEquals(1, updatedProfile.totalGames, "Счетчик игр должен увеличиться")
        assertEquals(1, updatedProfile.totalWins, "Счетчик побед должен увеличиться")
    }
}