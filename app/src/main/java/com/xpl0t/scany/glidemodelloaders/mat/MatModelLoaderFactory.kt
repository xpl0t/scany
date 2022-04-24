package com.xpl0t.scany.glidemodelloaders.mat

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import org.opencv.core.Mat
import java.nio.ByteBuffer

class MatModelLoaderFactory : ModelLoaderFactory<Mat, ByteBuffer> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Mat, ByteBuffer> {
        return MatModelLoader()
    }

    override fun teardown() {}

}