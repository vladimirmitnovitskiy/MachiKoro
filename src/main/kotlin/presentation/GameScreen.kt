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

@Composable
fun GameScreen(engine: IGameEngine, onExitGame: () -> Unit) {
    // МАГИЯ COMPOSE: Подписываемся на состояние движка!
    // Любое изменение в StateFlow заставит экран перерисоваться автоматически.
    val gameState by engine.stateFlow.collectAsState()

    if (gameState.winner != null) {
        // Экран победы
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆 ПАРТИЯ ЗАВЕРШЕНА 🏆", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                Spacer(Modifier.height(16.dp))
                Text("Победитель: Мэр ${gameState.winner!!.name}!", fontSize = 24.sp)
                Spacer(Modifier.height(32.dp))
                Button(onClick = onExitGame) { Text("Вернуться в лобби") }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // ВЕРХНЯЯ ПАНЕЛЬ: Чей ход и результат кубика
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Ход мэра: ${gameState.activePlayer.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Баланс: ${gameState.activePlayer.balance} 💰", fontSize = 20.sp, color = Color(0xFF2E7D32))
            }
            if (gameState.lastDiceRoll > 0) {
                Text("🎲 Выпало: ${gameState.lastDiceRoll}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
            } else {
                Text("Ожидание броска...", fontSize = 20.sp, color = Color.Gray)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ИГРОВОЙ СТОЛ (Рынок слева, Игроки справа)
        Row(modifier = Modifier.weight(1f)) {
            // ЛЕВАЯ КОЛОНКА: Рынок
            Column(modifier = Modifier.weight(1f).border(1.dp, Color.LightGray).padding(8.dp)) {
                Text("Рынок (Доступно)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Divider(Modifier.padding(vertical = 8.dp))

                LazyColumn {
                    val uniqueCards = gameState.market.availableCards.distinctBy { it.name }
                    items(uniqueCards) { card ->
                        MarketCardView(card, canAfford = gameState.activePlayer.balance >= card.cost) {
                            engine.buyEstablishment(card)
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Text("Достопримечательности", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Divider(Modifier.padding(vertical = 8.dp))
                    }

                    val unbuilt = gameState.activePlayer.landmarks.filter { !it.isBuilt }
                    items(unbuilt) { landmark ->
                        LandmarkView(landmark, canAfford = gameState.activePlayer.balance >= landmark.cost) {
                            engine.buildLandmark(landmark)
                        }
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            // ПРАВАЯ КОЛОНКА: Все игроки
            Column(modifier = Modifier.weight(1f).border(1.dp, Color.LightGray).padding(8.dp)) {
                Text("Города игроков", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Divider(Modifier.padding(vertical = 8.dp))

                LazyColumn {
                    items(gameState.players) { player ->
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

        Spacer(Modifier.height(16.dp))

        // НИЖНЯЯ ПАНЕЛЬ: Действия
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { engine.rollDice() },
                enabled = gameState.lastDiceRoll == 0,
                modifier = Modifier.height(50.dp).weight(1f)
            ) { Text("1. БРОСИТЬ КУБИК 🎲") }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = { engine.passTurn() },
                enabled = gameState.lastDiceRoll > 0, // Передать ход можно только после броска
                modifier = Modifier.height(50.dp).weight(1f),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFCDD2))
            ) { Text("0. ЗАВЕРШИТЬ ХОД (Ничего не покупать)") }

            Spacer(Modifier.width(16.dp))

            OutlinedButton(onClick = {
                engine.abortGame()
                onExitGame()
            }, modifier = Modifier.height(50.dp)) { Text("Сдаться / Выход") }
        }
    }
}

// Вспомогательные компоненты для отрисовки карточек покупок
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