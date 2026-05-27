package application

import domain.Establishment

class Market(initialDeck: List<Establishment>) {

    // Закрытая колода (перемешиваем при создании)
    private val deck = initialDeck.toMutableList().apply { shuffle() }

    // Открытые карты на столе (доступные для покупки)
    private val _availableCards = mutableListOf<Establishment>()
    val availableCards: List<Establishment> get() = _availableCards.toList()

    init {
        // При создании игры сразу выкладываем стартовые карты на стол
        replenishMarket()
    }

    fun removeCard(card: Establishment) {
        _availableCards.remove(card)
        // Срабатывает автоматическое пополнение резерва!
        replenishMarket()
    }

    // Логика автоматического пополнения
    fun replenishMarket() {
        // Допустим, на столе всегда должно лежать 5 карт (или меньше, если колода опустела)
        val maxMarketSize = 5

        while (_availableCards.size < maxMarketSize && deck.isNotEmpty()) {
            val drawnCard = deck.removeAt(0) // Берем верхнюю карту из колоды
            _availableCards.add(drawnCard)   // Кладем на стол
        }
    }
}