package com.armsx2.nevoa.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.armsx2.GameInfo
import com.armsx2.ui.common.GameCoverArt
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
import kotlin.math.roundToInt


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

private const val TransitionMillis = 900

/** Moves the chosen cover into a restrained launch presentation, then hands off on black. */
@Composable
fun LaunchBurst(active: Boolean, game: GameInfo? = null, sourceBounds: Rect? = null, onFinished: () -> Unit) {
    if (!active) return
    val progress = remember { Animatable(0f) }
    val finished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(TransitionMillis, easing = LinearEasing))
        finished()
    }
    BoxWithConstraints(
        Modifier.fillMaxSize().pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) awaitPointerEvent().changes.forEach { it.consume() }
            }
        },
    ) {
        val p = progress.value
        val travel = FastOutSlowInEasing.transform((p / 0.65f).coerceIn(0f, 1f))
        val fade = ((p - 0.72f) / 0.28f).coerceIn(0f, 1f)
        Canvas(Modifier.fillMaxSize()) {
            val dim = (p / 0.28f).coerceIn(0f, 1f)
            drawRect(NevoaPalette.Background.copy(alpha = dim * 0.96f))
            drawRect(Brush.verticalGradient(listOf(
                Color.Transparent,
                NevoaPalette.Fog.copy(alpha = 0.10f * dim * (1f - fade)),
                Color.Transparent,
            )))
            drawRect(Color.Black.copy(alpha = fade * fade * (3f - 2f * fade)))
        }
        if (game != null) {
            val density = LocalDensity.current
            val targetHeight = (maxHeight * 0.62f).coerceAtMost(240.dp)
            val targetWidth = targetHeight * (135f / 190f)
            val sourceWidth = with(density) { sourceBounds?.width?.toDp() } ?: 125.dp
            val sourceHeight = with(density) { sourceBounds?.height?.toDp() } ?: 176.dp
            val sourceX = with(density) { sourceBounds?.left?.toDp() } ?: (maxWidth - sourceWidth) / 2f
            val sourceY = with(density) { sourceBounds?.top?.toDp() } ?: (maxHeight - sourceHeight) / 2f
            val targetX = (maxWidth - targetWidth) / 2f
            val targetY = (maxHeight - targetHeight) / 2f - 14.dp
            val width = sourceWidth + (targetWidth - sourceWidth) * travel
            val height = sourceHeight + (targetHeight - sourceHeight) * travel
            val x = sourceX + (targetX - sourceX) * travel
            val y = sourceY + (targetY - sourceY) * travel
            val opacity = (p / 0.12f).coerceIn(0f, 1f) * (1f - fade)
            Box(
                Modifier.offset { IntOffset(x.roundToPx(), y.roundToPx()) }
                    .size(width, height).graphicsLayer { alpha = opacity },
            ) { GameCoverArt(game, Modifier.fillMaxSize()) }
            Text(
                coverLabel(game.title, game.uri.lastPathSegment),
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = targetY + targetHeight + 12.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth().graphicsLayer {
                        alpha = ((p - 0.25f) / 0.25f).coerceIn(0f, 1f) * (1f - fade)
                    },
                color = NevoaPalette.Text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}