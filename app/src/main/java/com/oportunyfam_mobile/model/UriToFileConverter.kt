package com.oportunyfam_mobile_ong.model


import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun Context.getRealPathFromURI(uri: Uri): String? {
    return try {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
