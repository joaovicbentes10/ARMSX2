package com.armsx2.nevoa.touch

import com.armsx2.ui.touch.TouchButtonId
import com.armsx2.ui.touch.TouchLayout
import kotlin.math.abs
import kotlin.math.sqrt

/** Correct the stock percentage-based diamond for the actual screen aspect ratio.
 * Custom positions stay authored by the user. The overlay uses the same corrected
 * layout for painting and hit testing; sizes and every non-position field survive.
 */
fun uniformDefaultFaceDiamond(layout: TouchLayout, width: Float, height: Float): TouchLayout {
    if (!width.isFinite() || !height.isFinite() || width <= 0f || height <= 0f) return layout
    val face = layout.buttons.filter { it.id.kind == TouchButtonId.Kind.FACE }
    if (face.size != 4 || face.map { it.id }.toSet().size != 4) return layout
    val defaults = TouchLayout.defaultFor(height > width).buttons.associateBy { it.id }
    if (face.any { button ->
        val stock = defaults.getValue(button.id)
        abs(button.xFrac - stock.xFrac) > 0.0001f || abs(button.yFrac - stock.yFrac) > 0.0001f
    }) return layout

    val cx = face.sumOf { (it.xFrac * width).toDouble() }.toFloat() / 4f
    val cy = face.sumOf { (it.yFrac * height).toDouble() }.toFloat() / 4f
    // Preserve the original adjacent-button distance while making both axes equal.
    val radius = sqrt(face.sumOf {
        val dx = it.xFrac * width - cx
        val dy = it.yFrac * height - cy
        (dx * dx + dy * dy).toDouble()
    } / 4.0).toFloat()
    return layout.copy(buttons = layout.buttons.map { button ->
        when (button.id) {
            TouchButtonId.TRIANGLE -> button.copy(xFrac = cx / width, yFrac = (cy - radius) / height)
            TouchButtonId.CIRCLE -> button.copy(xFrac = (cx + radius) / width, yFrac = cy / height)
            TouchButtonId.CROSS -> button.copy(xFrac = cx / width, yFrac = (cy + radius) / height)
            TouchButtonId.SQUARE -> button.copy(xFrac = (cx - radius) / width, yFrac = cy / height)
            else -> button
        }
    })
}
