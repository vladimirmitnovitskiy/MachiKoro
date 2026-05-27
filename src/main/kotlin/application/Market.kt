package application

import domain.Establishment

class Market(initialCards: List<Establishment>) {
    // Внутренний изменяемый список, наружу отдаем неизменяемый (инкапсуляция)
    private val _availableCards = initialCards.toMutableList()
    val availableCards: List<Establishment> get() = _availableCards.toList()

    fun removeCard(card: Establishment) {
        _availableCards.remove(card)
    }

    // В будущем здесь можно добавить логику пополнения рынка из колоды
    fun replenishMarket() {
        // Пока оставим пустым
    }
}