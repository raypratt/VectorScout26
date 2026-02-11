package com.example.vectorscout26.ui.qrcode

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.vectorscout26.data.database.ScoutDatabase
import com.example.vectorscout26.data.model.PitScoutData
import com.example.vectorscout26.data.repository.PitScoutRepository
import com.example.vectorscout26.utils.PitJsonSerializer
import com.example.vectorscout26.utils.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun PitQRCodeScreen(
    pitScoutId: Long,
    onNewPitScout: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pitData by remember { mutableStateOf<PitScoutData?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var jsonContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Permission launcher for storage access (Android 9 and below)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        scope.launch {
            val data = pitData
            if (data != null) {
                val pitLabel = "Pit_${data.teamNumber}"
                val filename = "$pitLabel.png"

                val savedPath = savePitQRCodeImage(context, jsonContent, pitLabel, filename, isGranted)
                if (savedPath != null) {
                    Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to save QR code", Toast.LENGTH_LONG).show()
                }
            }
            onNewPitScout()
        }
    }

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(pitScoutId) {
        scope.launch {
            try {
                val database = ScoutDatabase.getDatabase(context)
                val repository = PitScoutRepository(database.pitScoutDao())

                val data = repository.getPitScoutById(pitScoutId)
                pitData = data

                if (data != null) {
                    val json = PitJsonSerializer.toCompactJson(data)
                    jsonContent = json
                    val bitmap = QRCodeGenerator.generateQRCode(json, 800, 800)
                    qrBitmap = bitmap
                    isLoading = false
                } else {
                    error = "Pit scout data not found"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = "Error loading pit scout data: ${e.message}"
                isLoading = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pit Scout Complete",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            pitData?.let { data ->
                Text(
                    text = "Team ${data.teamNumber}",
                    style = MaterialTheme.typography.titleLarge
                )
                if (data.event.isNotBlank()) {
                    Text(
                        text = data.event,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${data.drivetrainType} | ${data.preferredRole}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${data.autoPaths.size} auto path(s) recorded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Text(
                        text = "Generating QR Code...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                error != null -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                qrBitmap != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    pitData?.let { data ->
                        val pitLabel = "Pit_${data.teamNumber}"
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = pitLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan this QR code to upload pit data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "JSON: ${jsonContent.length} chars",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Pit QR JSON", jsonContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Copy JSON to Clipboard")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    fun saveAndNavigate() {
                        scope.launch {
                            val data = pitData
                            if (data != null) {
                                val pitLabel = "Pit_${data.teamNumber}"
                                val filename = "$pitLabel.png"

                                val savedPath = savePitQRCodeImage(context, jsonContent, pitLabel, filename, hasStoragePermission())
                                if (savedPath != null) {
                                    Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to save QR code", Toast.LENGTH_LONG).show()
                                }
                            }
                            onNewPitScout()
                        }
                    }

                    if (hasStoragePermission()) {
                        saveAndNavigate()
                    } else {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(84.dp)
            ) {
                Text("Scout Another Team", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onHome,
                modifier = Modifier.fillMaxWidth().height(84.dp)
            ) {
                Text("Back to Home", fontSize = 24.sp)
            }
        }
    }
}

private suspend fun savePitQRCodeImage(
    context: Context,
    jsonContent: String,
    label: String,
    filename: String,
    hasStoragePermission: Boolean = false
): String? = withContext(Dispatchers.IO) {
    try {
        Log.d("PitQRCodeScreen", "Attempting to save QR code: $filename")

        val bitmap = QRCodeGenerator.generateQRCodeWithLabel(jsonContent, label, 800)
        if (bitmap == null) {
            Log.e("PitQRCodeScreen", "Failed to generate QR bitmap")
            return@withContext null
        }

        // Save to internal storage
        val qrDir = File(context.filesDir, "qrcodes")
        if (!qrDir.exists()) {
            qrDir.mkdirs()
        }
        val internalFile = File(qrDir, filename)
        FileOutputStream(internalFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        // Try to save to visible location
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
            } else if (hasStoragePermission) {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val downloadFile = File(downloadsDir, filename)
                FileOutputStream(downloadFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(downloadFile.absolutePath),
                    arrayOf("image/png"),
                    null
                )
                visiblePath = downloadFile.absolutePath
            }
        } catch (e: Exception) {
            Log.w("PitQRCodeScreen", "Could not save to visible location: ${e.message}")
        }

        visiblePath ?: internalFile.absolutePath
    } catch (e: Exception) {
        Log.e("PitQRCodeScreen", "Error saving QR code", e)
        null
    }
}
