package com.armsx2.nevoa.background

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShaderClockTest {
    @Test fun starts_at_zero() = assertEquals(0f, shaderSeconds(5_000L, 5_000L), 1e-6f)
    @Test fun counts_seconds() = assertEquals(2.5f, shaderSeconds(2_500_000_000L, 0L), 1e-4f)
    @Test fun wraps_to_keep_float_precision() = assertEquals(1f, shaderSeconds(601_000_000_000L, 0L), 1e-3f)
    @Test fun advances_after_interval() = assertTrue(shouldAdvance(1_000_000_000L + 33_000_000L, 1_000_000_000L))
    @Test fun holds_within_interval() = assertFalse(shouldAdvance(1_000_000_000L + 10_000_000L, 1_000_000_000L))
    @Test fun first_frame_advances() = assertTrue(shouldAdvance(0L, Long.MIN_VALUE))
}
