package com.armsx2.nevoa.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import com.armsx2.ui.Colors
import com.armsx2.ui.theme.ArmsTypography

object NevoaPalette {
    val Background = Color(0xFF050812)
    val Fog = Color(0xFF5A8CFF)
    val TowerBlue = Color(0xFF9CC3FF)
    val TowerViolet = Color(0xFFB9A3FF)
    val TowerCyan = Color(0xFF7FE0FF)
    val Text = Color(0xFFE8EEFF)
    val TextDim = Color(0xFF9AA6C8)
    val Glass = Color.White.copy(alpha = 0.08f)
    val GlassBorder = Color.White.copy(alpha = 0.20f)
    val Selected = TowerBlue

    val scheme: ColorScheme = darkColorScheme(
        primary = Selected,
        secondary = TowerViolet,
        tertiary = TowerCyan,
        background = Background,
        surface = Background,
        onBackground = Text,
        onSurface = Text,
        onSurfaceVariant = TextDim,
        surfaceVariant = Glass.compositeOver(Background),
        outline = GlassBorder.compositeOver(Background),
    )

    val typography: Typography = ArmsTypography.let { t ->
        fun TextStyle.nevoa() = copy(fontWeight = FontWeight.Light, letterSpacing = 0.06.em)
        t.copy(
            displayLarge = t.displayLarge.nevoa(),
            displayMedium = t.displayMedium.nevoa(),
            displaySmall = t.displaySmall.nevoa(),
            headlineLarge = t.headlineLarge.nevoa(),
            headlineMedium = t.headlineMedium.nevoa(),
            headlineSmall = t.headlineSmall.nevoa(),
            titleLarge = t.titleLarge.nevoa(),
            titleMedium = t.titleMedium.nevoa(),
            titleSmall = t.titleSmall.nevoa(),
        )
    }

    /** Points the legacy Colors holders at the palette so non-themed screens match. */
    fun applyLegacyColors() {
        Colors.surface.value = Background
        Colors.surfaceDark.value = Background
        Colors.surfaceDarker.value = Background
        Colors.secondary.value = Selected
        Colors.secondarySurface.value = scheme.surfaceVariant
    }
}
