package com.armsx2.nevoa.home

import org.junit.Assert.*
import org.junit.Test

class CategoryPoolTest {
    private data class Game(val name: String, val key: String?)
    private val scooby = Game("Scooby", "SLUS-21928")
    private val other = Game("Outro", "SLUS-00001")
    private val bios = Game("BIOS", null)
    private val games = listOf(bios, scooby, other)
    private val categories = mapOf("Aventura" to setOf("SLUS-21928"), "Favoritos" to setOf("SLUS-21928", "SLUS-00001"), "Vazia" to emptySet())
    private fun pool(selected: String?, map: Map<String, Set<String>> = categories) =
        categoryPool(games, selected, map.keys.toList(), { map.getValue(it) }, Game::key)

    @Test fun allGamesKeepsEntriesWithoutSettingsKeys() {
        assertEquals(games, pool(null).games)
    }
    @Test fun membershipUsesStableKeysAndIgnoresUnkeyedEntries() {
        assertEquals(listOf(scooby), pool("Aventura").games)
    }
    @Test fun oneGameCanBelongToSeveralCategories() {
        assertTrue(scooby in pool("Aventura").games)
        assertEquals(listOf(scooby, other), pool("Favoritos").games)
    }
    @Test fun existingEmptyCategoryRemainsSelectedWithNoMatches() {
        val result = pool("Vazia")
        assertEquals("Vazia", result.activeCategory)
        assertTrue(result.games.isEmpty())
    }
    @Test fun deletingSelectedCategoryRestoresAllRatherThanStrandingAnEmptyView() {
        val result = pool("Aventura", categories - "Aventura")
        assertNull(result.activeCategory)
        assertEquals(games, result.games)
    }
    @Test fun changingMembershipRebuildsThePoolWithoutRescanningOrLeakingOtherGamesIntoSearch() {
        val changed = pool("Aventura", categories + ("Aventura" to setOf("SLUS-00001")))
        assertEquals(listOf(other), changed.games)
        assertTrue(changed.games.filter { it.name.contains("Scooby") }.isEmpty())
        assertEquals(games, pool(null).games)
    }
}
