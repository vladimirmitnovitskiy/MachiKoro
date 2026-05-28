package infrastructure

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

// 1. СХЕМА БАЗЫ ДАННЫХ (Таблицы)

data class PlayerProfile(
    val id: UUID,
    val name: String,
    var totalGames: Int = 0,
    var totalWins: Int = 0
)

data class MatchRecord(
    val id: Int,
    val winnerName: String,
    val isAborted: Boolean,
    val date: String
)

// Таблица профилей игроков
object PlayersTable : Table("players") {
    val id = uuid("id")
    val name = varchar("name", 50)
    val totalGames = integer("total_games").default(0)
    val totalWins = integer("total_wins").default(0)

    override val primaryKey = PrimaryKey(id)
}

// Таблица истории партий
object MatchesTable : Table("matches") {
    val id = integer("id").autoIncrement()
    val winnerId = uuid("winner_id").nullable()
    val isAborted = bool("is_aborted")
    val timestamp = long("timestamp") // Время игры

    override val primaryKey = PrimaryKey(id)
}

// 2. ИНТЕРФЕЙС И РЕАЛИЗАЦИЯ

interface IMatchRepository {
    fun saveMatchResult(winnerId: UUID?, isAborted: Boolean, allParticipants: List<UUID>)
    fun loadPlayersProfiles(): List<PlayerProfile>
    fun getOrCreatePlayer(name: String): PlayerProfile
    fun loadMatchHistory(): List<MatchRecord>
}

class SQLiteMatchRepository(dbPath: String = "jdbc:sqlite:machikoro.db") : IMatchRepository {

    init {
        Database.connect(dbPath, driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(PlayersTable, MatchesTable)
        }
    }

    override fun saveMatchResult(winnerId: UUID?, isAborted: Boolean, allParticipants: List<UUID>) {
        transaction {
            MatchesTable.insert {
                it[this.winnerId] = winnerId
                it[this.isAborted] = isAborted
                it[this.timestamp] = System.currentTimeMillis()
            }
            allParticipants.forEach { participantId ->
                val currentGames = PlayersTable.select { PlayersTable.id eq participantId }.single()[PlayersTable.totalGames]
                PlayersTable.update({ PlayersTable.id eq participantId }) {
                    it[totalGames] = currentGames + 1
                }
            }

            if (winnerId != null) {
                val currentWins = PlayersTable.select { PlayersTable.id eq winnerId }.single()[PlayersTable.totalWins]
                PlayersTable.update({ PlayersTable.id eq winnerId }) {
                    it[totalWins] = currentWins + 1
                }
            }
        }
    }

    override fun loadPlayersProfiles(): List<PlayerProfile> {
        return transaction {
            PlayersTable.selectAll().map {
                PlayerProfile(
                    id = it[PlayersTable.id],
                    name = it[PlayersTable.name],
                    totalGames = it[PlayersTable.totalGames],
                    totalWins = it[PlayersTable.totalWins]
                )
            }
        }
    }

    override fun getOrCreatePlayer(playerName: String): PlayerProfile {
        return transaction {
            val existing = PlayersTable.select { PlayersTable.name eq playerName }.singleOrNull()

            if (existing != null) {
                PlayerProfile(
                    id = existing[PlayersTable.id],
                    name = existing[PlayersTable.name],
                    totalGames = existing[PlayersTable.totalGames],
                    totalWins = existing[PlayersTable.totalWins]
                )
            } else {
                val newId = UUID.randomUUID()
                PlayersTable.insert {
                    it[id] = newId
                    it[name] = playerName
                }
                PlayerProfile(newId, playerName, 0, 0)
            }
        }
    }

    override fun loadMatchHistory(): List<MatchRecord> {
        return transaction {
            val query = MatchesTable.leftJoin(PlayersTable, { winnerId }, { id })
                .selectAll()
                .orderBy(MatchesTable.timestamp to SortOrder.DESC) // Новые игры сверху

            val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")

            query.map {
                val winner = it[PlayersTable.name]
                val aborted = it[MatchesTable.isAborted]

                val statusText = when {
                    aborted -> "Прервана"
                    winner != null -> "Победил(а) $winner"
                    else -> "Ничья / Неизвестно"
                }

                MatchRecord(
                    id = it[MatchesTable.id],
                    winnerName = statusText,
                    isAborted = aborted,
                    date = dateFormat.format(java.util.Date(it[MatchesTable.timestamp]))
                )
            }
        }
    }
}