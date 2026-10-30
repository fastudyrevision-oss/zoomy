package com.zoomy.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zoomy_prefs")

class PrefsRepository(
    private val context: Context,
    private val externalScope: CoroutineScope
) {
    private val dataStore = context.dataStore

    object Keys {
        val COINS = intPreferencesKey("coins")
        val BEST_SCORE = intPreferencesKey("bestScore")
        val BEST_DISTANCE_M = intPreferencesKey("bestDistanceM")
        val TOTAL_COINS_EARNED = intPreferencesKey("totalCoinsEarned")
        val GAMES_PLAYED = intPreferencesKey("gamesPlayed")

        val BOOSTER_SHIELD = intPreferencesKey("boosterShield")
        val BOOSTER_MAGNET = intPreferencesKey("boosterMagnet")
        val BOOSTER_SLOWMO = intPreferencesKey("boosterSlowmo")

        val SOUND_ON = booleanPreferencesKey("soundOn")
        val HAPTICS_ON = booleanPreferencesKey("hapticsOn")

        val REMOVE_ADS = booleanPreferencesKey("removeAds")
        val PLUS_ACTIVE = booleanPreferencesKey("plusActive")

        val OWNED_CARS = stringSetPreferencesKey("ownedCars")
        val EQUIPPED_CAR = stringPreferencesKey("equippedCar")

        val LAST_PLUS_GRANT_DATE = stringPreferencesKey("lastPlusGrantDate")
        val DAILY_CLAIMED_DATE = stringPreferencesKey("dailyClaimedDate")
        val DAILY_BEST_DATE = stringPreferencesKey("dailyBestDate")
        val DAILY_BEST = intPreferencesKey("dailyBest")

        val GAME_OVER_COUNT = intPreferencesKey("gameOverCount")
        val LAST_AD_SHOWN_EPOCH_MS = longPreferencesKey("lastAdShownEpochMs")
        val PROCESSED_TOKENS = stringSetPreferencesKey("processedTokens")
    }

    val prefsFlow: Flow<UserPreferences> = dataStore.data.map { p ->
        UserPreferences(
            coins = p[Keys.COINS] ?: 1250,
            bestScore = p[Keys.BEST_SCORE] ?: 0,
            bestDistanceM = p[Keys.BEST_DISTANCE_M] ?: 0,
            totalCoinsEarned = p[Keys.TOTAL_COINS_EARNED] ?: 0,
            gamesPlayed = p[Keys.GAMES_PLAYED] ?: 0,
            boosterShield = p[Keys.BOOSTER_SHIELD] ?: 2,
            boosterMagnet = p[Keys.BOOSTER_MAGNET] ?: 1,
            boosterSlowmo = p[Keys.BOOSTER_SLOWMO] ?: 0,
            soundOn = p[Keys.SOUND_ON] ?: true,
            hapticsOn = p[Keys.HAPTICS_ON] ?: true,
            removeAds = p[Keys.REMOVE_ADS] ?: false,
            plusActive = p[Keys.PLUS_ACTIVE] ?: false,
            ownedCars = p[Keys.OWNED_CARS] ?: setOf("pip", "bumble", "minty", "blueberry", "grapey"),
            equippedCar = p[Keys.EQUIPPED_CAR] ?: "pip",
            lastPlusGrantDate = p[Keys.LAST_PLUS_GRANT_DATE] ?: "",
            dailyClaimedDate = p[Keys.DAILY_CLAIMED_DATE] ?: "",
            dailyBestDate = p[Keys.DAILY_BEST_DATE] ?: "",
            dailyBest = p[Keys.DAILY_BEST] ?: 0,
            gameOverCount = p[Keys.GAME_OVER_COUNT] ?: 0,
            lastAdShownEpochMs = p[Keys.LAST_AD_SHOWN_EPOCH_MS] ?: 0L,
            processedTokens = p[Keys.PROCESSED_TOKENS] ?: emptySet()
        )
    }

    suspend fun updateSound(enabled: Boolean) {
        dataStore.edit { it[Keys.SOUND_ON] = enabled }
    }

    suspend fun updateHaptics(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS_ON] = enabled }
    }

    suspend fun addCoins(amount: Int) {
        if (amount == 0) return
        dataStore.edit { p ->
            val cur = p[Keys.COINS] ?: 1250
            p[Keys.COINS] = (cur + amount).coerceAtLeast(0)
            if (amount > 0) {
                val total = p[Keys.TOTAL_COINS_EARNED] ?: 0
                p[Keys.TOTAL_COINS_EARNED] = total + amount
            }
        }
    }

    suspend fun deductCoins(amount: Int): Boolean {
        var success = false
        dataStore.edit { p ->
            val cur = p[Keys.COINS] ?: 1250
            if (cur >= amount) {
                p[Keys.COINS] = (cur - amount).coerceAtLeast(0)
                success = true
            }
        }
        return success
    }

    suspend fun updateBestScore(newScore: Int, newDistanceM: Int) {
        dataStore.edit { p ->
            val currentBest = p[Keys.BEST_SCORE] ?: 0
            if (newScore > currentBest) {
                p[Keys.BEST_SCORE] = newScore
            }
            val currentDist = p[Keys.BEST_DISTANCE_M] ?: 0
            if (newDistanceM > currentDist) {
                p[Keys.BEST_DISTANCE_M] = newDistanceM
            }
        }
    }

    suspend fun recordGameOver() {
        dataStore.edit { p ->
            val games = p[Keys.GAMES_PLAYED] ?: 0
            p[Keys.GAMES_PLAYED] = games + 1
            val count = p[Keys.GAME_OVER_COUNT] ?: 0
            p[Keys.GAME_OVER_COUNT] = count + 1
        }
    }

    fun recordAdShown() {
        externalScope.launch(Dispatchers.IO) {
            dataStore.edit { p ->
                p[Keys.LAST_AD_SHOWN_EPOCH_MS] = System.currentTimeMillis()
            }
        }
    }

    suspend fun setShieldCount(count: Int) {
        dataStore.edit { it[Keys.BOOSTER_SHIELD] = count.coerceAtLeast(0) }
    }

    suspend fun setMagnetCount(count: Int) {
        dataStore.edit { it[Keys.BOOSTER_MAGNET] = count.coerceAtLeast(0) }
    }

    suspend fun setSlowMoCount(count: Int) {
        dataStore.edit { it[Keys.BOOSTER_SLOWMO] = count.coerceAtLeast(0) }
    }

    suspend fun addBooster(type: String, count: Int = 1) {
        dataStore.edit { p ->
            when (type.lowercase()) {
                "shield" -> {
                    val c = p[Keys.BOOSTER_SHIELD] ?: 2
                    p[Keys.BOOSTER_SHIELD] = (c + count).coerceAtLeast(0)
                }
                "magnet" -> {
                    val c = p[Keys.BOOSTER_MAGNET] ?: 1
                    p[Keys.BOOSTER_MAGNET] = (c + count).coerceAtLeast(0)
                }
                "slowmo" -> {
                    val c = p[Keys.BOOSTER_SLOWMO] ?: 0
                    p[Keys.BOOSTER_SLOWMO] = (c + count).coerceAtLeast(0)
                }
            }
        }
    }

    suspend fun equipCar(carId: String) {
        dataStore.edit { p ->
            val owned = p[Keys.OWNED_CARS] ?: setOf("pip")
            val isPlus = p[Keys.PLUS_ACTIVE] ?: false
            if (owned.contains(carId) || (carId == "goldie" && isPlus)) {
                p[Keys.EQUIPPED_CAR] = carId
            }
        }
    }

    suspend fun buyCar(carId: String, price: Int): Boolean {
        var bought = false
        dataStore.edit { p ->
            val curCoins = p[Keys.COINS] ?: 1250
            val owned = (p[Keys.OWNED_CARS] ?: setOf("pip")).toMutableSet()
            if (curCoins >= price && !owned.contains(carId)) {
                p[Keys.COINS] = curCoins - price
                owned.add(carId)
                p[Keys.OWNED_CARS] = owned
                p[Keys.EQUIPPED_CAR] = carId
                bought = true
            }
        }
        return bought
    }

    suspend fun setRemoveAds(active: Boolean) {
        dataStore.edit { it[Keys.REMOVE_ADS] = active }
    }

    suspend fun setPlusActive(active: Boolean) {
        dataStore.edit { p ->
            p[Keys.PLUS_ACTIVE] = active
            if (!active) {
                // If Plus lapses and Goldie was equipped, fall back to pip
                val equipped = p[Keys.EQUIPPED_CAR] ?: "pip"
                if (equipped == "goldie") {
                    p[Keys.EQUIPPED_CAR] = "pip"
                }
            }
        }
    }

    suspend fun checkAndApplyPlusDailyGrant(todayDateStr: String) {
        dataStore.edit { p ->
            val isPlus = p[Keys.PLUS_ACTIVE] ?: false
            val lastDate = p[Keys.LAST_PLUS_GRANT_DATE] ?: ""
            if (isPlus && lastDate != todayDateStr) {
                val s = p[Keys.BOOSTER_SHIELD] ?: 2
                val m = p[Keys.BOOSTER_MAGNET] ?: 1
                val sl = p[Keys.BOOSTER_SLOWMO] ?: 0
                p[Keys.BOOSTER_SHIELD] = maxOf(s, 3)
                p[Keys.BOOSTER_MAGNET] = maxOf(m, 3)
                p[Keys.BOOSTER_SLOWMO] = maxOf(sl, 3)
                p[Keys.LAST_PLUS_GRANT_DATE] = todayDateStr
            }
        }
    }

    suspend fun claimDailyReward(todayDateStr: String, coinsReward: Int): Boolean {
        var claimed = false
        dataStore.edit { p ->
            val lastClaimed = p[Keys.DAILY_CLAIMED_DATE] ?: ""
            if (lastClaimed != todayDateStr) {
                val curCoins = p[Keys.COINS] ?: 1250
                p[Keys.COINS] = curCoins + coinsReward
                val total = p[Keys.TOTAL_COINS_EARNED] ?: 0
                p[Keys.TOTAL_COINS_EARNED] = total + coinsReward
                p[Keys.DAILY_CLAIMED_DATE] = todayDateStr
                claimed = true
            }
        }
        return claimed
    }

    suspend fun recordDailyScore(todayDateStr: String, score: Int) {
        dataStore.edit { p ->
            val lastDate = p[Keys.DAILY_BEST_DATE] ?: ""
            val currentBest = if (lastDate == todayDateStr) p[Keys.DAILY_BEST] ?: 0 else 0
            if (score > currentBest) {
                p[Keys.DAILY_BEST] = score
                p[Keys.DAILY_BEST_DATE] = todayDateStr
            }
        }
    }

    suspend fun recordProcessedToken(token: String): Boolean {
        var wasNew = false
        dataStore.edit { p ->
            val tokens = (p[Keys.PROCESSED_TOKENS] ?: emptySet()).toMutableSet()
            if (!tokens.contains(token)) {
                tokens.add(token)
                p[Keys.PROCESSED_TOKENS] = tokens
                wasNew = true
            }
        }
        return wasNew
    }
}
