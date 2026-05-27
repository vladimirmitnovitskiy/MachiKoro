package application

// Интерфейс генератора случайных чисел (виртуализация из твоего ТЗ)
interface Dice {
    fun roll(): Int
}

// Реальный кубик для обычной игры
class RandomDice : Dice {
    override fun roll(): Int = (1..6).random()
}

// Подставной кубик для тестов (выдает заранее заданные числа)
class FakeDice(vararg val fixedRolls: Int) : Dice {
    private var index = 0
    override fun roll(): Int {
        val result = fixedRolls[index % fixedRolls.size]
        index++
        return result
    }
}