package com.armsx2.nevoa.saves

import java.io.File
import java.security.MessageDigest
import java.util.Properties

data class StateIdentity(val serial: String, val crc: String) {
    val valid: Boolean get() = serial.isNotBlank() && crc.matches(Regex("[0-9A-Fa-f]{8}")) && crc != "00000000"
    fun matches(other: StateIdentity) = valid && other.valid && serial.equals(other.serial, true) && crc.equals(other.crc, true)
}

data class AssociatedState(val file: File, val slot: Int, val identity: StateIdentity, val sha256: String, val size: Long, val modified: Long)

/** Only native-confirmed saves get a receipt. A plausible filename alone is never sufficient. */
object StateAssociation {
    fun parseNativeIdentity(value: String): StateIdentity? {
        val match = Regex("^(.+) \\(([0-9A-Fa-f]{8})\\)$").matchEntire(value.trim()) ?: return null
        return StateIdentity(match.groupValues[1], match.groupValues[2]).takeIf { it.valid }
    }
    private fun receipt(file: File) = File(file.parentFile, file.name + ".nevoa-state")
    fun digest(file: File): String {
        val hash = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { stream ->
            val buffer = ByteArray(65536)
            while (true) { val count = stream.read(buffer); if (count < 0) break; hash.update(buffer, 0, count) }
        }
        return hash.digest().joinToString("") { "%02x".format(it) }
    }
    fun write(file: File, slot: Int, identity: StateIdentity, sourceUri: String? = null): AssociatedState? = runCatching {
        if (!file.isFile || file.length() == 0L || !identity.valid || slot !in 0..9 && slot != -2) return null
        val state = AssociatedState(file.canonicalFile, slot, identity, digest(file), file.length(), file.lastModified())
        val props = Properties().apply {
            setProperty("version", "1"); setProperty("serial", identity.serial); setProperty("crc", identity.crc)
            setProperty("path", state.file.path); setProperty("slot", slot.toString()); setProperty("sha256", state.sha256)
            setProperty("size", state.size.toString()); setProperty("modified", state.modified.toString())
            sourceUri?.let { setProperty("sourceUri", it) }
        }
        val target = receipt(file)
        val temp = File(target.path + ".part")
        temp.outputStream().use { props.store(it, "Nevoa native-confirmed save") }
        java.nio.file.Files.move(temp.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        state
    }.getOrNull()

    /** A library row is linked only by a URI recorded during a successful native operation. */
    fun readForSource(file: File, sourceUri: String, serial: String?): AssociatedState? = runCatching {
        if (sourceUri.isBlank() || serial.isNullOrBlank()) return null
        val props = Properties().apply { receipt(file).inputStream().use { load(it) } }
        if (props.getProperty("sourceUri") != sourceUri) return null
        val identity = StateIdentity(props.getProperty("serial", ""), props.getProperty("crc", ""))
        if (!identity.serial.equals(serial, true)) return null
        read(file, identity)
    }.getOrNull()

    fun read(file: File, identity: StateIdentity): AssociatedState? = runCatching {
        if (!file.isFile || !identity.valid) return null
        val props = Properties().apply { receipt(file).inputStream().use { load(it) } }
        if (props.getProperty("version") != "1") return null
        val stored = StateIdentity(props.getProperty("serial", ""), props.getProperty("crc", ""))
        val slot = props.getProperty("slot")?.toIntOrNull() ?: return null
        if (!stored.matches(identity) || slot !in 0..9 && slot != -2 || props.getProperty("path") != file.canonicalPath) return null
        val size = props.getProperty("size")?.toLongOrNull() ?: return null
        val modified = props.getProperty("modified")?.toLongOrNull() ?: return null
        if (file.length() != size || file.lastModified() != modified) return null
        val sha = props.getProperty("sha256", "")
        if (sha != digest(file)) return null
        AssociatedState(file.canonicalFile, slot, stored, sha, size, modified)
    }.getOrNull()

    /** Recheck the exact runtime slot target, so changing the active save directory cannot load another file. */
    fun matchesRuntimeTarget(state: AssociatedState, path: String?): Boolean = runCatching {
        !path.isNullOrBlank() && File(path).canonicalFile == state.file &&
            File(path).length() == state.size && File(path).lastModified() == state.modified
    }.getOrDefault(false)
}
