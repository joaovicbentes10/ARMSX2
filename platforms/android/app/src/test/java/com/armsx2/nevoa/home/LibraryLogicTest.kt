package com.armsx2.nevoa.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryLogicTest {
    @Test fun libraryOrder_dedupesRecentFirst() = assertEquals(listOf("b", "a", "c"), libraryOrder(listOf("b"), listOf("a", "b", "c")) { it })
    @Test fun libraryOrder_200games_noDuplicates() { val all = (1..200).map { "g$it" }; assertEquals(200, libraryOrder(all.take(5), all) { it }.toSet().size) }
    @Test fun moveSelection_clampsAtEnds() { assertEquals(0, moveSelection(0, -1, 3)); assertEquals(2, moveSelection(2, 1, 3)); assertEquals(1, moveSelection(0, 1, 3)) }
    @Test fun moveSelection_emptyIsZero() = assertEquals(0, moveSelection(4, 1, 0))
    @Test fun shouldShowClassic_whenNoDirs() = assertTrue(shouldShowClassic(initialized = true, scanning = false, gameCount = 3, hasRomDirs = false, classicRequested = false))
    @Test fun shouldShowClassic_whenScanningEmpty() = assertTrue(shouldShowClassic(initialized = true, scanning = true, gameCount = 0, hasRomDirs = true, classicRequested = false))
    @Test fun shouldShowNevoa_whenGamesPresent() = assertFalse(shouldShowClassic(initialized = true, scanning = false, gameCount = 3, hasRomDirs = true, classicRequested = false))
    @Test fun coverLabel_fallsBackToTitle() = assertEquals("Scooby", coverLabel("  Scooby ", "x.iso"))
    @Test fun coverLabel_fallsBackToFileName() = assertEquals("x", coverLabel(" ", "x.iso"))
    @Test fun coverLabel_nothingAtAll() = assertEquals("?", coverLabel("", null))
    @Test fun indexOfKey_findsMovedGame() = assertEquals(1, indexOfKey(listOf("b", "a", "c"), "a"))
    @Test fun indexOfKey_missingFallsBackToZero() = assertEquals(0, indexOfKey(listOf("b", "a", "c"), "z"))
    @Test fun indexOfKey_nullIsZero() = assertEquals(0, indexOfKey(listOf("b", "a", "c"), null))
}
