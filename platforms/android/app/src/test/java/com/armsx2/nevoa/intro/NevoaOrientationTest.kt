package com.armsx2.nevoa.intro

import org.junit.Assert.assertEquals
import org.junit.Test

class NevoaOrientationTest {
    @Test fun legacyDeviceDefaultKeepsLandscapeAtGameBoot() = assertEquals(1, nevoaOrientation(0))
    @Test fun landscapeStaysLocked() = assertEquals(1, nevoaOrientation(1))
    @Test fun explicitPortraitIsPreserved() = assertEquals(2, nevoaOrientation(2))
    @Test fun explicitAutoRotateIsPreserved() = assertEquals(3, nevoaOrientation(3))
    @Test fun invalidSettingFallsBackToLandscape() = assertEquals(1, nevoaOrientation(-1))
}
