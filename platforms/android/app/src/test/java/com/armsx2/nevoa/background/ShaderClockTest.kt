package com.armsx2.nevoa.background

import org.junit.Assert.assertEquals
import org.junit.Test

class ShaderClockTest {
    @Test fun starts_at_zero() = assertEquals(0f, shaderSeconds(5_000L, 5_000L), 1e-6f)
    @Test fun counts_seconds() = assertEquals(2.5f, shaderSeconds(2_500_000_000L, 0L), 1e-4f)
    @Test fun wraps_to_keep_float_precision() = assertEquals(1f, shaderSeconds(601_000_000_000L, 0L), 1e-3f)
}
