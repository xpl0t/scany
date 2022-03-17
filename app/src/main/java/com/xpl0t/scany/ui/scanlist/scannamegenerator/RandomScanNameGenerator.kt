package com.xpl0t.scany.ui.scanlist.scannamegenerator

import android.content.Context
import com.xpl0t.scany.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RandomScanNameGenerator @Inject constructor(
    @ApplicationContext val context: Context
) : ScanNameGenerator {

    /**
     * Generate random scan name.
     */
    override fun generate(): String {
        val scanNames = context.resources.getStringArray(R.array.scan_names)
        return scanNames.random()
    }

}