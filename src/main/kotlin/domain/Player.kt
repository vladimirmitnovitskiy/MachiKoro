package domain

import java.util.UUID

class Player(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    initialBalance: Int = 3
) {
    var balance: Int = initialBalance
        private set

    private val _establishments = mutableListOf<Establishment>()
    val establishments: List<Establishment> get() = _establishments.toList()

    private val _landmarks = mutableListOf<Landmark>()
    val landmarks: List<Landmark> get() = _landmarks.toList()

    fun addCoins(amount: Int) {
        if (amount > 0) balance += amount
    }

    fun deductCoins(amount: Int): Int {
        val actualDeduction = if (balance >= amount) amount else balance
        balance -= actualDeduction
        return actualDeduction
    }

    fun buyEstablishment(card: Establishment) {
        if (balance >= card.cost) {
            deductCoins(card.cost)
            _establishments.add(card)
        }
    }

    fun canBuildLandmark(landmark: Landmark): Boolean {
        val alreadyBuilt = _landmarks.any { it.name == landmark.name && it.isBuilt }
        return !alreadyBuilt && balance >= landmark.cost
    }

    fun buildLandmark(landmark: Landmark) {
        if (canBuildLandmark(landmark)) {
            deductCoins(landmark.cost)
            val existing = _landmarks.find { it.name == landmark.name }
            if (existing != null) {
                existing.isBuilt = true
            } else {
                landmark.isBuilt = true
                _landmarks.add(landmark)
            }
        }
    }

    fun hasWon(): Boolean {
        return _landmarks.count { it.isBuilt } >= 4
    }

    fun setInitialLandmarks(initial: List<Landmark>) {
        _landmarks.clear()
        _landmarks.addAll(initial)
    }
}