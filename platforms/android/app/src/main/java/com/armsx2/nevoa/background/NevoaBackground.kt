package com.armsx2.nevoa.background

import android.graphics.RuntimeShader
import android.os.Build
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

/** Seconds since [startNanos], wrapped at [wrapSeconds] so the shader's float time stays precise. */
fun shaderSeconds(nowNanos: Long, startNanos: Long, wrapSeconds: Float = 600f): Float {
    val wrapNanos = (wrapSeconds * 1e9).toLong()
    return ((nowNanos - startNanos) % wrapNanos) / 1e9f
}

/** The animated "Névoa da BIOS" backdrop. [calm] halves speed and brightness. */
@Composable
fun NevoaBackground(modifier: Modifier = Modifier, calm: Boolean = false) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AnimatedNevoa(modifier, calm)
    } else {
        Box(modifier.background(Brush.verticalGradient(listOf(NevoaPalette.Background, NevoaPalette.Fog.copy(alpha = 0.35f).compositeOver(NevoaPalette.Background)))))
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AnimatedNevoa(modifier: Modifier, calm: Boolean) {
    val shader = remember {
        RuntimeShader(NEVOA_AGSL).apply {
            setColorUniform("cSky", NevoaPalette.Background.toArgb())
            setColorUniform("cFog", NevoaPalette.Fog.toArgb())
            setColorUniform("cBlue", NevoaPalette.TowerBlue.toArgb())
            setColorUniform("cViolet", NevoaPalette.TowerViolet.toArgb())
            setColorUniform("cCyan", NevoaPalette.TowerCyan.toArgb())
        }
    }
    val brush = remember(shader) { ShaderBrush(shader) }
    var seconds by remember { mutableFloatStateOf(0f) }
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val emu = MainActivityRuntime.eState.value
    val ticking = lifecycle.isAtLeast(Lifecycle.State.RESUMED) && emu != EmuState.RUNNING && emu != EmuState.PAUSED
    LaunchedEffect(ticking) {
        if (!ticking) return@LaunchedEffect
        // Resume from where the clock stopped instead of jumping ahead.
        val base = seconds
        val start = withFrameNanos { it }
        while (true) withFrameNanos { seconds = (base + shaderSeconds(it, start)) % 600f }
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
