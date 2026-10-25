package com.armsx2.nevoa.intro

/** Legacy/default mode 0 must not release the landscape lock when emulation starts. */
fun nevoaOrientation(mode: Int): Int = when (mode) {
    2, 3 -> mode // Explicit portrait and auto-rotate remain available in settings.
    else -> 1
}
