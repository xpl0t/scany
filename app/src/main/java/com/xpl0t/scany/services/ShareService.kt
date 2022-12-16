package com.xpl0t.scany.services

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ShareService @Inject() constructor() {

    fun shareImage(context: Context, img: ByteArray) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "page")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        try {
            val outStream = context.contentResolver.openOutputStream(uri!!)
            outStream?.write(img)
            outStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Could not write image to media store", e)
            return
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(context, shareIntent, null)
    }

    companion object {
        const val TAG = "ShareService"
    }

}