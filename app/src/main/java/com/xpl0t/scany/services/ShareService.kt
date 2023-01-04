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

    fun share(context: Context, data: ByteArray, mimeType: String) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "data")
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            values
        )

        val outStream = context.contentResolver.openOutputStream(uri!!)
        outStream?.write(data)
        outStream?.close()

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(context, shareIntent, null)
    }

    companion object {
        const val TAG = "ShareService"
    }

}