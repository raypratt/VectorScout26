package com.example.vectorscout26.ui.foul

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
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
import com.example.vectorscout26.utils.QRCodeGenerator
import com.example.vectorscout26.utils.saveQRCodeImageToDownloads
import kotlinx.coroutines.launch

@Composable
fun FoulQRCodeScreen(
    viewModel: FoulScoutingViewModel,
    onNewMatch: () -> Unit,
    onHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val jsonContent = state.qrJson
    val matchLabel = "${state.event}_M${state.matchNumber}_Fouls"

    LaunchedEffect(jsonContent) {
        if (jsonContent.isNotEmpty()) {
            qrBitmap = QRCodeGenerator.generateQRCode(jsonContent, 800, 800)
            isLoading = false
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
                text = "Foul Scout Complete",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (state.event.isNotEmpty()) "${state.event} — Match ${state.matchNumber}"
                else "Match ${state.matchNumber}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Text("Generating QR Code...", modifier = Modifier.padding(top = 16.dp))
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
                            contentDescription = "Foul QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan this QR code to upload foul data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "JSON: ${jsonContent.length} chars",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Foul JSON", jsonContent))
                        Toast.makeText(context, "JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Copy JSON to Clipboard")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        val filename = "$matchLabel.png"
                        val saved = saveQRCodeImageToDownloads(context, jsonContent, matchLabel, filename)
                        Toast.makeText(
                            context,
                            if (saved != null) "Saved: $filename" else "Failed to save QR code",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.resetForNewMatch()
                        onNewMatch()
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
