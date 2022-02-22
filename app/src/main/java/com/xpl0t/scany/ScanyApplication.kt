package com.xpl0t.scany

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScanyApplication : Application() {

    init {
        System.loadLibrary("opencv_java4")
        System.loadLibrary("yuv-rgb-convert")
    }

}