package com.example.contacts

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PhotoRepository(private val context: Context) {

    fun saveFromUri(source: Uri): String {
        val resolver: ContentResolver = context.contentResolver
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 28) {
            val src = ImageDecoder.createSource(resolver, source)
            ImageDecoder.decodeBitmap(src)
        } else {
            @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(resolver, source)
        }
        return saveBitmap(bitmap)
    }

    fun saveBitmap(bitmap: Bitmap): String {
        val fileName = "contact_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun deletePhoto(path: String?) {
        path ?: return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
}

