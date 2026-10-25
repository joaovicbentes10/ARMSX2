package com.armsx2.nevoa.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.armsx2.nevoa.theme.NevoaPalette
import kotlin.math.sin
import kotlin.math.PI

/** Allows a single launch until [reset]; guards against double taps and pad + touch at once. */
class LaunchGate {
    private var started = false

    fun tryStart(): Boolean {
        if (started) return false
        started = true
        return true
    }

    fun reset() {
        started = false
    }
}

/** True when the gate may reopen: the game ran and ended, or the launch never started (failed). */
fun shouldReopenGate(
    sawNonStopped: Boolean,
    nowStopped: Boolean,
    elapsedMs: Long,
    failureTimeoutMs: Long = 15_000L,
): Boolean = nowStopped && (sawNonStopped || elapsedMs >= failureTimeoutMs)

private const val TransitionMillis = 520

/** Dissolves the library through blue fog into the game's dark loading frame. */
@Composable
fun LaunchBurst(active: Boolean, onFinished: () -> Unit) {
    if (!active) return
    val progress = remember { Animatable(0f) }
    val finished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(TransitionMillis, easing = LinearEasing))
        finished()
    }
    Canvas(
        Modifier
            .fillMaxSize()
            // Consume everything so nothing underneath reacts during the burst.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent().changes.forEach { it.consume() }
                }
            },
    ) {
        val p = progress.value
        val dissolve = p * p * (3f - 2f * p)
        drawRect(NevoaPalette.Background.copy(alpha = dissolve))

        // A broad, dim veil belongs to the existing scene, rather than a white
        // emitter or particles placed on top of it. It disappears before gameplay.
        val mist = sin(p * PI).toFloat().coerceAtLeast(0f) * 0.12f
        val drift = size.width * (p * 0.6f - 0.4f)
        drawRect(Brush.horizontalGradient(
            colors = listOf(Color.Transparent, NevoaPalette.Fog.copy(alpha = mist), Color.Transparent),
            startX = drift,
            endX = drift + size.width * 1.4f,
        ))
        val ending = ((p - 0.4f) / 0.6f).coerceIn(0f, 1f)
        drawRect(Color.Black.copy(alpha = ending * ending * (3f - 2f * ending)))
    }
}
