package com.armsx2.nevoa.touch

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import com.armsx2.R
import com.armsx2.nevoa.theme.NevoaPalette

/** Default artwork only. Imported skins and the overlay's input geometry stay independent. */
@Composable
fun nevoaPadPainter(resource: Int, held: Boolean = false): Painter {
    val art = when (resource) {
        R.drawable.pad_cross, R.drawable.pad_cross_pressed -> "cross"
        R.drawable.pad_circle -> "circle"
        R.drawable.pad_square -> "square"
        R.drawable.pad_triangle, R.drawable.pad_triangle_pressed -> "triangle"
        R.drawable.pad_l1, R.drawable.pad_l1_pressed -> "L1"
        R.drawable.pad_l2, R.drawable.pad_l2_pressed -> "L2"
        R.drawable.pad_r1, R.drawable.pad_r1_pressed -> "R1"
        R.drawable.pad_r2, R.drawable.pad_r2_pressed -> "R2"
        R.drawable.pad_l3, R.drawable.pad_l3_pressed -> "L3"
        R.drawable.pad_r3, R.drawable.pad_r3_pressed -> "R3"
        R.drawable.pad_start, R.drawable.pad_start_pressed -> "start"
        R.drawable.pad_select, R.drawable.pad_select_pressed -> "select"
        R.drawable.pad_dpad_up, R.drawable.pad_dpad_up_pressed -> "up"
        R.drawable.pad_dpad_left, R.drawable.pad_dpad_left_pressed -> "left"
        R.drawable.pad_dpad_right, R.drawable.pad_dpad_right_pressed -> "right"
        R.drawable.pad_stick_base -> "base"
        R.drawable.pad_thumb -> "thumb"
        else -> return painterResource(resource)
    }
    val pressed = held || resource in pressedResources
    return remember(art, pressed) { PadPainter(art, pressed) }
}

private val pressedResources = setOf(
    R.drawable.pad_cross_pressed, R.drawable.pad_triangle_pressed,
    R.drawable.pad_l1_pressed, R.drawable.pad_l2_pressed,
    R.drawable.pad_r1_pressed, R.drawable.pad_r2_pressed,
    R.drawable.pad_l3_pressed, R.drawable.pad_r3_pressed,
    R.drawable.pad_start_pressed, R.drawable.pad_select_pressed,
    R.drawable.pad_dpad_up_pressed, R.drawable.pad_dpad_left_pressed, R.drawable.pad_dpad_right_pressed,
)

private class PadPainter(private val art: String, private val pressed: Boolean) : Painter() {
    // Preserve the built-in sprite ratios: shoulder spacing and D-pad bounds use these sizes.
    override val intrinsicSize: Size = when (art) {
        "L1", "R1" -> Size(69f, 40f)
        "L2", "R2" -> Size(71f, 63f)
        "L3", "R3" -> Size(70f, 70f)
        "start" -> Size(39f, 26f)
        "select" -> Size(42f, 27f)
        "up" -> Size(45f, 52f)
        "left", "right" -> Size(53f, 44f)
        "base" -> Size(133f, 133f)
        "thumb" -> Size(101f, 101f)
        else -> Size(57f, 57f)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    override fun DrawScope.onDraw() {
        val unit = size.minDimension
        val ink = if (pressed) NevoaPalette.TowerBlue else NevoaPalette.Text
        val edge = ink.copy(alpha = if (pressed) 0.95f else 0.68f)
        val fill = if (pressed) NevoaPalette.SelectedSurface.copy(alpha = 0.75f)
            else NevoaPalette.Background.copy(alpha = 0.28f)
        val stroke = Stroke(unit * 0.025f)
        val mid = center
        fun point(x: Float, y: Float) = Offset(size.width * x, size.height * y)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(ink, point(x1, y1), point(x2, y2), unit * 0.036f)
        fun panel() {
            val top = point(0.06f, 0.10f)
            val bounds = Size(size.width * 0.88f, size.height * 0.80f)
            val corners = CornerRadius(unit * 0.13f)
            drawRoundRect(fill, top, bounds, corners)
            drawRoundRect(edge, top, bounds, corners, style = stroke)
        }
        when (art) {
            "base" -> {
                drawCircle(fill, unit * 0.46f)
                drawCircle(edge.copy(alpha = 0.50f), unit * 0.46f, style = stroke)
            }
            "thumb" -> {
                drawCircle(NevoaPalette.Background.copy(alpha = 0.50f), unit * 0.43f)
                drawCircle(edge, unit * 0.43f, style = stroke)
            }
            "up", "left", "right" -> {
                // The original arm bounds overlap. Taper each inner end toward the
                // centre so the outlines keep a clean gap rather than crossing.
                val vertices = when (art) {
                    "up" -> listOf(0.12f to 0.10f, 0.88f to 0.10f, 0.88f to 0.62f, 0.50f to 0.90f, 0.12f to 0.62f)
                    "left" -> listOf(0.10f to 0.12f, 0.62f to 0.12f, 0.90f to 0.50f, 0.62f to 0.88f, 0.10f to 0.88f)
                    else -> listOf(0.90f to 0.12f, 0.38f to 0.12f, 0.10f to 0.50f, 0.38f to 0.88f, 0.90f to 0.88f)
                }
                val arm = Path().apply {
                    vertices.forEachIndexed { index, (x, y) ->
                        if (index == 0) moveTo(size.width * x, size.height * y)
                        else lineTo(size.width * x, size.height * y)
                    }
                    close()
                }
                drawPath(arm, fill)
                drawPath(arm, edge, style = stroke)
                when (art) {
                    "up" -> { line(0.32f, 0.57f, 0.50f, 0.37f); line(0.50f, 0.37f, 0.68f, 0.57f) }
                    "left" -> { line(0.57f, 0.30f, 0.38f, 0.50f); line(0.38f, 0.50f, 0.57f, 0.70f) }
                    else -> { line(0.43f, 0.30f, 0.62f, 0.50f); line(0.62f, 0.50f, 0.43f, 0.70f) }
                }
            }
            "cross", "circle", "square", "triangle" -> {
                drawCircle(fill, unit * 0.46f)
                drawCircle(edge, unit * 0.46f, style = stroke)
                when (art) {
                    "cross" -> { line(0.35f, 0.35f, 0.65f, 0.65f); line(0.35f, 0.65f, 0.65f, 0.35f) }
                    "circle" -> drawCircle(ink, unit * 0.20f, style = stroke)
                    "square" -> drawRect(ink, point(0.32f, 0.32f), Size(unit * 0.36f, unit * 0.36f), style = stroke)
                    else -> drawPath(Path().apply {
                        moveTo(size.width * 0.50f, size.height * 0.28f)
                        lineTo(size.width * 0.72f, size.height * 0.67f)
                        lineTo(size.width * 0.28f, size.height * 0.67f)
                        close()
                    }, ink, style = stroke)
                }
            }
            else -> {
                panel()
                when (art) {
                    "start" -> drawPath(Path().apply {
                        moveTo(size.width * 0.42f, size.height * 0.28f)
                        lineTo(size.width * 0.65f, size.height * 0.50f)
                        lineTo(size.width * 0.42f, size.height * 0.72f)
                        close()
                    }, ink, style = stroke)
                    "select" -> {
                        drawRect(ink.copy(alpha = 0.55f), point(0.32f, 0.28f), Size(size.width * 0.25f, size.height * 0.32f), style = stroke)
                        drawRect(ink, point(0.42f, 0.42f), Size(size.width * 0.25f, size.height * 0.32f), style = stroke)
                    }
                    else -> drawIntoCanvas { canvas ->
                        labelPaint.color = ink.toArgb()
                        labelPaint.textSize = minOf(unit * 0.40f, size.width * 0.25f)
                        val baseline = mid.y - (labelPaint.ascent() + labelPaint.descent()) / 2
                        canvas.nativeCanvas.drawText(art, mid.x, baseline, labelPaint)
                    }
                }
            }
        }
    }
}
