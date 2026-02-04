package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.R

/**
 * Represents a clickable zone on the start location field map.
 * Coordinates are normalized (0.0 to 1.0) relative to image dimensions.
 */
data class StartZone(
    val label: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * A composable that displays the field map for selecting robot starting locations.
 * Supports toggling between Blue Right and Red Right field orientations.
 * Image selection is based on both robot alliance color and field orientation.
 *
 * @param selectedLocation The currently selected location label (L1, L2, etc.) or empty string
 * @param onLocationSelected Callback when a location is selected or deselected
 * @param robotDesignation The robot being scouted (e.g., "Blue1", "Red2") - determines image color
 * @param isBlueRight Whether the field is oriented with Blue on the right
 * @param onToggleOrientation Callback when the orientation toggle is clicked
 * @param modifier Optional modifier for the component
 */
@Composable
fun StartLocationSelector(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    robotDesignation: String,
    isBlueRight: Boolean,
    onToggleOrientation: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if scouting a blue or red robot
    val isBlueRobot = robotDesignation.startsWith("Blue")
    // Track which specific zone is selected (by index) for single-box highlighting
    var selectedZoneIndex by remember { mutableStateOf(-1) }

    // Define clickable zones based on field orientation
    // Coordinates are normalized (0.0 to 1.0) based on image dimensions (291 x 323)

    // Right map zones (used when isBlueRight = true, shows *_right_b images)
    val rightMapZones = listOf(
        StartZone("L1", 0.412f, 0.811f, 0.515f, 0.941f),
        StartZone("L2", 0.412f, 0.594f, 0.515f, 0.811f),
        StartZone("L3a", 0.412f, 0.508f, 0.515f, 0.594f),
        StartZone("L3b", 0.412f, 0.427f, 0.515f, 0.511f),
        StartZone("L3", 0.515f, 0.427f, 0.605f, 0.594f),
        StartZone("L4", 0.412f, 0.204f, 0.515f, 0.427f),
        StartZone("L5", 0.412f, 0.068f, 0.515f, 0.204f)
    )

    // Left map zones (used when isBlueRight = false, shows *_left_b images)
    val leftMapZones = listOf(
        StartZone("L1", 0.478f, 0.068f, 0.584f, 0.198f),
        StartZone("L2", 0.478f, 0.201f, 0.584f, 0.421f),
        StartZone("L3a", 0.481f, 0.424f, 0.584f, 0.508f),
        StartZone("L3b", 0.481f, 0.508f, 0.584f, 0.591f),
        StartZone("L3", 0.392f, 0.424f, 0.481f, 0.594f),
        StartZone("L4", 0.481f, 0.588f, 0.584f, 0.808f),
        StartZone("L5", 0.481f, 0.808f, 0.584f, 0.938f)
    )

    // Select zones based on whether the scouted robot's side is on the right
    // Blue robot on right when isBlueRight, Red robot on right when !isBlueRight
    val robotOnRight = (isBlueRobot && isBlueRight) || (!isBlueRobot && !isBlueRight)
    val activeZones = if (robotOnRight) rightMapZones else leftMapZones

    // Sync selectedZoneIndex when selectedLocation changes externally or on recomposition
    LaunchedEffect(selectedLocation, activeZones) {
        selectedZoneIndex = if (selectedLocation.isEmpty()) {
            -1
        } else {
            activeZones.indexOfFirst { it.label == selectedLocation }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toggle button for field orientation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    isBlueRobot && isBlueRight -> "Blue Right"
                    isBlueRobot && !isBlueRight -> "Blue Left"
                    !isBlueRobot && !isBlueRight -> "Red Right"  // Red is on right when !isBlueRight
                    else -> "Red Left"  // Red is on left when isBlueRight
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onToggleOrientation
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Toggle field orientation"
                )
            }
        }

        // Field image with clickable overlay - 66% width, centered
        Box(
            modifier = Modifier
                .fillMaxWidth(0.66f)
                .pointerInput(robotOnRight, selectedLocation) {
                    detectTapGestures { offset ->
                        // Use size from PointerInputScope directly
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()

                        if (width > 0 && height > 0) {
                            // Convert tap coordinates to normalized values
                            val normalizedX = offset.x / width
                            val normalizedY = offset.y / height

                            // Find which zone was tapped
                            val tappedZone = activeZones.find { zone ->
                                normalizedX >= zone.left && normalizedX <= zone.right &&
                                normalizedY >= zone.top && normalizedY <= zone.bottom
                            }

                            tappedZone?.let { zone ->
                                val zoneIndex = activeZones.indexOf(zone)
                                // Toggle selection
                                if (selectedZoneIndex == zoneIndex) {
                                    selectedZoneIndex = -1
                                    onLocationSelected("")
                                } else {
                                    selectedZoneIndex = zoneIndex
                                    onLocationSelected(zone.label)
                                }
                            }
                        }
                    }
                }
        ) {
            // Field image - select based on robot color AND field orientation
            // For red robots: "Red Right" means red is on right (!isBlueRight), use red_right_b
            Image(
                painter = painterResource(
                    id = when {
                        isBlueRobot && isBlueRight -> R.drawable.start_locations_blue_right_b
                        isBlueRobot && !isBlueRight -> R.drawable.start_locations_blue_left_b
                        !isBlueRobot && !isBlueRight -> R.drawable.start_locations_red_right_b  // Red Right
                        else -> R.drawable.start_locations_red_left_b  // Red Left
                    }
                ),
                contentDescription = "Field start locations",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            // Highlight overlay for selected zone
            Canvas(modifier = Modifier.matchParentSize()) {
                if (selectedZoneIndex >= 0 && selectedZoneIndex < activeZones.size) {
                    val zone = activeZones[selectedZoneIndex]
                    val left = zone.left * size.width
                    val top = zone.top * size.height
                    val zoneWidth = (zone.right - zone.left) * size.width
                    val zoneHeight = (zone.bottom - zone.top) * size.height

                    drawRect(
                        color = Color(0xFF4CAF50).copy(alpha = 0.5f),
                        topLeft = Offset(left, top),
                        size = Size(zoneWidth, zoneHeight)
                    )
                    drawRect(
                        color = Color(0xFF4CAF50),
                        topLeft = Offset(left, top),
                        size = Size(zoneWidth, zoneHeight),
                        style = Stroke(width = 4f)
                    )
                }
            }
        }

        // Show selected location
        if (selectedLocation.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selected: $selectedLocation",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
