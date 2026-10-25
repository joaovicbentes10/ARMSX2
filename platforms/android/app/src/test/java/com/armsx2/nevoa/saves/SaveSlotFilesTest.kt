package com.armsx2.nevoa.saves

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SaveSlotFilesTest {
    @get:Rule val temp = TemporaryFolder()
    @Test fun nativeCandidateWithoutFileIsEmpty() =
        assertNull(existingSaveStatePath(File(temp.root, "SLUS-21928.00.p2s").path))
    @Test fun existingFileIsOccupied() {
        val state = temp.newFile("slot.p2s")
        assertEquals(state.path, existingSaveStatePath(state.path))
    }
    @Test fun directoryIsNotAState() = assertNull(existingSaveStatePath(temp.root.path))
    @Test fun missingOrBlankPathIsEmpty() {
        assertNull(existingSaveStatePath(null))
        assertNull(existingSaveStatePath(""))
    }
}
