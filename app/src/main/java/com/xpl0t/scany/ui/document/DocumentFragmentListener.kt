package com.xpl0t.scany.ui.document

interface DocumentFragmentListener {
    fun showDocument(id: Int?)
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