package com.example.vectorscout26.ui.pit

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.vectorscout26.data.model.Event
import com.example.vectorscout26.data.repository.EventRepository
import com.example.vectorscout26.ui.match.components.EventSelector
import com.example.vectorscout26.ui.pit.components.AutoPathBuilder
import com.example.vectorscout26.ui.pit.components.DrawableFieldMap
import com.example.vectorscout26.utils.Constants
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitScoutingScreen(
    viewModel: PitScoutingViewModel,
    onSubmitSuccess: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Photo capture
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            viewModel.updatePhotoPath(photoUri.toString())
            Toast.makeText(context, "Photo captured", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = createPhotoFile(context, state.teamNumber)
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Load events from repository
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    LaunchedEffect(Unit) {
        events = EventRepository.loadEvents(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pit Scouting") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error display
            if (state.errors.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        state.errors.forEach { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Event Selector
            EventSelector(
                events = events,
                selectedEventCode = state.eventCode,
                onEventSelected = { eventCode ->
                    val selectedEvent = events.find { it.eventCode == eventCode }
                    if (selectedEvent != null) {
                        viewModel.updateEvent(selectedEvent.eventName, eventCode)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Team Number
            OutlinedTextField(
                value = state.teamNumber,
                onValueChange = { viewModel.updateTeamNumber(it) },
                label = { Text("Team Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Drivetrain Type
            Text("Drivetrain Type", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Constants.DRIVETRAIN_TYPES.forEach { type ->
                    FilterChip(
                        selected = state.drivetrainType == type,
                        onClick = { viewModel.updateDrivetrainType(type) },
                        label = { Text(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Preferred Role
            Text("Preferred Role", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Constants.PREFERRED_ROLES.forEach { role ->
                    FilterChip(
                        selected = state.preferredRole == role,
                        onClick = { viewModel.updatePreferredRole(role) },
                        label = { Text(role) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Preferred Path
            Text("Preferred Path", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Constants.PREFERRED_PATHS.forEach { path ->
                    FilterChip(
                        selected = state.preferredPath == path,
                        onClick = { viewModel.updatePreferredPath(path) },
                        label = { Text(path) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Robot Photo
            Text("Robot Photo", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (state.teamNumber.isBlank()) {
                            Toast.makeText(context, "Enter team number first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val photoFile = createPhotoFile(context, state.teamNumber)
                            photoUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            cameraLauncher.launch(photoUri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Take Photo")
                }

                if (state.photoPath != null) {
                    Text(
                        text = "Photo saved",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider()

            // Auto Paths Section
            Text("Auto Paths", style = MaterialTheme.typography.titleLarge)

            // Path tabs
            ScrollableTabRow(
                selectedTabIndex = state.currentPathIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                state.autoPaths.forEachIndexed { index, path ->
                    Tab(
                        selected = state.currentPathIndex == index,
                        onClick = { viewModel.selectAutoPath(index) },
                        text = { Text(path.name) }
                    )
                }

                // Add path tab
                Tab(
                    selected = false,
                    onClick = { viewModel.addAutoPath() },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add path"
                        )
                    }
                )
            }

            // Current path content
            val currentPath = state.autoPaths.getOrNull(state.currentPathIndex)
            if (currentPath != null) {
                // Delete path button (if more than one)
                if (state.autoPaths.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.removeCurrentAutoPath() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete path",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete ${currentPath.name}")
                        }
                    }
                }

                // Auto path builder
                AutoPathBuilder(
                    steps = currentPath.steps,
                    currentCategory = currentPath.currentCategory,
                    locationOptions = viewModel.getLocationOptionsForCurrentPath(),
                    onAddStep = { step -> viewModel.addStep(step) },
                    onDeleteStepAndAfter = { index -> viewModel.deleteStepAndAfter(index) },
                    onClear = { viewModel.clearCurrentPath() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Drawable field map
                DrawableFieldMap(
                    teamNumber = state.teamNumber,
                    pathName = currentPath.name,
                    strokes = currentPath.drawingStrokes,
                    onStrokesChanged = { newStrokes ->
                        viewModel.updateDrawingStrokes(newStrokes)
                    },
                    onUndo = { viewModel.undoLastStroke() },
                    onClear = { viewModel.clearDrawingStrokes() },
                    onDrawingSaved = { path ->
                        viewModel.updateDrawingPath(path)
                        Toast.makeText(context, "Drawing saved: $path", Toast.LENGTH_SHORT).show()
                    }
                )

                if (currentPath.drawingPath != null) {
                    Text(
                        text = "Drawing saved: ${currentPath.drawingPath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    viewModel.submitPitScout { id ->
                        onSubmitSuccess(id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isSubmitting
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Generate QR Code", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun createPhotoFile(context: android.content.Context, teamNumber: String): File {
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File(storageDir, "${teamNumber}_robot.jpg")
}
