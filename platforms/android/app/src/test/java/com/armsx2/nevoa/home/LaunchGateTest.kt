package com.armsx2.nevoa.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchGateTest {
    @Test fun only_first_start_wins() { val g = LaunchGate(); assertTrue(g.tryStart()); assertFalse(g.tryStart()) }
    @Test fun reset_allows_again() { val g = LaunchGate(); g.tryStart(); g.reset(); assertTrue(g.tryStart()) }
    @Test fun reopens_after_game_ends() = assertTrue(shouldReopenGate(true, true, 1000))
    @Test fun stays_closed_while_running() = assertFalse(shouldReopenGate(true, false, 1000))
    @Test fun stays_closed_during_slow_launch() = assertFalse(shouldReopenGate(false, true, 5000))
    @Test fun reopens_after_failed_launch() = assertTrue(shouldReopenGate(false, true, 15000))
}
