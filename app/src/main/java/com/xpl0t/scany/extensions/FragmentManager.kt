package com.xpl0t.scany.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.xpl0t.scany.R

fun FragmentManager.showFragment(frag: Fragment, addToBackStack: Boolean = true, bundle: Bundle? = null, container: Int = R.id.mainFrame) {
    if (bundle != null)
        frag.arguments = bundle

    beginTransaction().apply {
        replace(container, frag, null)
        if (addToBackStack) addToBackStack(null)
        commit()
    }
}
