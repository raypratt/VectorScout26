package com.example.vectorscout26.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QRCodeGenerator {
    /**
     * Generate a QR code bitmap from the given content
     */
    fun generateQRCode(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                // Use M (medium) error correction for better capacity (~2,331 chars vs ~1,273 with H)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate a QR code with a text label below it for saving.
     */
    fun generateQRCodeWithLabel(
        content: String,
        label: String,
        qrSize: Int = 512
    ): Bitmap? {
        Log.d("QRCodeGenerator", "generateQRCodeWithLabel called, label: $label, qrSize: $qrSize")

        val qrBitmap = generateQRCode(content, qrSize, qrSize)
        if (qrBitmap == null) {
            Log.e("QRCodeGenerator", "Failed to generate base QR code")
            return null
        }
        Log.d("QRCodeGenerator", "Base QR generated: ${qrBitmap.width}x${qrBitmap.height}")

        // Calculate text height
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = qrSize / 12f  // Scale text with QR size
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val padding = (qrSize * 0.04f).toInt()
        val textHeight = (textPaint.descent() - textPaint.ascent()).toInt()
        val totalHeight = qrSize + textHeight + padding * 3

        Log.d("QRCodeGenerator", "Creating combined bitmap: ${qrSize + padding * 2}x$totalHeight")

        // Create combined bitmap
        val combinedBitmap = Bitmap.createBitmap(qrSize + padding * 2, totalHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(combinedBitmap)

        // Fill background white
        canvas.drawColor(Color.WHITE)

        // Draw QR code
        canvas.drawBitmap(qrBitmap, padding.toFloat(), padding.toFloat(), null)

        // Draw text label centered below QR
        val textX = (qrSize + padding * 2) / 2f
        val textY = qrSize + padding * 2 - textPaint.ascent()
        canvas.drawText(label, textX, textY, textPaint)

        Log.d("QRCodeGenerator", "Combined bitmap created successfully")
        return combinedBitmap
    }
}
