package com.xpl0t.scany.ui.scanlist.scannamegenerator

import io.reactivex.rxjava3.core.Observable

interface ScanNameGenerator {

    /**
     * Generate scan name.
     */
    fun generate(): Observable<String>

}