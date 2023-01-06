package com.xpl0t.scany.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class PageImageStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun getFilePath(id: Int): String {
        return context.filesDir.absolutePath + id
    }

    fun create(id: Int, bytes: ByteArray): ByteArray {
        val file = File(getFilePath(id))

        if (file.exists())
            throw Exception("Image with id $id already exists")

        file.createNewFile()

        val fos = FileOutputStream(file)
        fos.write(bytes)
        fos.close()

        return bytes
    }

    fun read(id: Int): ByteArray {
        val file = File(getFilePath(id))

        if (!file.exists())
            throw Exception("No image with id $id")

        file.createNewFile()

        val bytes = ByteArray(file.length().toInt())
        val fis = FileInputStream(file)
        fis.read(bytes)
        fis.close()

        return bytes
    }

    fun update(id: Int, bytes: ByteArray): ByteArray {
        val file = File(getFilePath(id))

        if (!file.exists())
            throw Exception("No image with id $id")

        val fos = FileOutputStream(file)
        fos.write(bytes)
        fos.close()

        return bytes
    }

    fun delete(id: Int) {
        val file = File(getFilePath(id))

        if (!file.exists())
            throw Exception("No image with id $id")

        file.delete()
    }

}