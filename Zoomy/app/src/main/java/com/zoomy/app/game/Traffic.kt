package com.zoomy.app.game

import androidx.compose.ui.geometry.Rect

enum class ObstacleType {
    Car,
    Truck,
    Cone
}

data class Obstacle(
    val id: Long,
    val type: ObstacleType,
    val lane: Int,
    var x: Float, // center X in dp
    var y: Float, // center Y in dp
    val width: Float,
    val height: Float,
    val hitboxWidth: Float,
    val hitboxHeight: Float,
    val carColorIndex: Int = 1,
    var passedPlayer: Boolean = false, // for close call detection
    var isPopping: Boolean = false, // shield absorb pop animation
    var popProgress: Float = 0f
) {
    fun getHitbox(): Rect {
        return Rect(
            left = x - hitboxWidth / 2f,
            top = y - hitboxHeight / 2f,
            right = x + hitboxWidth / 2f,
            bottom = y + hitboxHeight / 2f
        )
    }
}

data class GameCoin(
    val id: Long,
    val lane: Int,
    var x: Float,
    var y: Float,
    val radius: Float = 10f, // 20dp diameter
    var collected: Boolean = false
) {
    fun getHitbox(): Rect {
        return Rect(
            left = x - radius,
            top = y - radius,
            right = x + radius,
            bottom = y + radius
        )
    }
}
