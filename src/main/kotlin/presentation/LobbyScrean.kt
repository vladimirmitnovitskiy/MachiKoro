package presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domain.Player
import infrastructure.IMatchRepository
import infrastructure.MatchRecord
import infrastructure.PlayerProfile

@Composable
fun LobbyScreen(repository: IMatchRepository, onStartGame: (List<Player>) -> Unit) {
    var newPlayerName by remember { mutableStateOf("") }
    var selectedProfiles by remember { mutableStateOf(listOf<PlayerProfile>()) }
    var showStatsDialog by remember { mutableStateOf(false) } // Флаг показа статистики

    val allProfiles = remember { mutableStateListOf(*repository.loadPlayersProfiles().toTypedArray()) }

    if (showStatsDialog) {
        StatisticsDialog(
            repository = repository,
            onClose = { showStatsDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Лобби Мачи Коро", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        AddPlayerSection(newPlayerName, onNameChange = { newPlayerName = it }) {
            if (newPlayerName.isNotBlank() && selectedProfiles.none { it.name == newPlayerName }) {
                val profile = repository.getOrCreatePlayer(newPlayerName)
                if (allProfiles.none { it.id == profile.id }) allProfiles.add(profile)
                selectedProfiles = selectedProfiles + profile
                newPlayerName = ""
            }
        }

        Spacer(Modifier.height(24.dp))
        PlayerListSection(selectedProfiles)
        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedButton(
                onClick = { showStatsDialog = true },
                modifier = Modifier.height(50.dp).weight(1f)
            ) { Text("📊 Статистика и История") }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = { onStartGame(selectedProfiles.map { Player(id = it.id, name = it.name) }) },
                enabled = selectedProfiles.size >= 2,
                modifier = Modifier.height(50.dp).weight(2f)
            ) { Text("НАЧАТЬ ИГРУ") }
        }
    }
}

@Composable
fun AddPlayerSection(name: String, onNameChange: (String) -> Unit, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Имя нового игрока") })
        Spacer(Modifier.width(8.dp))
        Button(onClick = onAdd) { Text("Добавить в игру") }
    }
}

@Composable
fun PlayerListSection(profiles: List<PlayerProfile>) {
    Text("Участники партии:", style = MaterialTheme.typography.h6)
    profiles.forEach { profile ->
        Card(modifier = Modifier.fillMaxWidth().padding(4.dp), elevation = 4.dp) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Мэр ${profile.name}", style = MaterialTheme.typography.subtitle1)
                Text("Побед: ${profile.totalWins} / Игр: ${profile.totalGames}")
            }
        }
    }
}

@Composable
fun StatisticsDialog(repository: IMatchRepository, onClose: () -> Unit) {
    val history = remember { repository.loadMatchHistory() }
    val leaderboard = remember { repository.loadPlayersProfiles().sortedByDescending { it.totalWins } }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Глобальная статистика", fontWeight = FontWeight.Bold) },
        text = {
            Row(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                Column(modifier = Modifier.weight(1f).border(1.dp, Color.LightGray).padding(8.dp)) {
                    Text("Топ Мэров (Рейтинг)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Divider(Modifier.padding(vertical = 8.dp))
                    LazyColumn {
                        items(leaderboard) { profile ->
                            Text("${profile.name}: ${profile.totalWins} побед (${profile.totalGames} игр)")
                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1.5f).border(1.dp, Color.LightGray).padding(8.dp)) {
                    Text("История партий", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Divider(Modifier.padding(vertical = 8.dp))
                    LazyColumn {
                        items(history) { record ->
                            val color = if (record.isAborted) Color.Red else Color(0xFF2E7D32)
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(record.date, fontSize = 12.sp, color = Color.Gray)
                                Text("Игра #${record.id} | ${record.winnerName}", color = color, fontWeight = FontWeight.SemiBold)
                            }
                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) { Text("Закрыть") }
        },
        modifier = Modifier.width(800.dp)
    )
}