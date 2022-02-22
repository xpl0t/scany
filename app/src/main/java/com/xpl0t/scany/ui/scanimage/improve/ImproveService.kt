package com.xpl0t.scany.ui.scanimage.improve

import android.graphics.Bitmap
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImproveService @Inject() constructor() {
    val documentSubject = PublishSubject.create<Bitmap>()
}