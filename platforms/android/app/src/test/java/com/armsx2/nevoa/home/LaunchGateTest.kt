package com.armsx2.nevoa.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchGateTest {
    @Test fun only_first_start_wins() { val g = LaunchGate(); assertTrue(g.tryStart()); assertFalse(g.tryStart()) }
    @Test fun reset_allows_again() { val g = LaunchGate(); g.tryStart(); g.reset(); assertTrue(g.tryStart()) }
}
