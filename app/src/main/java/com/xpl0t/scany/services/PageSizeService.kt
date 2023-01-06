package com.xpl0t.scany.services

import android.content.Context
import android.print.PrintAttributes
import com.xpl0t.scany.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PageSizeService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val defaultMediaSize = PrintAttributes.MediaSize.ISO_A4
    private val pageSizeMap = getPageSizeMap()

    private fun getPageSizeMap(): Map<String, PrintAttributes.MediaSize> {
        val pageSizeStrings = context.resources.getStringArray(R.array.page_sizes)

        // It is crucial, that the resource string array is ordered correctly!
        return mapOf(
            Pair(pageSizeStrings[0], PrintAttributes.MediaSize.NA_LEGAL),
            Pair(pageSizeStrings[1], PrintAttributes.MediaSize.NA_LETTER),
            Pair(pageSizeStrings[2], PrintAttributes.MediaSize.NA_TABLOID),
            Pair(pageSizeStrings[3], PrintAttributes.MediaSize.ISO_A0),
            Pair(pageSizeStrings[4], PrintAttributes.MediaSize.ISO_A1),
            Pair(pageSizeStrings[5], PrintAttributes.MediaSize.ISO_A2),
            Pair(pageSizeStrings[6], PrintAttributes.MediaSize.ISO_A3),
            Pair(pageSizeStrings[7], PrintAttributes.MediaSize.ISO_A4),
            Pair(pageSizeStrings[8], PrintAttributes.MediaSize.ISO_A5)
        )
    }

    fun getDefaultPageSizeString(): String {
        return pageSizeMap.filter { it.value == defaultMediaSize }.keys.first()
    }

    fun getPageSizeStrings(): List<String> {
        return pageSizeMap.keys.toList()
    }

    fun getMediaSizeForPageSizeStr(pageSizeStr: String): PrintAttributes.MediaSize? {
        return pageSizeMap[pageSizeStr]
    }

}