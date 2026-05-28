package presentation

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import application.ClassicGameFactory
import application.EngineDbDecorator
import application.IGameEngine
import infrastructure.IMatchRepository
import infrastructure.SQLiteMatchRepository

enum class AppScreen { LOBBY, GAME }

@Composable
fun AppRouter(repository: IMatchRepository) {
    var currentScreen by remember { mutableStateOf(AppScreen.LOBBY) }
    var gameEngine by remember { mutableStateOf<IGameEngine?>(null) }

    MaterialTheme {
        when (currentScreen) {
            AppScreen.LOBBY -> {
                LobbyScreen(
                    repository = repository,
                    onStartGame = { players ->
                        val factory = ClassicGameFactory()
                        val originalEngine = factory.createGame(players)
                        gameEngine = EngineDbDecorator(originalEngine, repository)
                        currentScreen = AppScreen.GAME
                    }
                )
            }
            AppScreen.GAME -> {
                if (gameEngine == null) {
                    currentScreen = AppScreen.LOBBY
                } else {
                    GameScreen(
                        engine = gameEngine!!,
                        onExitGame = { currentScreen = AppScreen.LOBBY }
                    )
                }
            }
        }
    }
}

fun main() = application {
    val dbRepository = SQLiteMatchRepository()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Machi Koro",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        AppRouter(dbRepository)
    }
}