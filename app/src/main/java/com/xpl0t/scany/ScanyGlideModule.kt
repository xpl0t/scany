package com.xpl0t.scany

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.xpl0t.scany.glidemodelloaders.mat.MatModelLoaderFactory
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.glidemodelloaders.page.PageModelLoaderFactory
import org.opencv.core.Mat
import java.nio.ByteBuffer

@GlideModule
class ScanyGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(Page::class.java, ByteBuffer::class.java, PageModelLoaderFactory(context))
        registry.prepend(Mat::class.java, ByteBuffer::class.java, MatModelLoaderFactory())
    }

}