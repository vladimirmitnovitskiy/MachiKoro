package application

import domain.*

interface IGameFactory {
    fun createGame(players: List<Player>): IGameEngine
}

class ClassicGameFactory : IGameFactory {
    override fun createGame(players: List<Player>): IGameEngine {
        // Каждому игроку на старте даем Пшеничное поле и Пекарню (по правилам Мачи Коро)
        players.forEach { player ->
            player.establishments.clear() // очищаем на всякий случай
            player.establishments.add(createWheatField())
            player.establishments.add(createBakery())

            // Также добавляем им непостроенные Достопримечательности (цель игры - 4 шт)
            player.landmarks.clear()
            player.landmarks.add(Landmark("Вокзал", 4))
            player.landmarks.add(Landmark("Торговый Центр", 10))
            player.landmarks.add(Landmark("Парк Развлечений", 16))
            player.landmarks.add(Landmark("Радиовышка", 22))
        }

        // Генерируем общий рынок (по несколько копий каждой карты)
        val deck = mutableListOf<Establishment>()
        repeat(6) { deck.add(createWheatField()) }
        repeat(6) { deck.add(createBakery()) }
        repeat(6) { deck.add(createCafe()) }
        repeat(3) { deck.add(createStadium()) }

        val market = Market(deck)

        return MachiKoroEngine(players, market, RandomDice())
    }

    // Вспомогательные методы для создания карт
    private fun createWheatField() = Establishment(
        "Пшеничное поле", 1, listOf(1), CardColor.BLUE, BlueIncomeStrategy(1)
    )
    private fun createBakery() = Establishment(
        "Пекарня", 1, listOf(2, 3), CardColor.GREEN, GreenIncomeStrategy(1)
    )
    private fun createCafe() = Establishment(
        "Кафе", 2, listOf(3), CardColor.RED, RedIncomeStrategy(1)
    )
    private fun createStadium() = Establishment(
        "Стадион", 6, listOf(6), CardColor.PURPLE, PurpleIncomeStrategy(2)
    )
}