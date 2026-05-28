package presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.Player
import infrastructure.LocalPlayerRegistry
import infrastructure.PlayerProfile
import java.util.*

@Composable
fun LobbyScreen(
    registry: LocalPlayerRegistry,
    onStartGame: (List<Player>) -> Unit
) {
    // Состояния интерфейса
    var newPlayerName by remember { mutableStateOf("") }
    var selectedProfiles by remember { mutableStateOf(listOf<PlayerProfile>()) }

    // Загружаем статистику из файла
    val allProfiles = remember { mutableStateListOf(*registry.loadProfiles().toTypedArray()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Лобби Мачи Коро", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        // Блок добавления игрока
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newPlayerName,
                onValueChange = { newPlayerName = it },
                label = { Text("Имя нового игрока") }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (newPlayerName.isNotBlank() && selectedProfiles.none { it.name == newPlayerName }) {
                    // Ищем в базе или создаем нового
                    val profile = allProfiles.find { it.name == newPlayerName }
                        ?: PlayerProfile(UUID.randomUUID(), newPlayerName).also {
                            registry.saveProfile(it)
                            allProfiles.add(it)
                        }
                    selectedProfiles = selectedProfiles + profile
                    newPlayerName = ""
                }
            }) {
                Text("Добавить в игру")
            }
        }

        Spacer(Modifier.height(24.dp))

        // Список добавленных в текущую игру
        Text("Участники партии:", style = MaterialTheme.typography.h6)
        selectedProfiles.forEach { profile ->
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp), elevation = 4.dp) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Мэр ${profile.name}", style = MaterialTheme.typography.subtitle1)
                    Text("Побед: ${profile.totalWins} / Игр: ${profile.totalGames}")
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Кнопка старта (активна, если игроков >= 2)
        Button(
            onClick = {
                // Преобразуем профили в игроков и запускаем игру!
                val gamePlayers = selectedProfiles.map { Player(id = it.id, name = it.name) }
                onStartGame(gamePlayers)
            },
            enabled = selectedProfiles.size >= 2,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("НАЧАТЬ ИГРУ")
        }
    }
}