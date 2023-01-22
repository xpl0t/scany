package com.xpl0t.scany

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.xpl0t.scany.glidemodelloaders.mat.MatModelLoaderFactory
import com.xpl0t.scany.glidemodelloaders.page.PageModelLoaderFactory
import com.xpl0t.scany.models.Page
import org.opencv.core.Mat
import java.nio.ByteBuffer


@GlideModule
class ScanyGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(Page::class.java, ByteBuffer::class.java, PageModelLoaderFactory(context))
        registry.prepend(Mat::class.java, ByteBuffer::class.java, MatModelLoaderFactory())
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions {
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        }
    }
}