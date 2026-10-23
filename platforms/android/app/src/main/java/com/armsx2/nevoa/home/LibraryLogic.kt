package com.armsx2.nevoa.home

/** Recent games first, then the rest of the library, each [key] once. */
fun <T> libraryOrder(recent: List<T>, all: List<T>, key: (T) -> String): List<T> =
    (recent + all).distinctBy(key)

/** Moves the selection by [delta], stopping at either end instead of wrapping. */
fun moveSelection(current: Int, delta: Int, count: Int): Int =
    if (count <= 0) 0 else (current + delta).coerceIn(0, count - 1)

/** The classic library takes over when asked for, or whenever the row would have nothing to show. */
fun shouldShowClassic(
    initialized: Boolean,
    scanning: Boolean,
    gameCount: Int,
    hasRomDirs: Boolean,
    classicRequested: Boolean,
): Boolean = classicRequested || !hasRomDirs || !initialized || gameCount == 0

/** Text drawn on a case with no cover: the title, else the file name without its extension. */
fun coverLabel(title: String, fileName: String?): String =
    title.trim().ifEmpty {
        fileName?.substringAfterLast('/')?.substringAfterLast(':')?.substringBeforeLast('.')?.trim()?.ifEmpty { null } ?: "?"
    }

/** Position of [key] in [keys], or 0 when it is null or no longer there. */
fun indexOfKey(keys: List<String>, key: String?): Int = keys.indexOf(key).coerceAtLeast(0)
