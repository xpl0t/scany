package com.xpl0t.scany.services

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.FileUtils
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import com.xpl0t.scany.extensions.scale
import com.xpl0t.scany.extensions.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class PdfService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getPdfFromImages(images: List<ByteArray>): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val printAttrs = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setMinMargins(PrintAttributes.Margins(0, 0, 0, 0))
            .build()

        val document = PrintedPdfDocument(context, printAttrs)

        for (i in images.indices) {
            val page = document.startPage(i)
            val canvas = page.canvas
            val mat = Imgcodecs.imdecode(MatOfByte(*images[i]), Imgcodecs.IMREAD_UNCHANGED)

            val aspectRatioCanvas = canvas.width.toFloat() / canvas.height.toFloat()
            val aspectRatioImg = mat.width().toFloat() / mat.height().toFloat()
            val scaleByWidth = aspectRatioImg > aspectRatioCanvas

            val scaledWidth = if (scaleByWidth) canvas.width.toDouble()
                else canvas.height * aspectRatioImg.toDouble()

            val scaledHeight = if (!scaleByWidth) canvas.height.toDouble()
                else canvas.width / aspectRatioImg.toDouble()

            mat.scale(scaledWidth, scaledHeight, false)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB)
            val bitmap = mat.toBitmap()

            canvas.drawBitmap(
                bitmap,
                if (scaleByWidth) 0f else (canvas.width - mat.width()) / 2f,
                if (scaleByWidth) (canvas.height - mat.height()) / 2f else 0f,
                null
            )

            document.finishPage(page)
        }

        document.writeTo(outputStream)
        document.close()

        val byteArray = outputStream.toByteArray()
        outputStream.close()
        return byteArray
    }

}