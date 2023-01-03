package com.xpl0t.scany.services.backpress

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackPressHandlerService @Inject constructor() {

    private var handler: BackPressHandler? = null

    fun setHandler(handler: BackPressHandler) {
        this.handler = handler
    }

    fun resetHandler() {
        handler = null
    }

    fun getHandler(): BackPressHandler? {
        return handler
    }

}