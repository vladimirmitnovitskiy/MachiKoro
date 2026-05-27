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

// Красные карты (Кафе, Ресторан): Забирают монеты у бросившего кубик
class RedIncomeStrategy(private val amount: Int) : IncomeStrategy {
    override fun calculateIncome(context: TurnContext, owner: Player) {
        // Красная карта НЕ работает, если сейчас твой ход
        if (context.activePlayer == owner) return

        // Активный игрок платит владельцу карты.
        // Если у активного игрока меньше денег, чем amount, он отдаст всё что есть
        val actuallyPaid = context.activePlayer.deductCoins(amount)
        owner.addCoins(actuallyPaid)

        println("☕ Красная карта сработала! Игрок ${context.activePlayer.name} заплатил $actuallyPaid монет игроку ${owner.name}.")
    }
}

// Фиолетовые карты (Крупный бизнес): срабатывают последними, только в свой ход.
// Пример: Стадион (берет по 2 монеты у каждого ДРУГОГО игрока)
class PurpleIncomeStrategy(private val amountFromEach: Int) : IncomeStrategy {
    override fun calculateIncome(context: TurnContext, owner: Player) {
        // Фиолетовые карты работают ТОЛЬКО если сейчас твой ход
        if (context.activePlayer != owner) return

        var totalCollected = 0
        // Проходим по всем игрокам за столом
        for (otherPlayer in context.allPlayers) {
            if (otherPlayer != owner) {
                // Пытаемся забрать монеты (если у них меньше, заберем сколько есть)
                val taken = otherPlayer.deductCoins(amountFromEach)
                totalCollected += taken
                if (taken > 0) {
                    println("🟣 Фиолетовая карта! Игрок ${owner.name} забрал $taken монет у ${otherPlayer.name}.")
                }
            }
        }
        // Отдаем всё собранное владельцу карты
        owner.addCoins(totalCollected)
    }
}