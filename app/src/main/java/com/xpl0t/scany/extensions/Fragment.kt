package com.xpl0t.scany.extensions

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

// Taken from: https://stackoverflow.com/a/59655582
/**
 * Run action on UI thread.
 *
 * @param action Action to run on UI thread.
 */
fun Fragment.runOnUiThread(action: () -> Unit) {
    if (!isAdded) return // Fragment not attached to an activity.
    activity?.runOnUiThread(action)
}

/**
 * Closes fragment.
 */
fun Fragment.finish() {
    findNavController().popBackStack()
}
