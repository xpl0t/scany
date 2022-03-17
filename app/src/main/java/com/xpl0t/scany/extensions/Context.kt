package com.xpl0t.scany.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.content.res.AppCompatResources
import java.io.File

fun Context.phoenixExecutablePath(): String {
    return filesDir.absolutePath + File.separator + "phoenix";
}

fun Context.getThemeColor(@AttrRes attribute: Int): ColorStateList {
    TypedValue().let {
        theme.resolveAttribute(attribute, it, true)
        return AppCompatResources.getColorStateList(this, it.resourceId)
    }
}
