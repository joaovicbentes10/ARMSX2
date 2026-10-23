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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.armsx2.nevoa.theme.NevoaPalette
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

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

private const val BurstMillis = 700
private const val FadeMillis = 150
private const val ParticleCount = 40

/** Light burst over the whole screen. Swallows touches while [active]; calls [onFinished] once. */
@Composable
fun LaunchBurst(active: Boolean, onFinished: () -> Unit) {
    if (!active) return
    val progress = remember { Animatable(0f) }
    val finished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(BurstMillis, easing = LinearEasing))
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
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = hypot(size.width, size.height) / 2f
        val grow = 1f - (1f - p) * (1f - p) // ease-out
        val radius = (maxRadius * grow).coerceAtLeast(1f)
        drawCircle(
            Brush.radialGradient(
                0f to NevoaPalette.Text,
                0.35f to NevoaPalette.TowerCyan,
                0.7f to NevoaPalette.Fog,
                0.95f to NevoaPalette.Background,
                1f to Color.Transparent,
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
        for (i in 0 until ParticleCount) {
            val angle = i * 2f * Math.PI.toFloat() / ParticleCount + (i % 3) * 0.07f
            val reach = radius * (0.5f + 0.5f * ((i * 37) % 10) / 10f)
            drawCircle(
                NevoaPalette.Text.copy(alpha = 1f - p),
                radius = 3f + (i % 4) * 1.5f,
                center = Offset(center.x + cos(angle) * reach, center.y + sin(angle) * reach),
            )
        }
        val fadeStart = 1f - FadeMillis.toFloat() / BurstMillis
        if (p > fadeStart) {
            drawRect(Color.Black.copy(alpha = ((p - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f)))
        }
    }
}
