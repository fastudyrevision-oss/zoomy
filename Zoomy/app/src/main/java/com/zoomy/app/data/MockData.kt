package com.zoomy.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// MOCK DATA (MockData.kt)
// Coins 1,250; best score 12,480; boosters Shield 2, Magnet 1, Slow-mo 0;
// garage owned cars 1-5 with Pip equipped; shop, plans as written.

data class CarItem(
    val id: Int, // 1 to 8
    val name: String,
    val price: Int = 0,
    val isPlusOnly: Boolean = false
)

data class BoosterState(
    var shieldCount: Int = 2,
    var magnetCount: Int = 1,
    var slowMoCount: Int = 0
)

object MockData {
    var coins by mutableIntStateOf(1250)
    var bestScore by mutableIntStateOf(12480)

    val boosterState = BoosterState()

    var equippedCarId by mutableIntStateOf(1) // Pip equipped
    val ownedCarIds = mutableStateListOf(1, 2, 3, 4, 5) // Pip, Bumble, Minty, Blueberry, Grapey

    val cars = listOf(
        CarItem(1, "Pip", 0),
        CarItem(2, "Bumble", 0),
        CarItem(3, "Minty", 0),
        CarItem(4, "Blueberry", 0),
        CarItem(5, "Grapey", 0),
        CarItem(6, "Orangy", 4000),
        CarItem(7, "Pinky", 6000),
        CarItem(8, "Goldie", 0, isPlusOnly = true)
    )

    fun equipCar(id: Int) {
        if (ownedCarIds.contains(id)) {
            equippedCarId = id
        }
    }

    fun updateScore(newScore: Int) {
        if (newScore > bestScore) {
            bestScore = newScore
        }
    }

    fun addCoins(amount: Int) {
        coins += amount
    }
}
