package domain

// Контекст хода: кубик, кто бросал, кто вообще играет
data class TurnContext(
    val diceRoll: Int,
    val activePlayer: Player,
    val allPlayers: List<Player>
)

// Общий интерфейс для всех эффектов
interface IncomeStrategy {
    // Метод рассчитывает доход/штраф.
    // owner - владелец этой карточки
    fun calculateIncome(context: TurnContext, owner: Player)
}

// --- Примеры конкретных стратегий (эффектов карт) ---

// Синие карты (Пшеничное поле, Ферма): Банк платит 1 монету в ЛЮБОЙ ход
class BlueIncomeStrategy(private val amount: Int) : IncomeStrategy {
    override fun calculateIncome(context: TurnContext, owner: Player) {
        owner.addCoins(amount)
        println("Синяя карта сработала! Игрок ${owner.name} получил $amount монет из Банка.")
    }
}

// Зеленые карты (Пекарня): Банк платит монеты ТОЛЬКО в твой ход
class GreenIncomeStrategy(private val amount: Int) : IncomeStrategy {
    override fun calculateIncome(context: TurnContext, owner: Player) {
        if (context.activePlayer == owner) {
            owner.addCoins(amount)
            println("Зеленая карта сработала! Игрок ${owner.name} получил $amount монет в свой ход.")
        }
    }
}