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
import com.example.vectorscout26.data.model.MatchScoutData
import com.example.vectorscout26.data.repository.ScoutRepository
import com.example.vectorscout26.utils.JsonSerializer
import com.example.vectorscout26.utils.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun QRCodeScreen(
    matchScoutId: Long,
    onNewMatch: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var matchData by remember { mutableStateOf<MatchScoutData?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var jsonContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Permission launcher for storage access (Android 9 and below)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // After permission result, save and navigate
        scope.launch {
            val data = matchData
            if (data != null) {
                val matchLabel = "${data.event}_${data.matchNumber}_${data.robotDesignation}_${data.teamNumber}"
                val filename = "$matchLabel.png"

                val savedPath = saveQRCodeImage(context, jsonContent, matchLabel, filename, isGranted)
                if (savedPath != null) {
                    Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to save QR code", Toast.LENGTH_LONG).show()
                }
            }
            onNewMatch()
        }
    }

    // Check if we have storage permission
    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true  // Android 10+ doesn't need permission for MediaStore
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(matchScoutId) {
        scope.launch {
            try {
                val database = ScoutDatabase.getDatabase(context)
                val repository = ScoutRepository(database.matchScoutDao())

                // Load match data
                val data = repository.getMatchScoutById(matchScoutId)
                matchData = data

                if (data != null) {
                    // Generate QR code
                    val json = JsonSerializer.toCompactJson(data)
                    jsonContent = json
                    val bitmap = QRCodeGenerator.generateQRCode(json, 800, 800)
                    qrBitmap = bitmap
                    isLoading = false
                } else {
                    error = "Match data not found"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = "Error loading match data: ${e.message}"
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
                text = "Match Scout Complete",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            matchData?.let { data ->
                Text(
                    text = "${data.event} - Match ${data.matchNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Team ${data.teamNumber} (${data.robotDesignation})",
                    style = MaterialTheme.typography.bodyLarge
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

                    // Match info label (same text that will be saved in image)
                    matchData?.let { data ->
                        val matchLabel = "${data.event}_${data.matchNumber}_${data.robotDesignation}_${data.teamNumber}"
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = matchLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan this QR code to upload data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Show JSON size and copy button for debugging
                    Text(
                        text = "JSON: ${jsonContent.length} chars",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("QR JSON", jsonContent)
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
                            val data = matchData
                            if (data != null) {
                                val matchLabel = "${data.event}_${data.matchNumber}_${data.robotDesignation}_${data.teamNumber}"
                                val filename = "$matchLabel.png"

                                val savedPath = saveQRCodeImage(context, jsonContent, matchLabel, filename, hasStoragePermission())
                                if (savedPath != null) {
                                    Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to save QR code", Toast.LENGTH_LONG).show()
                                }
                            }
                            onNewMatch()
                        }
                    }

                    // Check permission before saving
                    if (hasStoragePermission()) {
                        saveAndNavigate()
                    } else {
                        // Request permission, then save
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(84.dp)
            ) {
                Text("Scout New Match", fontSize = 24.sp)
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

/**
 * Save QR code with label as PNG image.
 * Returns the saved file path on success, null on failure.
 */
private suspend fun saveQRCodeImage(
    context: android.content.Context,
    jsonContent: String,
    label: String,
    filename: String,
    hasStoragePermission: Boolean = false
): String? = withContext(Dispatchers.IO) {
    try {
        Log.d("QRCodeScreen", "Attempting to save QR code: $filename")
        Log.d("QRCodeScreen", "Android SDK version: ${Build.VERSION.SDK_INT}")

        // Generate QR with label
        val bitmap = QRCodeGenerator.generateQRCodeWithLabel(jsonContent, label, 800)
        if (bitmap == null) {
            Log.e("QRCodeScreen", "Failed to generate QR bitmap")
            return@withContext null
        }

        // First save to internal storage (guaranteed to work)
        val qrDir = File(context.filesDir, "qrcodes")
        if (!qrDir.exists()) {
            qrDir.mkdirs()
        }
        val internalFile = File(qrDir, filename)
        FileOutputStream(internalFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        Log.d("QRCodeScreen", "Saved to internal: ${internalFile.absolutePath}, size: ${internalFile.length()}")

        // Now try to save to a user-visible location
        var visiblePath: String? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Use MediaStore Downloads (no permission needed)
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
                    Log.d("QRCodeScreen", "Saved to Downloads via MediaStore: $uri")
                }
            } else if (hasStoragePermission) {
                // Android 9 and below: Save to public Downloads folder directly (needs permission)
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val downloadFile = File(downloadsDir, filename)
                FileOutputStream(downloadFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                // Notify media scanner
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(downloadFile.absolutePath),
                    arrayOf("image/png"),
                    null
                )

                visiblePath = downloadFile.absolutePath
                Log.d("QRCodeScreen", "Saved to Downloads: ${downloadFile.absolutePath}")
            } else {
                Log.d("QRCodeScreen", "No storage permission, skipping visible location save")
            }
        } catch (e: Exception) {
            Log.w("QRCodeScreen", "Could not save to visible location: ${e.message}")
        }

        visiblePath ?: internalFile.absolutePath
    } catch (e: Exception) {
        Log.e("QRCodeScreen", "Error saving QR code", e)
        null
    }
}
