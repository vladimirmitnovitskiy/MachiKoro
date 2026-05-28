package presentation

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import application.ClassicGameFactory
import application.IGameEngine
import infrastructure.LocalPlayerRegistry

// Состояния нашего приложения (какой экран открыт)
enum class AppScreen {
    LOBBY,
    GAME
}

@Composable
fun AppRouter(registry: LocalPlayerRegistry) {
    // Изначально мы в лобби
    var currentScreen by remember { mutableStateOf(AppScreen.LOBBY) }

    // Переменная для хранения запущенного движка игры
    var gameEngine by remember { mutableStateOf<IGameEngine?>(null) }

    MaterialTheme {
        when (currentScreen) {
            AppScreen.LOBBY -> {
                LobbyScreen(
                    registry = registry,
                    onStartGame = { players ->
                        // Когда нажали старт, Фабрика создает игру!
                        val factory = ClassicGameFactory()
                        gameEngine = factory.createGame(players)
                        currentScreen = AppScreen.GAME // Переключаем экран!
                    }
                )
            }
            AppScreen.GAME -> {
                // Если движок почему-то null, возвращаемся назад
                if (gameEngine == null) {
                    currentScreen = AppScreen.LOBBY
                } else {
                    GameScreen(
                        engine = gameEngine!!,
                        onExitGame = {
                            currentScreen = AppScreen.LOBBY // Возврат в лобби при выходе
                        }
                    )
                }
            }
        }
    }
}

fun main() = application {
    val registry = LocalPlayerRegistry() // Создаем реестр при старте приложения

    Window(
        onCloseRequest = ::exitApplication,
        title = "Machi Koro",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        AppRouter(registry)
    }
}