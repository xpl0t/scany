package com.xpl0t.scany.glidemodelloaders.page

import android.content.Context
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.repository.RepositoryImpl
import java.nio.ByteBuffer

class PageModelLoaderFactory constructor(
    private val context: Context
) : ModelLoaderFactory<Page, ByteBuffer> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Page, ByteBuffer> {
        val repo = RepositoryImpl(context)
        return PageModelLoader(repo)
    }

    override fun teardown() {}

}