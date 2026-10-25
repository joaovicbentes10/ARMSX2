package com.armsx2.nevoa.saves

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import com.armsx2.GameInfo
import com.armsx2.runtime.MainActivityRuntime
import kr.co.iefriends.pcsx2.NativeApp
import java.io.File

object GameStateCatalog {
    val revision = mutableIntStateOf(0)

    /** Call on the same worker, immediately after a native save succeeds. */
    fun record(slot: Int) {
        runCatching {
            val identity = runningIdentity() ?: return
            val game = MainActivityRuntime.currentGame.value ?: return
            if (!identity.serial.equals(game.serial, true)) return
            val path = if (slot == -2) NativeApp.getAutosaveGamePath() else NativeApp.getGamePathSlot(slot)
            if (!path.isNullOrBlank() && StateAssociation.write(File(path), slot, identity, game.uri.toString()) != null) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { revision.intValue++ }
            }
        }.onFailure { android.util.Log.w("NevoaStates", "Could not associate save", it) }
    }

    // This JNI getter reports the disc identity used by the native save-slot resolver.
    // getGameCRC reports the executing ELF, and probing the ROM is unavailable while CDVD is held.
    fun runningIdentity(): StateIdentity? = StateAssociation.parseNativeIdentity(NativeApp.getPauseGameSerial().orEmpty())

    /** Blocking; the source URI was linked to this disc identity by a successful native operation. */
    fun list(context: Context, game: GameInfo): List<AssociatedState> {
        if (game.serial.isNullOrBlank()) return emptyList()
        val roots = listOfNotNull(
            runCatching { MainActivityRuntime.assetCopyRoot(context) }.getOrNull(),
            context.getExternalFilesDir(null)?.absolutePath,
        ).distinct()
        val result = roots.flatMap { root -> listOf("sstates", "savestates").flatMap { folder ->
            val dir = File(root, folder)
            if (!dir.isDirectory) emptyList() else dir.walkTopDown().maxDepth(3)
                .onEnter { it == dir || it.name != "backups" }
                .filter { it.isFile && it.extension.equals("p2s", true) }
                .mapNotNull { StateAssociation.readForSource(it, game.uri.toString(), game.serial) }.toList()
        } }.distinctBy { it.file.path }.sortedByDescending { it.modified }.distinctBy { it.slot }
        android.util.Log.d("NevoaStates", "Catalog serial=${game.serial} states=${result.size}")
        return result
    }
}
