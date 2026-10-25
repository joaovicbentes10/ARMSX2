package com.armsx2.nevoa.intro

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File
import com.armsx2.R

/**
 * User-chosen override for the bundled boot intro soundtrack. Copied into private storage like
 * BootIntro's custom video, so it survives the source URI going away.
 */
object IntroSound {
    const val MaxBytes = 5L * 1024 * 1024

    fun file(context: Context): File = File(context.filesDir, "nevoa_intro_sound")

    /** Returns null when acceptable, else a PT-BR message. */
    fun validate(sizeBytes: Long, mime: String?): String? = when {
        mime == null || !(mime.startsWith("audio/") || mime.equals("application/ogg", ignoreCase = true)) -> com.armsx2.i18n.I18n.get("nevoa.chooseAudio")
        sizeBytes > MaxBytes -> com.armsx2.i18n.I18n.get("nevoa.audioTooBig")
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
            return com.armsx2.i18n.I18n.get("nevoa.readFailed")
        }
        target.delete()
        if (!temp.renameTo(target)) {
            temp.delete()
            return com.armsx2.i18n.I18n.get("nevoa.readFailed")
        }
        return null
    }

    fun clear(context: Context) {
        file(context).delete()
    }

    fun exists(context: Context): Boolean = file(context).length() > 0L

    /** User override first, otherwise the bundled Névoa track. Caller must release. */
    fun start(context: Context): MediaPlayer? {
        val useCustom = exists(context)
        val mp = MediaPlayer()
        return try {
            if (useCustom) {
                mp.setDataSource(file(context).absolutePath)
            } else {
                mp.setDataSource(context, Uri.parse("android.resource://${context.packageName}/${R.raw.nevoa_intro_sound}"))
            }
            mp.isLooping = false
            mp.setOnPreparedListener { player ->
                runCatching { player.start() }
                    .onSuccess { android.util.Log.d("NevoaIntroSound", "Playback started: ${if (useCustom) "custom" else "bundled"}") }
                    .onFailure { android.util.Log.w("NevoaIntroSound", "Could not start intro audio", it) }
            }
            mp.setOnErrorListener { player, what, extra ->
                android.util.Log.w("NevoaIntroSound", "Intro audio error: $what/$extra")
                runCatching { player.release() }
                true
            }
            mp.prepareAsync()
            mp
        } catch (_: Exception) {
            runCatching { mp.release() }
            null
        }
    }
}
