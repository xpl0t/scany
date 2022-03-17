package com.xpl0t.scany.ui.scan

interface ScanFragmentListener {
    fun showScan(id: Int?)
    fun renameTitle()
    fun export()
    fun reorderPages()

    /**
     * Expand bottom sheet fragment.
     */
    fun expand()

    /**
     * Hide bottom sheet fragment.
     */
    fun hide()
}