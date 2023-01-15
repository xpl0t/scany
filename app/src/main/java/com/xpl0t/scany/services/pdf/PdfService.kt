package com.xpl0t.scany.services.pdf

import android.content.Context
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import com.xpl0t.scany.extensions.scale
import com.xpl0t.scany.extensions.toBitmap
import com.xpl0t.scany.services.pdf.scalecalculator.ScaleCalculatorList
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class PdfService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scaleCalculators: ScaleCalculatorList
) {

    fun getPdfFromImages(images: List<ByteArray>, mediaSize: PrintAttributes.MediaSize, scaleType: ScaleType): ByteArray {
        val scaleCalculator = scaleCalculators.find { it.scaleType == scaleType }!!
        val outputStream = ByteArrayOutputStream()

        val printAttrs = PrintAttributes.Builder()
            .setMediaSize(mediaSize)
            .setMinMargins(PrintAttributes.Margins(0, 0, 0, 0))
            // .setResolution(PrintAttributes.Resolution("hd", "hd", 300, 300))
            .build()

        val document = PrintedPdfDocument(context, printAttrs)
        val docAspectRatio = document.pageWidth.toFloat() / document.pageHeight.toFloat()

        for (i in images.indices) {
            val pageInfo = PdfDocument.PageInfo.Builder((docAspectRatio * 2000).toInt(),2000, i).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val mat = Imgcodecs.imdecode(MatOfByte(*images[i]), Imgcodecs.IMREAD_UNCHANGED)

            val scaledSize = scaleCalculator.getSize(
                Rect(0, 0, canvas.width, canvas.height),
                Rect(0, 0, mat.width(), mat.height())
            )
            val offset = scaleCalculator.getOffset(
                Rect(0, 0, canvas.width, canvas.height),
                Rect(0, 0, mat.width(), mat.height())
            )

            mat.scale(scaledSize.width().toDouble(), scaledSize.height().toDouble(), false)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB)
            val bitmap = mat.toBitmap()

            canvas.drawBitmap(
                bitmap,
                offset.x.toFloat(),
                offset.y.toFloat(),
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