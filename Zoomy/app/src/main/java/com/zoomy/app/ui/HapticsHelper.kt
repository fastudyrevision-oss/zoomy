package com.zoomy.app.ui

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

class HapticsHelper(
    private val hapticFeedback: HapticFeedback,
    var hapticsOn: Boolean = true
) {
    fun perform(type: HapticFeedbackType) {
        if (hapticsOn) {
            hapticFeedback.performHapticFeedback(type)
        }
    }
}
