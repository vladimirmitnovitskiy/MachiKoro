package presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.IGameEngine
import domain.CardColor
import domain.Establishment
import domain.Landmark
import domain.Player

@Composable
fun GameScreen(engine: IGameEngine, onExitGame: () -> Unit) {
    val gameState by engine.stateFlow.collectAsState()

    // 1. Проверка экрана победы
    if (gameState.winner != null) {
        VictoryScreen(
            winnerName = gameState.winner!!.name,
            onExitGame = onExitGame
        )
        return
    }

    // 2. Основной экран игры
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        GameHeader(
            activePlayerName = gameState.activePlayer.name,
            activePlayerBalance = gameState.activePlayer.balance,
            lastDiceRoll = gameState.lastDiceRoll
        )

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.weight(1f)) {
            MarketPanel(
                modifier = Modifier.weight(1f),
                uniqueCards = gameState.market.uniqueAvailableCards,
                unbuiltLandmarks = gameState.activePlayer.landmarks.filter { !it.isBuilt },
                playerBalance = gameState.activePlayer.balance,
                onBuyCard = { engine.buyEstablishment(it) },
                onBuildLandmark = { engine.buildLandmark(it) }
            )

            Spacer(Modifier.width(16.dp))

            PlayersPanel(
                modifier = Modifier.weight(1f),
                players = gameState.players
            )
        }

        Spacer(Modifier.height(16.dp))

        ActionPanel(
            canRollDice = gameState.lastDiceRoll == 0,
            canPassTurn = gameState.lastDiceRoll > 0,
            onRollDice = { engine.rollDice() },
            onPassTurn = { engine.passTurn() },
            onExitGame = {
                engine.abortGame()
                onExitGame()
            }
        )
    }
}


@Composable
fun VictoryScreen(winnerName: String, onExitGame: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏆 ПАРТИЯ ЗАВЕРШЕНА 🏆", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
            Spacer(Modifier.height(16.dp))
            Text("Победитель: Мэр $winnerName!", fontSize = 24.sp)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onExitGame) { Text("Вернуться в лобби") }
        }
    }
}

@Composable
fun GameHeader(activePlayerName: String, activePlayerBalance: Int, lastDiceRoll: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Ход мэра: $activePlayerName", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Баланс: $activePlayerBalance 💰", fontSize = 20.sp, color = Color(0xFF2E7D32))
        }
        if (lastDiceRoll > 0) {
            Text("🎲 Выпало: $lastDiceRoll", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
        } else {
            Text("Ожидание броска...", fontSize = 20.sp, color = Color.Gray)
        }
    }
}

@Composable
fun MarketPanel(
    modifier: Modifier,
    uniqueCards: List<Establishment>,
    unbuiltLandmarks: List<Landmark>,
    playerBalance: Int,
    onBuyCard: (Establishment) -> Unit,
    onBuildLandmark: (Landmark) -> Unit
) {
    Column(modifier = modifier.border(1.dp, Color.LightGray).padding(8.dp)) {
        Text("Рынок (Доступно)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Divider(Modifier.padding(vertical = 8.dp))

        LazyColumn {
            items(uniqueCards) { card ->
                MarketCardView(card, canAfford = playerBalance >= card.cost) {
                    onBuyCard(card)
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Достопримечательности", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Divider(Modifier.padding(vertical = 8.dp))
            }

            items(unbuiltLandmarks) { landmark ->
                LandmarkView(landmark, canAfford = playerBalance >= landmark.cost) {
                    onBuildLandmark(landmark)
                }
            }
        }
    }
}

@Composable
fun PlayersPanel(modifier: Modifier, players: List<Player>) {
    Column(modifier = modifier.border(1.dp, Color.LightGray).padding(8.dp)) {
        Text("Города игроков", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Divider(Modifier.padding(vertical = 8.dp))

        LazyColumn {
            items(players) { player ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Мэр ${player.name} | Баланс: ${player.balance} 💰", fontWeight = FontWeight.Bold)
                        val builtCount = player.landmarks.count { it.isBuilt }
                        Text("Достопримечательности: $builtCount / 4")
                        Text("Карты: ${player.establishments.joinToString { it.name }}", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionPanel(
    canRollDice: Boolean,
    canPassTurn: Boolean,
    onRollDice: () -> Unit,
    onPassTurn: () -> Unit,
    onExitGame: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(
            onClick = onRollDice,
            enabled = canRollDice,
            modifier = Modifier.height(50.dp).weight(1f)
        ) { Text("1. БРОСИТЬ КУБИК 🎲") }

        Spacer(Modifier.width(16.dp))

        Button(
            onClick = onPassTurn,
            enabled = canPassTurn, // Передать ход можно только после броска
            modifier = Modifier.height(50.dp).weight(1f),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFCDD2))
        ) { Text("0. ЗАВЕРШИТЬ ХОД (Ничего не покупать)") }

        Spacer(Modifier.width(16.dp))

        OutlinedButton(
            onClick = onExitGame,
            modifier = Modifier.height(50.dp)
        ) { Text("Сдаться / Выход") }
    }
}


@Composable
fun MarketCardView(card: Establishment, canAfford: Boolean, onBuy: () -> Unit) {
    val bgColor = when (card.color) {
        CardColor.BLUE -> Color(0xFFBBDEFB)
        CardColor.GREEN -> Color(0xFFC8E6C9)
        CardColor.RED -> Color(0xFFFFCDD2)
        CardColor.PURPLE -> Color(0xFFE1BEE7)
    }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), backgroundColor = bgColor) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(card.name, fontWeight = FontWeight.Bold)
                Text("Кубик: ${card.activationNumbers} | Цена: ${card.cost} 💰", fontSize = 12.sp)
            }
            Button(onClick = onBuy, enabled = canAfford) { Text("Купить") }
        }
    }
}

@Composable
fun LandmarkView(landmark: Landmark, canAfford: Boolean, onBuild: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), backgroundColor = Color(0xFFFFF9C4)) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(landmark.name, fontWeight = FontWeight.Bold)
                Text("Цена: ${landmark.cost} 💰", fontSize = 12.sp)
            }
            Button(onClick = onBuild, enabled = canAfford) { Text("Построить") }
        }
    }
}