package com.armsx2.nevoa.saves

import java.io.File

/** The native slot path is a proposed filename, even when no state has been written. */
fun existingSaveStatePath(path: String?): String? =
    path?.takeIf { it.isNotBlank() && File(it).isFile }
