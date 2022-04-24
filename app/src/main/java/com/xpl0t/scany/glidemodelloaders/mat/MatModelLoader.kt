package com.xpl0t.scany.glidemodelloaders.mat

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import org.opencv.core.Mat
import java.nio.ByteBuffer

/**
 * Loads an [ByteBuffer] for a page in the scany room database.
 */
class MatModelLoader : ModelLoader<Mat, ByteBuffer> {

    override fun buildLoadData(
        model: Mat,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<ByteBuffer> {
        return LoadData(ObjectKey(model.hashCode()), MatDataFetcher(model))
    }

    override fun handles(model: Mat): Boolean {
        return true
    }

}