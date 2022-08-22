package com.example.edgedetectioninterview.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.InputStream

class BitmapHelper {
    fun showBitmap(
        context: Context?,
        bitmap: Bitmap,
        imageView: ImageView?,
    ) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data: ByteArray = baos.toByteArray()
        if (context != null) {
            if (imageView != null) {
                Glide.with(context).load(data).into(imageView)
            }
        }
    }

    fun readBitmapFromPath(context: Context, path: Uri?): Bitmap {
        val stream: InputStream? = path?.let { context.contentResolver.openInputStream(it) }
        val bitmap = BitmapFactory.decodeStream(stream)
        stream?.close()
        return bitmap
    }

}

