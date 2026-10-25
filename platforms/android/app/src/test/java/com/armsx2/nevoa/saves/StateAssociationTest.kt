package com.armsx2.nevoa.saves

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StateAssociationTest {
    @get:Rule val temp = TemporaryFolder()
    private val id = StateIdentity("SLUS-21928", "38E42973")
    private fun state(name: String = "state.p2s") = temp.newFile(name).apply { writeText("native-state-payload") }
    @Test fun nativeReceiptBindsFileToSerialCrcAndSlot() {
        val file = state()
        assertNotNull(StateAssociation.write(file, 3, id))
        val read = StateAssociation.read(file, StateIdentity("slus-21928", "38e42973"))!!
        assertEquals(3, read.slot)
        assertEquals(file.canonicalFile, read.file)
    }
    @Test fun sameSerialDifferentRevisionCannotResume() {
        val file = state(); StateAssociation.write(file, 0, id)
        assertNull(StateAssociation.read(file, id.copy(crc = "12345678")))
    }
    @Test fun differentSerialCannotResumeEvenWithSameCrc() {
        val file = state(); StateAssociation.write(file, 0, id)
        assertNull(StateAssociation.read(file, id.copy(serial = "SLUS-00001")))
    }
    @Test fun plausibleFilenameWithoutNativeReceiptIsNotAssociation() {
        assertNull(StateAssociation.read(state("SLUS-21928 (38E42973).00.p2s"), id))
    }
    @Test fun changedBytesAreRejectedEvenWhenSizeAndTimeArePreserved() {
        val file = state(); StateAssociation.write(file, 0, id)
        val modified = file.lastModified()
        file.writeText("altered-state-bytes!")
        assertEquals("native-state-payload".length, "altered-state-bytes!".length)
        assertTrue(file.setLastModified(modified))
        assertNull(StateAssociation.read(file, id))
    }
    @Test fun deletedStateDoesNotLeaveAnOccupiedEntry() {
        val file = state(); StateAssociation.write(file, 0, id); assertTrue(file.delete())
        assertNull(StateAssociation.read(file, id))
    }
    @Test fun copiedReceiptCannotRedirectToAnotherSaveRoot() {
        val file = state(); StateAssociation.write(file, 0, id)
        val other = temp.newFolder("other")
        file.copyTo(File(other, file.name))
        File(file.path + ".nevoa-state").copyTo(File(other, file.name + ".nevoa-state"))
        assertNull(StateAssociation.read(File(other, file.name), id))
    }
    @Test fun runtimeTargetMustBeTheExactVerifiedFile() {
        val file = state(); val proof = StateAssociation.write(file, 0, id)!!
        assertTrue(StateAssociation.matchesRuntimeTarget(proof, file.path))
        assertFalse(StateAssociation.matchesRuntimeTarget(proof, state("other.p2s").path))
        assertFalse(StateAssociation.matchesRuntimeTarget(proof, null))
    }
    @Test fun automaticAndManualSlotsAreExplicitAndOtherSentinelsRejected() {
        val file = state()
        assertNotNull(StateAssociation.write(file, -2, id))
        assertNotNull(StateAssociation.write(file, 9, id))
        assertNull(StateAssociation.write(file, -1, id))
        assertNull(StateAssociation.write(file, 10, id))
        assertNull(StateAssociation.write(file, 0, id.copy(crc = "00000000")))
    }
    @Test fun digestIsTheStandardSha256OfTheActualPayload() {
        val file = state(); file.writeText("abc")
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", StateAssociation.digest(file))
    }
    @Test fun nativeDiscIdentityIsParsedWithoutGuessingFromTheFilename() {
        assertEquals(StateIdentity("NEVO-00001", "CD915601"), StateAssociation.parseNativeIdentity("NEVO-00001 (CD915601)"))
        assertNull(StateAssociation.parseNativeIdentity("NEVO-00001 (00000000)"))
        assertNull(StateAssociation.parseNativeIdentity("NEVO-00001 (CD915601).09.p2s"))
        assertNull(StateAssociation.parseNativeIdentity(""))
    }
    @Test fun sourceMustHaveBeenConfirmedDuringTheNativeOperation() {
        val file = state()
        StateAssociation.write(file, 0, id)
        assertNull(StateAssociation.readForSource(file, "content://disc/one", id.serial))
        StateAssociation.write(file, 0, id, "content://disc/one")
        assertNotNull(StateAssociation.readForSource(file, "content://disc/one", id.serial))
        assertNull(StateAssociation.readForSource(file, "content://disc/two", id.serial))
        assertNull(StateAssociation.readForSource(file, "content://disc/one", "OTHER-00001"))
    }
}
