package com.xpl0t.scany.ui.scanimage.camera

import org.opencv.core.Mat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraService @Inject() constructor() {
    var document: Mat? = null
}
