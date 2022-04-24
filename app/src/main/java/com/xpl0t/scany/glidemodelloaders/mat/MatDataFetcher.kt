package com.xpl0t.scany.glidemodelloaders.mat

import androidx.annotation.NonNull
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer


class MatDataFetcher(private val model: Mat) : DataFetcher<ByteBuffer> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ByteBuffer>) {
        val correctedMat = Mat()
        Imgproc.cvtColor(model, correctedMat, Imgproc.COLOR_BGR2RGB)

        val bytes = MatOfByte()
        Imgcodecs.imencode(".bmp", correctedMat, bytes)
        val buf = ByteBuffer.wrap(bytes.toArray())

        callback.onDataReady(buf)
    }

    override fun cleanup() {
    }

    override fun cancel() {
    }

    @NonNull
    override fun getDataClass(): Class<ByteBuffer> {
        return ByteBuffer::class.java
    }

    @NonNull
    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}