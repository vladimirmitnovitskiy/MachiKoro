package infrastructure

import java.io.File
import java.util.UUID

// Профиль игрока для статистики (отличается от игрового тем, что хранит победы за все время)
data class PlayerProfile(
    val id: UUID,
    val name: String,
    var totalGames: Int = 0,
    var totalWins: Int = 0
)

class LocalPlayerRegistry(private val fileName: String = "players_registry.csv") {

    // Читаем игроков из файла
    fun loadProfiles(): List<PlayerProfile> {
        val file = File(fileName)
        if (!file.exists()) return emptyList()

        return file.readLines().mapNotNull { line ->
            try {
                val parts = line.split(",")
                PlayerProfile(
                    id = UUID.fromString(parts[0]),
                    name = parts[1],
                    totalGames = parts[2].toInt(),
                    totalWins = parts[3].toInt()
                )
            } catch (e: Exception) {
                null // Если строчка сломана, игнорируем её
            }
        }
    }

    // Сохраняем или обновляем профиль
    fun saveProfile(profile: PlayerProfile) {
        val existing = loadProfiles().toMutableList()

        val index = existing.indexOfFirst { it.id == profile.id } // Ищем по id

        if (index >= 0) {
            existing[index] = profile // Обновляем существующего
        } else {
            existing.add(profile)     // Добавляем нового
        }

        // Записываем обратно в файл в формате CSV
        val file = File(fileName)
        file.writeText(existing.joinToString("\n") {
            "${it.id},${it.name},${it.totalGames},${it.totalWins}"
        })
    }
}