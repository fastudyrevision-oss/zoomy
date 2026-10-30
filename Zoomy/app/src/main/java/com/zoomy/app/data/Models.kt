package com.zoomy.app.data

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val coins: Int = 1250,
    val bestScore: Int = 0,
    val bestDistanceM: Int = 0,
    val totalCoinsEarned: Int = 0,
    val gamesPlayed: Int = 0,
    val boosterShield: Int = 2,
    val boosterMagnet: Int = 1,
    val boosterSlowmo: Int = 0,
    val soundOn: Boolean = true,
    val hapticsOn: Boolean = true,
    val removeAds: Boolean = false,
    val plusActive: Boolean = false,
    val ownedCars: Set<String> = setOf("pip", "bumble", "minty", "blueberry", "grapey"),
    val equippedCar: String = "pip",
    val lastPlusGrantDate: String = "",
    val dailyClaimedDate: String = "",
    val dailyBestDate: String = "",
    val dailyBest: Int = 0,
    val gameOverCount: Int = 0,
    val lastAdShownEpochMs: Long = 0L,
    val processedTokens: Set<String> = emptySet()
)

fun carIdToName(carId: String): String = when (carId.lowercase()) {
    "pip" -> "Pip"
    "bumble" -> "Bumble"
    "minty" -> "Minty"
    "blueberry" -> "Blueberry"
    "grapey" -> "Grapey"
    "orangy" -> "Orangy"
    "pinky" -> "Pinky"
    "goldie" -> "Goldie"
    else -> "Pip"
}

fun carNameToId(name: String): String = name.lowercase()

fun carIndexToId(index: Int): String = when (index) {
    1 -> "pip"
    2 -> "bumble"
    3 -> "minty"
    4 -> "blueberry"
    5 -> "grapey"
    6 -> "orangy"
    7 -> "pinky"
    8 -> "goldie"
    else -> "pip"
}

fun carIdToIndex(id: String): Int = when (id.lowercase()) {
    "pip" -> 1
    "bumble" -> 2
    "minty" -> 3
    "blueberry" -> 4
    "grapey" -> 5
    "orangy" -> 6
    "pinky" -> 7
    "goldie" -> 8
    else -> 1
}

fun getCarPrice(id: String): Int = when (id.lowercase()) {
    "bumble" -> 500
    "minty" -> 900
    "blueberry" -> 1500
    "grapey" -> 2500
    "orangy" -> 4000
    "pinky" -> 6000
    else -> 0
}
