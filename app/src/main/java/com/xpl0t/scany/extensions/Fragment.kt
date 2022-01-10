package com.xpl0t.scany.extensions

import androidx.fragment.app.Fragment

// Taken from: https://stackoverflow.com/a/59655582
// Modified comment.
fun Fragment?.runOnUiThread(action: () -> Unit) {
    this ?: return
    if (!isAdded) return // Fragment not attached to an activity.
    activity?.runOnUiThread(action)
}
