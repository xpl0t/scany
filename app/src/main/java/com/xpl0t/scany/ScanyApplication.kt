package com.xpl0t.scany

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader

@HiltAndroidApp
class ScanyApplication : Application() {

    init {
        if (!OpenCVLoader.initDebug()) {
            throw Error("Could not initialize opencv")
        }
        System.loadLibrary("yuv-rgb-convert")
    }

}