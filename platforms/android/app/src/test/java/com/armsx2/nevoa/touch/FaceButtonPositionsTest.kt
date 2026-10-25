package com.armsx2.nevoa.touch

import com.armsx2.ui.touch.TouchButtonId
import com.armsx2.ui.touch.TouchLayout
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.hypot

class FaceButtonPositionsTest {
    @Test fun landscapeDiamondHasEqualPixelRadiiInPs2Order() {
        checkDiamond(TouchLayout.default(), 3200f, 1440f)
    }

    @Test fun portraitDiamondAlsoUsesPixelsRatherThanEqualFractions() {
        checkDiamond(TouchLayout.defaultPortrait(), 1440f, 3200f)
    }

    @Test fun customPositionsArePreserved() {
        val original = TouchLayout.default()
        val custom = original.copy(buttons = original.buttons.map {
            if (it.id == TouchButtonId.CROSS) it.copy(xFrac = 0.7f) else it
        })
        assertSame(custom, uniformDefaultFaceDiamond(custom, 3200f, 1440f))
    }

    @Test fun correctionPreservesSizesFlagsAndOtherControlsAndIsIdempotent() {
        val original = TouchLayout.default()
        val corrected = uniformDefaultFaceDiamond(original, 3200f, 1440f)
        original.buttons.zip(corrected.buttons).forEach { (before, after) ->
            assertEquals(before, after.copy(xFrac = before.xFrac, yFrac = before.yFrac))
            if (before.id.kind != TouchButtonId.Kind.FACE) assertEquals(before, after)
        }
        assertEquals(corrected, uniformDefaultFaceDiamond(corrected, 3200f, 1440f))
    }

    @Test fun unavailableScreenSizeDoesNotMoveButtons() {
        val original = TouchLayout.default()
        assertSame(original, uniformDefaultFaceDiamond(original, 0f, 1440f))
        assertSame(original, uniformDefaultFaceDiamond(original, 3200f, Float.NaN))
    }

    private fun checkDiamond(original: TouchLayout, width: Float, height: Float) {
        val result = uniformDefaultFaceDiamond(original, width, height)
        val buttons = result.buttons.associateBy { it.id }
        val up = buttons.getValue(TouchButtonId.TRIANGLE)
        val right = buttons.getValue(TouchButtonId.CIRCLE)
        val down = buttons.getValue(TouchButtonId.CROSS)
        val left = buttons.getValue(TouchButtonId.SQUARE)
        val cx = up.xFrac * width
        val cy = right.yFrac * height
        val radii = listOf(up, right, down, left).map { hypot(it.xFrac * width - cx, it.yFrac * height - cy) }
        radii.forEach { assertEquals(radii.first(), it, 0.002f) }
        assertEquals(up.xFrac, down.xFrac, 0.000001f)
        assertEquals(left.yFrac, right.yFrac, 0.000001f)
        assertTrue(up.yFrac < down.yFrac)
        assertTrue(left.xFrac < right.xFrac)
        val originalFaces = original.buttons.filter { it.id.kind == TouchButtonId.Kind.FACE }
        assertEquals(originalFaces.map { it.xFrac }.average(), listOf(up, right, down, left).map { it.xFrac }.average(), 0.000001)
        assertEquals(originalFaces.map { it.yFrac }.average(), listOf(up, right, down, left).map { it.yFrac }.average(), 0.000001)
    }
}
