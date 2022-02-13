package com.xpl0t.scany.ui.common

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

open class BaseFragment(@LayoutRes layoutRes: Int): Fragment(layoutRes) {

    /*protected val supportActionBar: ActionBar
        get() = (activity as AppCompatActivity).supportActionBar!!*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set defaults
        /*if (toolbarId != -2) {
            if (titleId == -1) titleId = R.string.app_name
            if (toolbarId == -1) toolbarId = R.id.toolbar
        }*/
    }

    override fun onResume() {
        super.onResume()

        /*if (setToolbarTitle && toolbarId != -2) {
            activity!!.findViewById<Toolbar>(R.id.toolbar)?.visibility = if (toolbarId == R.id.toolbar) View.VISIBLE else View.GONE
            (activity!! as AppCompatActivity).setSupportActionBar(activity!!.findViewById(toolbarId))
            supportActionBar.title = getString(titleId)
        }

        // Set back button
        if (toolbarId != -2) {
            supportActionBar.setDisplayShowHomeEnabled(subFragment)
            supportActionBar.setDisplayHomeAsUpEnabled(subFragment)
            if (subFragment)
                setHasOptionsMenu(true)
        }*/
    }

    override fun onPause() {
        super.onPause()

        /*if (toolbarId != -2) {
            supportActionBar.setDisplayShowHomeEnabled(false)
            supportActionBar.setDisplayHomeAsUpEnabled(false)
        }*/
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return true
    }*/
}