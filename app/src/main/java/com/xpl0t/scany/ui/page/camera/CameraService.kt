package com.xpl0t.scany.ui.page.camera

import org.opencv.core.Mat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraService @Inject() constructor() {
    var page: Mat? = null
}
