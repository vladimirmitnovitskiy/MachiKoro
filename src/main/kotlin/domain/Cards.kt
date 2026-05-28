package domain

// Цвета карт определяют порядок и правила их срабатывания
enum class CardColor(val priority: Int) {
    RED(1),    // Штрафы (срабатывают первыми)
    GREEN(2),  // Доходы в свой ход
    BLUE(2),   // Доходы в любой ход
    PURPLE(3)  // Спец-эффекты (срабатывают последними)
}

// Базовый класс для всех карт
abstract class Card(
    val name: String,
    val cost: Int
)

// Предприятие (Обычные карты, которые активируются кубиком)
class Establishment(
    name: String,
    cost: Int,
    val activationNumbers: List<Int>, // Числа на кубике (например: [2, 3])
    val color: CardColor,
    val strategy: IncomeStrategy      // Паттерн Стратегия для эффекта карты
) : Card(name, cost)

// Достопримечательность
class Landmark(
    name: String,
    cost: Int,
    isBuilt: Boolean = false
) : Card(name, cost) {
    var isBuilt: Boolean = isBuilt
        internal set
}