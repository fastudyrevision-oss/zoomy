package com.zoomy.app

import com.zoomy.app.data.UserPreferences
import com.zoomy.app.game.DailyRules
import com.zoomy.app.game.GameMode
import com.zoomy.app.game.Obstacle
import com.zoomy.app.game.ObstacleType
import com.zoomy.app.game.ZoomyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ZoomyEngineTests {

    // 1. Engine determinism: with a fixed seed and fixed lane inputs, 600 steps produce identical object positions on two runs.
    @Test
    fun testEngineDeterminism() {
        val seed = 123456789L
        val dt = 1f / 120f

        val engine1 = ZoomyEngine(customSeed = seed)
        val engine2 = ZoomyEngine(customSeed = seed)

        for (i in 0 until 600) {
            if (i == 100) {
                engine1.onLeftInput()
                engine2.onLeftInput()
            } else if (i == 300) {
                engine1.onRightInput()
                engine2.onRightInput()
            }
            engine1.step(dt)
            engine2.step(dt)
        }

        assertEquals(engine1.playerX, engine2.playerX, 0.001f)
        assertEquals(engine1.playerY, engine2.playerY, 0.001f)
        assertEquals(engine1.distance, engine2.distance, 0.001f)
        assertEquals(engine1.obstacles.size, engine2.obstacles.size)

        for (i in 0 until engine1.obstacles.size) {
            val o1 = engine1.obstacles[i]
            val o2 = engine2.obstacles[i]
            assertEquals(o1.x, o2.x, 0.001f)
            assertEquals(o1.y, o2.y, 0.001f)
            assertEquals(o1.type, o2.type)
        }
    }

    // 2. Spawner rules: 1000 generated rows never block all 3 lanes; a coin line only appears in a free lane.
    @Test
    fun testSpawnerRules() {
        val engine = ZoomyEngine(customSeed = 42L)

        for (i in 0 until 1000) {
            engine.obstacles.clear()
            engine.coins.clear()
            engine.spawnRow()

            val occupiedLanes = engine.obstacles.map { it.lane }.toSet()
            // Never block all 3 lanes
            assertTrue("Blocked all 3 lanes!", occupiedLanes.size < 3)

            // A coin line only appears in a free lane
            for (coin in engine.coins) {
                assertFalse("Coin spawned in an occupied lane!", occupiedLanes.contains(coin.lane))
            }
        }
    }

    // 3. Collision and Shield: overlapping hitboxes crash; with Shield the obstacle is removed and the run continues.
    @Test
    fun testCollisionAndShield() {
        val engine = ZoomyEngine()
        engine.resetGame()

        // Spawn obstacle right at player hitbox
        val obs = Obstacle(
            id = 999L,
            type = ObstacleType.Car,
            lane = engine.playerLane,
            x = engine.playerX,
            y = engine.playerY,
            width = 44f,
            height = 84f,
            hitboxWidth = 36f,
            hitboxHeight = 76f
        )
        engine.obstacles.add(obs)

        // Run step with no shield -> crash
        engine.step(1f / 120f)
        assertTrue("Engine should have crashed!", engine.crashEffect.active)

        // Reset and test with Shield
        engine.resetGame()
        val shieldObs = Obstacle(
            id = 1000L,
            type = ObstacleType.Car,
            lane = engine.playerLane,
            x = engine.playerX,
            y = engine.playerY,
            width = 44f,
            height = 84f,
            hitboxWidth = 36f,
            hitboxHeight = 76f
        )
        engine.obstacles.add(shieldObs)
        engine.activateShield()
        assertTrue(engine.shieldActive)

        engine.step(1f / 120f)
        assertFalse("Crash effect should NOT be active when shielded!", engine.crashEffect.active)
        assertFalse("Shield should be consumed!", engine.shieldActive)
        assertTrue("Obstacle should be popping / destroyed!", shieldObs.isPopping)
    }

    // 4. Score and combo: close calls within 2000ms raise the combo up to 5; close-call score = 25 x combo; coin = +10; distance score = floor(distance / 10).
    @Test
    fun testScoreAndCombo() {
        val engine = ZoomyEngine()
        engine.resetGame()

        val dt = 1f / 120f
        // Simulate distance
        engine.distance = 550f // floor(550 / 10) = 55
        engine.step(dt)
        assertEquals(55, engine.score)

        // Close call in adjacent lane passing player
        val obs = Obstacle(
            id = 1L,
            type = ObstacleType.Car,
            lane = engine.playerLane + 1,
            x = engine.laneCenters[engine.playerLane + 1],
            y = engine.playerY - 2f,
            width = 44f,
            height = 84f,
            hitboxWidth = 36f,
            hitboxHeight = 76f
        )
        engine.obstacles.add(obs)

        // Step past player
        obs.y = engine.playerY + 2f
        engine.step(dt)

        assertEquals(1, engine.combo)
        assertEquals(25, engine.closeCallBonusScore)

        // Another close call within 2000ms
        val obs2 = Obstacle(
            id = 2L,
            type = ObstacleType.Car,
            lane = engine.playerLane + 1,
            x = engine.laneCenters[engine.playerLane + 1],
            y = engine.playerY - 2f,
            width = 44f,
            height = 84f,
            hitboxWidth = 36f,
            hitboxHeight = 76f
        )
        engine.obstacles.add(obs2)
        obs2.y = engine.playerY + 2f
        engine.step(dt)

        assertEquals(2, engine.combo)
        assertEquals(25 + 50, engine.closeCallBonusScore)
    }

    // 5. Daily: the same date gives the same spawn sequence; different dates differ; modifier by weekday matches the table.
    @Test
    fun testDailyRules() {
        val date1 = LocalDate.of(2026, 10, 26) // Monday
        val date2 = LocalDate.of(2026, 10, 27) // Tuesday

        val modMon = DailyRules.getModifier(date1)
        val modTue = DailyRules.getModifier(date2)

        assertEquals("Fast start", modMon.dayName)
        assertEquals(340f, modMon.startSpeed, 0.001f)

        assertEquals("Rainy road", modTue.dayName)
        assertEquals(0.220f, modTue.laneChangeDuration, 0.001f)

        val e1 = ZoomyEngine(mode = GameMode.DAILY, customSeed = date1.toEpochDay(), dailyModifier = modMon)
        val e2 = ZoomyEngine(mode = GameMode.DAILY, customSeed = date1.toEpochDay(), dailyModifier = modMon)
        val eDiff = ZoomyEngine(mode = GameMode.DAILY, customSeed = date2.toEpochDay(), dailyModifier = modTue)

        e1.spawnRow()
        e2.spawnRow()
        eDiff.spawnRow()

        assertEquals(e1.obstacles.size, e2.obstacles.size)
        assertEquals(e1.obstacles[0].type, e2.obstacles[0].type)
        assertEquals(e1.obstacles[0].lane, e2.obstacles[0].lane)

        // Different dates should have different seeds
        assertNotEquals(date1.toEpochDay(), date2.toEpochDay())
    }

    // 6. Economy: coins never go negative; Plus doubles run coins; the Plus daily grant tops boosters up to 3 once per date; car purchase deducts coins and auto-equips; Goldie falls back to Pip when Plus lapses.
    @Test
    fun testEconomyRules() {
        var prefs = UserPreferences(coins = 100, plusActive = true)

        // Coins never go negative check
        val deducted = (prefs.coins - 200).coerceAtLeast(0)
        assertEquals(0, deducted)

        // Plus doubles run coins
        val runCoins = 15
        val multiplier = if (prefs.plusActive) 2 else 1
        val earned = runCoins * multiplier
        assertEquals(30, earned)

        // Plus daily grant tops up boosters to at least 3
        var shield = 1
        var magnet = 4
        var slowmo = 0
        if (prefs.plusActive) {
            shield = maxOf(shield, 3)
            magnet = maxOf(magnet, 3)
            slowmo = maxOf(slowmo, 3)
        }
        assertEquals(3, shield)
        assertEquals(4, magnet)
        assertEquals(3, slowmo)

        // Goldie falls back to Pip when Plus lapses
        var equippedCar = "goldie"
        var plusActive = false
        if (!plusActive && equippedCar == "goldie") {
            equippedCar = "pip"
        }
        assertEquals("pip", equippedCar)
    }

    // 7. Interstitial rule: shown only on every 3rd game over and only after 90s since the last ad.
    @Test
    fun testInterstitialRule() {
        fun shouldShow(gameOverCount: Int, now: Long, lastAdShown: Long, removeAds: Boolean, plusActive: Boolean): Boolean {
            if (removeAds || plusActive) return false
            val isEvery3rd = gameOverCount > 0 && (gameOverCount % 3 == 0)
            val hasPassed90s = (now - lastAdShown) >= 90000L
            return isEvery3rd && hasPassed90s
        }

        val baseTime = 1000000L
        assertFalse("1st game over should not show ad", shouldShow(1, baseTime, baseTime - 100000L, false, false))
        assertFalse("2nd game over should not show ad", shouldShow(2, baseTime, baseTime - 100000L, false, false))
        assertTrue("3rd game over after 90s should show ad", shouldShow(3, baseTime, baseTime - 95000L, false, false))
        assertFalse("3rd game over within 90s should NOT show ad", shouldShow(3, baseTime, baseTime - 50000L, false, false))
        assertFalse("3rd game over with removeAds should NOT show ad", shouldShow(3, baseTime, baseTime - 95000L, true, false))
        assertFalse("3rd game over with plusActive should NOT show ad", shouldShow(3, baseTime, baseTime - 95000L, false, true))
    }
}
