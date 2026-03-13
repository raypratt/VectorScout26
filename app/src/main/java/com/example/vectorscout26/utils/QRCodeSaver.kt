package com.example.vectorscout26.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Save a QR code image (with label) to the Downloads folder.
 * Returns the saved path on success, null on failure.
 * Also saves a copy to internal storage as a fallback.
 */
suspend fun saveQRCodeImageToDownloads(
    context: Context,
    jsonContent: String,
    label: String,
    filename: String
): String? = withContext(Dispatchers.IO) {
    try {
        val bitmap = QRCodeGenerator.generateQRCodeWithLabel(jsonContent, label, 800)
            ?: return@withContext null

        // Always save to internal storage first (guaranteed to work)
        val qrDir = File(context.filesDir, "qrcodes")
        if (!qrDir.exists()) qrDir.mkdirs()
        val internalFile = File(qrDir, filename)
        FileOutputStream(internalFile).use { it ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        // Try to save to a user-visible Downloads location
        var visiblePath: String? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "image/png")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    visiblePath = "Downloads/$filename"
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val downloadFile = File(downloadsDir, filename)
                FileOutputStream(downloadFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                MediaScannerConnection.scanFile(
                    context, arrayOf(downloadFile.absolutePath), arrayOf("image/png"), null
                )
                visiblePath = downloadFile.absolutePath
            }
        } catch (e: Exception) {
            Log.w("QRCodeSaver", "Could not save to visible location: ${e.message}")
        }

        visiblePath ?: internalFile.absolutePath
    } catch (e: Exception) {
        Log.e("QRCodeSaver", "Error saving QR code", e)
        null
    }
}
