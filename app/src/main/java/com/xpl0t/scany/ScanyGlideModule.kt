package com.xpl0t.scany

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.pagemodelloader.PageModelLoaderFactory
import java.nio.ByteBuffer

@GlideModule
class ScanyGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val factory = PageModelLoaderFactory(context)
        registry.prepend(Page::class.java, ByteBuffer::class.java, factory)
    }

}