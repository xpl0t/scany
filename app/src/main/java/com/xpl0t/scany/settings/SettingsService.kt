package com.xpl0t.scany.settings

import io.reactivex.rxjava3.core.Observable

interface SettingsService {

    fun getSettings(): Observable<Settings>

}