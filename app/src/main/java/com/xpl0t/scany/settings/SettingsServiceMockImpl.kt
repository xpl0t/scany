package com.xpl0t.scany.settings

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.just

class SettingsServiceMockImpl : SettingsService {

    override fun getSettings(): Observable<Settings> {
        val settings = Settings(
            debugMode = true
        )
        return just(settings)
    }

}