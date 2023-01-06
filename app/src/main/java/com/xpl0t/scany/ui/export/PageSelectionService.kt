package com.xpl0t.scany.ui.export

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageSelectionService @Inject constructor() {

    // Page Id -> is selected map
    private val selectionMap = mutableMapOf<Int, Boolean>()

    fun setSelected(pageId: Int, selected: Boolean) {
        selectionMap[pageId] = selected
    }

    fun isSelected(pageId: Int): Boolean {
        return selectionMap[pageId] ?: true // Falls back to selected == true, if page is currently not persisted in the map.
    }

    fun getDeselectedPages(): List<Int> {
        return selectionMap
            .filter { !it.value }
            .map { it.key }
    }

    fun reset() {
        selectionMap.clear()
    }

}