package com.zoomy.app.game

import androidx.compose.ui.geometry.Rect
import java.util.Random
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class ZoomyEngine(
    val isAttractMode: Boolean = false,
    var playerColorIndex: Int = 1,
    var mode: GameMode = GameMode.NORMAL,
    var customSeed: Long? = null,
    var dailyModifier: DailyModifier = DailyModifier("Normal", "Normal")
) {
    val roadWidth = if (isAttractMode) 204f else 340f
    val roadHeight = if (isAttractMode) 324f else 540f
    val laneWidth = roadWidth / 3f
    val laneCenters = floatArrayOf(
        laneWidth * 0.5f,
        laneWidth * 1.5f,
        laneWidth * 2.5f
    )

    // Player state
    var playerLane: Int = 1
    var playerTargetLane: Int = 1
    var playerX: Float = laneCenters[1]
    var playerStartX: Float = laneCenters[1]
    var playerY: Float = if (isAttractMode) 240f else 420f
    val playerWidth = if (isAttractMode) 35f * 0.6f else 35f
    val playerHeight = if (isAttractMode) 67f * 0.6f else 67f
    val playerHitboxWidth = if (isAttractMode) 28f * 0.6f else 28f
    val playerHitboxHeight = if (isAttractMode) 60f * 0.6f else 60f

    // Lane change transition
    var laneChangeProgress: Float = 1f
    var laneChangeDuration: Float = if (mode == GameMode.DAILY) dailyModifier.laneChangeDuration else 0.140f
    var playerTiltDeg: Float = 0f
    private var queuedInputLaneOffset: Int? = null

    // Engine physics & timers
    var speed: Float = if (isAttractMode) 300f else (if (mode == GameMode.DAILY) dailyModifier.startSpeed else 260f)
    var distance: Float = 0f
    var runCoins: Int = 0
    var closeCallBonusScore: Int = 0
    var score: Int = 0

    // Combo system
    var combo: Int = 1
    var lastCloseCallTimeSec: Float = -100f
    var totalTimeSec: Float = 0f

    // Spawner
    private var random = if (customSeed != null) Random(customSeed!!) else Random()
    private var spawnTimer: Float = if (isAttractMode) 0.2f else 0.6f
    private var nextObjectId: Long = 1L

    // Entities
    val obstacles = mutableListOf<Obstacle>()
    val coins = mutableListOf<GameCoin>()
    val coinSparkles = mutableListOf<CoinSparkle>()
    val floatingTexts = mutableListOf<FloatingComboText>()
    val crashEffect = CrashEffectState()

    // Boosters
    var shieldActive: Boolean = false
    var magnetTimerSec: Float = 0f
    var slowMoTimerSec: Float = 0f
    var invulnerableTimerSec: Float = 0f

    // Daily target reached flag
    var dailyTargetReached: Boolean = false

    // Attract mode AI
    private var attractModeLaneTimer: Float = 1.2f

    // Game lifecycle state
    var isPaused: Boolean = false
    var isGameOver: Boolean = false
    var gameOverTriggered: Boolean = false

    // Callbacks
    var onGameOver: (() -> Unit)? = null
    var onHapticCrash: (() -> Unit)? = null
    var onHapticCloseCall: (() -> Unit)? = null
    var onCoinPickup: (() -> Unit)? = null
    var onSwipeSound: (() -> Unit)? = null
    var onShieldActivated: (() -> Unit)? = null
    var onShieldBroken: (() -> Unit)? = null
    var onMagnetActivated: (() -> Unit)? = null
    var onSlowMoActivated: (() -> Unit)? = null
    var onDailyTargetReached: (() -> Unit)? = null
    var onCoinPop: (() -> Unit)? = null
    var onComboScorePillPop: (() -> Unit)? = null

    // Accumulator for 1/120s
    private var timeAccumulator: Float = 0f
    private val DT = 1f / 120f

    fun onLeftInput() {
        if (isGameOver || isPaused || isAttractMode) return
        if (laneChangeProgress < 1f) {
            if (queuedInputLaneOffset == null && playerTargetLane > 0) {
                queuedInputLaneOffset = -1
            }
            return
        }
        if (playerLane > 0) {
            initiateLaneChange(playerLane - 1)
        }
    }

    fun onRightInput() {
        if (isGameOver || isPaused || isAttractMode) return
        if (laneChangeProgress < 1f) {
            if (queuedInputLaneOffset == null && playerTargetLane < 2) {
                queuedInputLaneOffset = 1
            }
            return
        }
        if (playerLane < 2) {
            initiateLaneChange(playerLane + 1)
        }
    }

    private fun initiateLaneChange(newLane: Int) {
        playerStartX = playerX
        playerTargetLane = newLane.coerceIn(0, 2)
        laneChangeProgress = 0f
        if (!isAttractMode) {
            onSwipeSound?.invoke()
        }
    }

    fun activateShield(): Boolean {
        if (isGameOver || isPaused || shieldActive) return false
        shieldActive = true
        onShieldActivated?.invoke()
        return true
    }

    fun activateMagnet(): Boolean {
        if (isGameOver || isPaused || magnetTimerSec > 0f) return false
        magnetTimerSec = 8.0f // 8000ms
        onMagnetActivated?.invoke()
        return true
    }

    fun activateSlowMo(): Boolean {
        if (isGameOver || isPaused || slowMoTimerSec > 0f) return false
        slowMoTimerSec = 4.0f // 4000ms
        onSlowMoActivated?.invoke()
        return true
    }

    fun revive() {
        obstacles.removeAll { it.y >= (roadHeight - 300f) }
        speed = if (mode == GameMode.DAILY) dailyModifier.startSpeed else 260f
        invulnerableTimerSec = 2.0f
        isGameOver = false
        gameOverTriggered = false
        crashEffect.active = false
    }

    fun resetGame() {
        speed = if (mode == GameMode.DAILY) dailyModifier.startSpeed else 260f
        laneChangeDuration = if (mode == GameMode.DAILY) dailyModifier.laneChangeDuration else 0.140f
        distance = 0f
        runCoins = 0
        closeCallBonusScore = 0
        score = 0
        combo = 1
        totalTimeSec = 0f
        lastCloseCallTimeSec = -100f
        playerLane = 1
        playerTargetLane = 1
        playerX = laneCenters[1]
        playerStartX = laneCenters[1]
        laneChangeProgress = 1f
        playerTiltDeg = 0f
        queuedInputLaneOffset = null
        obstacles.clear()
        coins.clear()
        coinSparkles.clear()
        floatingTexts.clear()
        crashEffect.active = false
        isGameOver = false
        gameOverTriggered = false
        shieldActive = false
        magnetTimerSec = 0f
        slowMoTimerSec = 0f
        invulnerableTimerSec = 0f
        dailyTargetReached = false
        spawnTimer = 0.6f
        if (customSeed != null) {
            random = Random(customSeed!!)
        }
    }

    fun update(frameDeltaSec: Float) {
        if (isPaused) return

        timeAccumulator += frameDeltaSec
        var steps = 0
        while (timeAccumulator >= DT && steps < 4) {
            step(DT)
            timeAccumulator -= DT
            steps++
        }
        if (steps >= 4) {
            timeAccumulator = 0f
        }
    }

    fun step(dt: Float) {
        totalTimeSec += dt

        // 1. Crash effect update
        if (crashEffect.active) {
            crashEffect.update(dt)
            if (crashEffect.elapsedSec >= crashEffect.durationSec && !gameOverTriggered) {
                gameOverTriggered = true
                isGameOver = true
                onGameOver?.invoke()
            }
            return
        }

        // 2. Timers
        if (slowMoTimerSec > 0f) {
            slowMoTimerSec = max(0f, slowMoTimerSec - dt)
        }
        if (magnetTimerSec > 0f) {
            magnetTimerSec = max(0f, magnetTimerSec - dt)
        }
        if (invulnerableTimerSec > 0f) {
            invulnerableTimerSec = max(0f, invulnerableTimerSec - dt)
        }

        val effectiveSpeedMultiplier = if (slowMoTimerSec > 0f) 0.6f else 1.0f

        if (!isAttractMode) {
            speed = min(620f, speed + 8f * dt)
        }
        val currentWorldSpeed = speed * effectiveSpeedMultiplier
        distance += currentWorldSpeed * dt

        // 3. Lane change interpolation
        if (laneChangeProgress < 1f) {
            laneChangeProgress += dt / laneChangeDuration
            val p = laneChangeProgress.coerceIn(0f, 1f)
            val easeP = p * p * (3f - 2f * p)
            val targetX = laneCenters[playerTargetLane]
            playerX = playerStartX + (targetX - playerStartX) * easeP

            val movingRight = targetX > playerStartX
            val tiltDirection = if (movingRight) 1f else -1f
            val tiltCurve = kotlin.math.sin(p * Math.PI.toFloat())
            playerTiltDeg = tiltDirection * 8f * tiltCurve

            if (laneChangeProgress >= 1f) {
                playerLane = playerTargetLane
                playerX = targetX
                playerTiltDeg = 0f
                if (queuedInputLaneOffset != null) {
                    val nextLane = (playerLane + queuedInputLaneOffset!!).coerceIn(0, 2)
                    queuedInputLaneOffset = null
                    if (nextLane != playerLane) {
                        initiateLaneChange(nextLane)
                    }
                }
            }
        }

        // 4. Attract Mode AI
        if (isAttractMode) {
            attractModeLaneTimer -= dt
            if (attractModeLaneTimer <= 0f && laneChangeProgress >= 1f) {
                attractModeLaneTimer = 1.2f
                val freeLanes = mutableListOf<Int>()
                for (lane in 0..2) {
                    val hasObstacle = obstacles.any {
                        it.lane == lane && (it.y in (playerY - 200f)..(playerY + 100f))
                    }
                    if (!hasObstacle) freeLanes.add(lane)
                }
                if (freeLanes.isNotEmpty()) {
                    val chosen = freeLanes[random.nextInt(freeLanes.size)]
                    if (chosen != playerLane) {
                        initiateLaneChange(chosen)
                    }
                }
            }
        }

        // 5. Spawner
        spawnTimer -= dt
        val baseSpawnInterval = max(0.55f, 1.2f - speed / 1000f)
        val nextSpawnInterval = if (mode == GameMode.DAILY) {
            baseSpawnInterval * dailyModifier.spawnIntervalMultiplier
        } else {
            baseSpawnInterval
        }
        if (spawnTimer <= 0f) {
            spawnTimer = nextSpawnInterval
            spawnRow()
        }

        // 6. Move obstacles & Close call check
        val playerCenterY = playerY
        val playerHitbox = getPlayerHitbox()

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obs = iterator.next()
            if (obs.isPopping) {
                obs.popProgress += dt / 0.24f
                if (obs.popProgress >= 1f) {
                    iterator.remove()
                    continue
                }
            } else {
                obs.y += currentWorldSpeed * dt

                if (!isAttractMode && !obs.passedPlayer) {
                    val isAdjacent = kotlin.math.abs(obs.lane - playerLane) == 1
                    if (isAdjacent && obs.y >= playerCenterY) {
                        obs.passedPlayer = true
                        handleCloseCall(obs)
                    }
                }

                if (!isAttractMode && invulnerableTimerSec <= 0f && !obs.isPopping) {
                    if (obs.getHitbox().overlaps(playerHitbox)) {
                        if (shieldActive) {
                            shieldActive = false
                            obs.isPopping = true
                            obs.popProgress = 0f
                            onShieldBroken?.invoke()
                            coinSparkles.add(CoinSparkle(nextObjectId++, obs.x, obs.y))
                        } else {
                            crashEffect.start()
                            onHapticCrash?.invoke()
                            return
                        }
                    }
                }

                if (obs.y > roadHeight + 100f) {
                    iterator.remove()
                }
            }
        }

        // 7. Move coins & Magnet attraction
        val coinIterator = coins.iterator()
        while (coinIterator.hasNext()) {
            val coin = coinIterator.next()
            coin.y += currentWorldSpeed * dt

            if (magnetTimerSec > 0f) {
                val dx = playerX - coin.x
                val dy = playerY - coin.y
                val dist = kotlin.math.hypot(dx, dy)
                if (dist <= 140f && dist > 1f) {
                    val stepDist = 600f * dt
                    coin.x += (dx / dist) * stepDist
                    coin.y += (dy / dist) * stepDist
                }
            }

            if (!isAttractMode && !coin.collected && coin.getHitbox().overlaps(playerHitbox)) {
                coin.collected = true
                runCoins++
                score += 10
                onCoinPickup?.invoke()
                onCoinPop?.invoke()
                coinSparkles.add(CoinSparkle(nextObjectId++, coin.x, coin.y))
                coinIterator.remove()
                continue
            }

            if (coin.y > roadHeight + 50f) {
                coinIterator.remove()
            }
        }

        // 8. Update coin sparkles
        val sparkleIterator = coinSparkles.iterator()
        while (sparkleIterator.hasNext()) {
            val sp = sparkleIterator.next()
            sp.ageSec += dt
            if (sp.ageSec >= sp.durationSec) {
                sparkleIterator.remove()
            }
        }

        // 9. Update floating combo texts
        val ftIterator = floatingTexts.iterator()
        while (ftIterator.hasNext()) {
            val ft = ftIterator.next()
            ft.ageSec += dt
            val p = (ft.ageSec / ft.durationSec).coerceIn(0f, 1f)
            ft.currentY = ft.startY - p * 40f
            if (ft.ageSec >= ft.durationSec) {
                ftIterator.remove()
            }
        }

        // 10. Update Score
        if (!isAttractMode) {
            score = floor(distance / 10f).toInt() + 10 * runCoins + closeCallBonusScore

            // Daily target check: 3,000 score
            if (mode == GameMode.DAILY && !dailyTargetReached && score >= 3000) {
                dailyTargetReached = true
                onDailyTargetReached?.invoke()
            }
        }
    }

    private fun handleCloseCall(obs: Obstacle) {
        val now = totalTimeSec
        if (now - lastCloseCallTimeSec <= 2.0f) {
            combo = min(5, combo + 1)
        } else {
            combo = 1
        }
        lastCloseCallTimeSec = now

        val points = 25 * combo
        closeCallBonusScore += points
        onHapticCloseCall?.invoke()
        onComboScorePillPop?.invoke()

        if (combo >= 2) {
            val text = when (combo) {
                2 -> "Close!"
                3 -> "Zoom!"
                else -> "Wow!"
            }
            floatingTexts.add(
                FloatingComboText(
                    id = nextObjectId++,
                    text = text,
                    x = playerX,
                    startY = playerY - 40f,
                    currentY = playerY - 40f
                )
            )
        }
    }

    fun spawnRow() {
        val y = -140f
        // STRICT RULE: Spawner chooses 1 lane (65%) or 2 lanes (35%), never all 3!
        // In Saturday Zig-zag day: every row has exactly 2 obstacle lanes
        val count = if (mode == GameMode.DAILY && dailyModifier.forceTwoObstacles) {
            2
        } else {
            if (random.nextFloat() < 0.65f) 1 else 2
        }

        val lanes = mutableListOf(0, 1, 2)
        lanes.shuffle(random)
        val chosenLanes = lanes.take(count)
        val freeLanes = lanes.drop(count)

        chosenLanes.forEach { lane ->
            val roll = random.nextFloat()
            val (type, w, h, hw, hh) = if (mode == GameMode.DAILY) {
                val carThresh = dailyModifier.carChance
                val truckThresh = carThresh + dailyModifier.truckChance
                when {
                    roll < carThresh -> {
                        val scale = if (isAttractMode) 0.6f else 1f
                        Quint(ObstacleType.Car, 35f * scale, 67f * scale, 28f * scale, 60f * scale)
                    }
                    roll < truckThresh -> {
                        val scale = if (isAttractMode) 0.6f else 1f
                        Quint(ObstacleType.Truck, 42f * scale, 96f * scale, 36f * scale, 88f * scale)
                    }
                    else -> {
                        val scale = if (isAttractMode) 0.6f else 1f
                        Quint(ObstacleType.Cone, 20f * scale, 20f * scale, 16f * scale, 16f * scale)
                    }
                }
            } else {
                when {
                    roll < 0.55f -> {
                        val scale = if (isAttractMode) 0.6f else 1f
                        Quint(ObstacleType.Car, 35f * scale, 67f * scale, 28f * scale, 60f * scale)
                    }
                    roll < 0.80f -> {
                        val scale = if (isAttractMode) 0.6f else 1f
                        Quint(ObstacleType.Truck, 42f * scale, 96f * scale, 36f * scale, 88f * scale)
                    }
                    else -> {
                        val scale = if (isAttractMode) 0.6f else 1f
                        Quint(ObstacleType.Cone, 20f * scale, 20f * scale, 16f * scale, 16f * scale)
                    }
                }
            }

            var colorIdx = random.nextInt(8) + 1
            if (colorIdx == playerColorIndex) {
                colorIdx = (colorIdx % 8) + 1
            }

            obstacles.add(
                Obstacle(
                    id = nextObjectId++,
                    type = type,
                    lane = lane,
                    x = laneCenters[lane],
                    y = y,
                    width = w,
                    height = h,
                    hitboxWidth = hw,
                    hitboxHeight = hh,
                    carColorIndex = colorIdx
                )
            )
        }

        // Coins spawning: in free lane only
        val coinRain = mode == GameMode.DAILY && dailyModifier.coinRain
        if (freeLanes.isNotEmpty() && (coinRain || random.nextFloat() < 0.40f)) {
            val coinLane = freeLanes[random.nextInt(freeLanes.size)]
            val spacing = if (isAttractMode) 56f * 0.6f else 56f
            val radius = if (isAttractMode) 10f * 0.6f else 10f
            for (i in 0 until 5) {
                coins.add(
                    GameCoin(
                        id = nextObjectId++,
                        lane = coinLane,
                        x = laneCenters[coinLane],
                        y = y - (i * spacing),
                        radius = radius
                    )
                )
            }
        }
    }

    private data class Quint<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

    fun getPlayerHitbox(): Rect {
        return Rect(
            left = playerX - playerHitboxWidth / 2f,
            top = playerY - playerHitboxHeight / 2f,
            right = playerX + playerHitboxWidth / 2f,
            bottom = playerY + playerHitboxHeight / 2f
        )
    }
}
