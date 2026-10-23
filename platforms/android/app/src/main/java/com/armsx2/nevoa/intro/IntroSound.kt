package com.armsx2.nevoa.intro

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File

/**
 * User-chosen sound for the bundled (silent) boot intro. Copied into private storage like
 * BootIntro's custom video, so it survives the source URI going away.
 */
object IntroSound {
    const val MaxBytes = 5L * 1024 * 1024

    fun file(context: Context): File = File(context.filesDir, "nevoa_intro_sound")

    /** Returns null when acceptable, else a PT-BR message. */
    fun validate(sizeBytes: Long, mime: String?): String? = when {
        mime == null || !mime.startsWith("audio/") -> "Escolha um arquivo de áudio."
        sizeBytes > MaxBytes -> "O áudio é grande demais (máximo 5 MB)."
        else -> null
    }

    /** Blocking copy; call off the main thread. Returns null on success, else a PT-BR message. */
    fun import(context: Context, uri: Uri): String? {
        val mime = context.contentResolver.getType(uri)
        validate(0, mime)?.let { return it }
        val target = file(context)
        val temp = File(target.parentFile, "nevoa_intro_sound.part")
        val tooBig = runCatching {
            context.contentResolver.openInputStream(uri)?.use { ins ->
                temp.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val n = ins.read(buf)
                        if (n < 0) break
                        total += n
                        // Cap enforced while copying: providers may not report a size.
                        if (total > MaxBytes) return@runCatching true
                        out.write(buf, 0, n)
                    }
                }
                false
            }
        }.getOrNull()
        if (tooBig == true) {
            temp.delete()
            return validate(MaxBytes + 1, mime)
        }
        if (tooBig == null || temp.length() == 0L) {
            temp.delete()
            return "Não foi possível ler o arquivo."
        }
        target.delete()
        if (!temp.renameTo(target)) {
            temp.delete()
            return "Não foi possível ler o arquivo."
        }
        return null
    }

    fun clear(context: Context) {
        file(context).delete()
    }

    fun exists(context: Context): Boolean = file(context).length() > 0L

    /** Starts playback; null (silently) if the file is missing or corrupt. Caller must release. */
    fun start(context: Context): MediaPlayer? {
        if (!exists(context)) return null
        val mp = MediaPlayer()
        return try {
            mp.setDataSource(file(context).absolutePath)
            mp.isLooping = false
            mp.prepare()
            mp.start()
            mp
        } catch (_: Exception) {
            runCatching { mp.release() }
            null
        }
    }
}
