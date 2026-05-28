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
fun LobbyScreen(registry: LocalPlayerRegistry, onStartGame: (List<Player>) -> Unit) {
    var newPlayerName by remember { mutableStateOf("") }
    var selectedProfiles by remember { mutableStateOf(listOf<PlayerProfile>()) }
    val allProfiles = remember { mutableStateListOf(*registry.loadProfiles().toTypedArray()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Лобби Мачи Коро", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        AddPlayerSection(newPlayerName, onNameChange = { newPlayerName = it }) {
            if (newPlayerName.isNotBlank() && selectedProfiles.none { it.name == newPlayerName }) {
                val profile = allProfiles.find { it.name == newPlayerName }
                    ?: PlayerProfile(UUID.randomUUID(), newPlayerName).also {
                        registry.saveProfile(it)
                        allProfiles.add(it)
                    }
                selectedProfiles = selectedProfiles + profile
                newPlayerName = ""
            }
        }

        Spacer(Modifier.height(24.dp))

        PlayerListSection(selectedProfiles)

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onStartGame(selectedProfiles.map { Player(id = it.id, name = it.name) }) },
            enabled = selectedProfiles.size >= 2,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("НАЧАТЬ ИГРУ") }
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