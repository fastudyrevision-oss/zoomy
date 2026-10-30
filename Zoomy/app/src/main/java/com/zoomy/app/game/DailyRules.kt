package com.zoomy.app.game

import java.time.DayOfWeek
import java.time.LocalDate

enum class GameMode {
    NORMAL,
    DAILY
}

data class DailyModifier(
    val dayName: String,
    val description: String,
    val startSpeed: Float = 260f,
    val laneChangeDuration: Float = 0.140f,
    val spawnIntervalMultiplier: Float = 1.0f,
    val truckChance: Float = 0.25f,
    val carChance: Float = 0.55f,
    val coneChance: Float = 0.20f,
    val coinRain: Boolean = false,
    val forceTwoObstacles: Boolean = false
)

object DailyRules {
    fun getModifier(date: LocalDate = LocalDate.now()): DailyModifier {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> DailyModifier(
                dayName = "Fast start",
                description = "Start speed 340 dp/s",
                startSpeed = 340f
            )
            DayOfWeek.TUESDAY -> DailyModifier(
                dayName = "Rainy road",
                description = "Lane change takes 220ms",
                laneChangeDuration = 0.220f
            )
            DayOfWeek.WEDNESDAY -> DailyModifier(
                dayName = "Rush hour",
                description = "Spawn interval x0.8",
                spawnIntervalMultiplier = 0.8f
            )
            DayOfWeek.THURSDAY -> DailyModifier(
                dayName = "Trucks day",
                description = "Truck 55%, Car 25%, Cone 20%",
                truckChance = 0.55f,
                carChance = 0.25f,
                coneChance = 0.20f
            )
            DayOfWeek.FRIDAY -> DailyModifier(
                dayName = "Coin rain",
                description = "A coin line in a free lane on every row",
                coinRain = true
            )
            DayOfWeek.SATURDAY -> DailyModifier(
                dayName = "Zig-zag day",
                description = "Every row has exactly 2 obstacle lanes",
                forceTwoObstacles = true
            )
            DayOfWeek.SUNDAY -> DailyModifier(
                dayName = "Cruise day",
                description = "Standard rules"
            )
            else -> DailyModifier("Cruise day", "Standard rules")
        }
    }
}
