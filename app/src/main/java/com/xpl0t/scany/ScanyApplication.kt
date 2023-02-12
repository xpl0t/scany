package com.xpl0t.scany

import android.app.Application
import com.xpl0t.scany.services.BillingService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ScanyApplication : Application() {

    @Inject() lateinit var billingService: BillingService

    init {
        System.loadLibrary("opencv_java4")
        System.loadLibrary("yuv-rgb-convert")
    }

    override fun onCreate() {
        super.onCreate()
        billingService.init()
    }
}