package com.armsx2.nevoa.background

import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.armsx2.EmuState
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.runtime.MainActivityRuntime

/** Continuous session time. Do not wrap: the noise field is not periodic at 600 seconds. */
fun shaderSeconds(nowNanos: Long, startNanos: Long): Float =
    (nowNanos - startNanos).coerceAtLeast(0L) / 1e9f

/** Frame throttle: true once [minIntervalNanos] passed since [lastNanos]; [Long.MIN_VALUE] means no frame yet. */
fun shouldAdvance(nowNanos: Long, lastNanos: Long, minIntervalNanos: Long = 30_000_000L): Boolean =
    lastNanos == Long.MIN_VALUE || nowNanos - lastNanos >= minIntervalNanos

/** The animated "Névoa da BIOS" backdrop. [calm] halves speed and brightness. */
@Composable
fun NevoaBackground(modifier: Modifier = Modifier, calm: Boolean = false, active: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) AnimatedNevoa(modifier, calm, active) else StaticNevoa(modifier)
}

@Composable
private fun StaticNevoa(modifier: Modifier) {
    Box(modifier.background(Brush.verticalGradient(listOf(NevoaPalette.Background, NevoaPalette.Fog.copy(alpha = 0.35f).compositeOver(NevoaPalette.Background)))))
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AnimatedNevoa(modifier: Modifier, calm: Boolean, active: Boolean) {
    // A device whose AGSL compiler rejects the shader gets the static backdrop instead of a crash.
    val shader = remember {
        runCatching {
            RuntimeShader(NEVOA_AGSL).apply {
                setColorUniform("cSky", NevoaPalette.Background.toArgb())
                setColorUniform("cFog", NevoaPalette.Fog.toArgb())
                setColorUniform("cBlue", NevoaPalette.TowerBlue.toArgb())
                setColorUniform("cViolet", NevoaPalette.TowerViolet.toArgb())
                setColorUniform("cCyan", NevoaPalette.TowerCyan.toArgb())
            }
        }.onFailure { Log.w("Nevoa", "AGSL shader failed to compile", it) }.getOrNull()
    }
    if (shader == null) {
        StaticNevoa(modifier)
        return
    }
    val brush = remember(shader) { ShaderBrush(shader) }
    var seconds by remember { mutableFloatStateOf(0f) }
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val emu = MainActivityRuntime.eState.value
    val ticking = active && lifecycle.isAtLeast(Lifecycle.State.RESUMED) && emu != EmuState.RUNNING && emu != EmuState.PAUSED
    LaunchedEffect(ticking) {
        if (!ticking) return@LaunchedEffect
        // Resume from where the clock stopped instead of jumping ahead.
        val base = seconds
        val start = withFrameNanos { it }
        // The motion is slow; ~30 fps halves the full-screen shader passes on 120 Hz panels.
        var last = Long.MIN_VALUE
        while (true) withFrameNanos {
            if (shouldAdvance(it, last)) {
                last = it
                seconds = base + shaderSeconds(it, start)
            }
        }
    }
    Box(
        modifier.drawBehind {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", seconds)
            shader.setFloatUniform("calm", if (calm) 1f else 0f)
            drawRect(brush)
        },
    )
}
