package com.xpl0t.scany.extensions

import android.content.Context
import java.io.File

fun Context.phoenixExecutablePath(): String {
    return filesDir.absolutePath + File.separator + "phoenix";
}
