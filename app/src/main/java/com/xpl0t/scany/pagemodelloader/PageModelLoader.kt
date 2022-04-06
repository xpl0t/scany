package com.xpl0t.scany.pagemodelloader

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.repository.Repository
import java.nio.ByteBuffer

/**
 * Loads an [ByteBuffer] for a page in the scany room database.
 */
class PageModelLoader(
    private val repo: Repository
) : ModelLoader<Page, ByteBuffer> {

    override fun buildLoadData(
        model: Page,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<ByteBuffer> {
        return LoadData(ObjectKey(model.id), PageDataFetcher(model, repo))
    }

    override fun handles(model: Page): Boolean {
        return model.id > 0
    }

}