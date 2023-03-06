package com.xpl0t.scany.ui.documentlist.documentnamegenerator

import io.reactivex.rxjava3.core.Observable

interface DocumentNameGenerator {

    /**
     * Generate document name.
     */
    fun generate(): Observable<String>

}