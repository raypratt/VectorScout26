package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.graphics.Paint
import com.example.vectorscout26.R

/**
 * Represents a rectangular zone for ferry location selection.
 */
data class FerryZone(
    val label: String,
    val left: Float,    // Normalized left edge (0.0 to 1.0)
    val top: Float,     // Normalized top edge (0.0 to 1.0)
    val right: Float,   // Normalized right edge (0.0 to 1.0)
    val bottom: Float   // Normalized bottom edge (0.0 to 1.0)
)

/**
 * A composable that displays the field map for selecting ferry delivery locations.
 */
@Composable
fun FerryLocationSelector(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    robotDesignation: String,
    isBlueRight: Boolean,
    modifier: Modifier = Modifier
) {
    val isBlueRobot = robotDesignation.startsWith("Blue")

    // Image selection based on field orientation (matches zone coordinates)
    val imageRes = if (isBlueRight) {
        R.drawable.field_blue_right
    } else {
        R.drawable.field_red_right
    }

    // Determine if robot being scouted is on the right side of the field
    val useRightCoordinates = (isBlueRobot && isBlueRight) || (!isBlueRobot && !isBlueRight)

    // Zone definitions - image is 557w x 280h
    // Same as Load zones but without Depot and Opponent
    // Zones are ordered with Outpost first so it takes priority over Alliance
    val zones = if (useRightCoordinates) {
        // Right side coordinates - robot is on the right side of the field
        listOf(
            // Outpost: (506, 24) to (554, 52)
            FerryZone("Outpost", 0.908f, 0.086f, 0.995f, 0.186f),
            // Alliance: (409, 17) to (530, 263) - irregular, Outpost overlaps
            FerryZone("Alliance", 0.734f, 0.061f, 0.952f, 0.939f),
            // Neutral: (173, 17) to (387, 263)
            FerryZone("Neutral", 0.311f, 0.061f, 0.695f, 0.939f)
        )
    } else {
        // Left side coordinates - robot is on the left side of the field
        listOf(
            // Outpost: (4, 230) to (55, 263)
            FerryZone("Outpost", 0.007f, 0.821f, 0.099f, 0.939f),
            // Alliance: (27, 17) to (150, 263) - irregular, Outpost overlaps
            FerryZone("Alliance", 0.048f, 0.061f, 0.269f, 0.939f),
            // Neutral: (173, 17) to (387, 263)
            FerryZone("Neutral", 0.311f, 0.061f, 0.695f, 0.939f)
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(zones, selectedLocation, useRightCoordinates) {
                    detectTapGestures { offset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()

                        if (width > 0 && height > 0) {
                            val normX = offset.x / width
                            val normY = offset.y / height

                            val tappedZone = findTappedFerryZone(normX, normY, zones)

                            if (tappedZone != null) {
                                if (selectedLocation == tappedZone) {
                                    onLocationSelected("")
                                } else {
                                    onLocationSelected(tappedZone)
                                }
                            }
                        }
                    }
                }
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Ferry delivery zones",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                // Highlight selected zone
                if (selectedLocation.isNotEmpty()) {
                    zones.find { it.label == selectedLocation }?.let { zone ->
                        drawFerryZone(zone, Color(0xFF4CAF50).copy(alpha = 0.5f), Color(0xFF4CAF50))
                    }
                }

                // Draw zone labels
                val textPaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = size.height * 0.08f  // Scale text with image
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                    setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
                }

                zones.forEach { zone ->
                    val centerX = ((zone.left + zone.right) / 2) * size.width
                    val centerY = ((zone.top + zone.bottom) / 2) * size.height + (textPaint.textSize / 3)

                    drawContext.canvas.nativeCanvas.drawText(
                        zone.label,
                        centerX,
                        centerY,
                        textPaint
                    )
                }
            }
        }

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

/**
 * Find which zone contains the tap point.
 */
private fun findTappedFerryZone(
    normX: Float,
    normY: Float,
    zones: List<FerryZone>
): String? {
    for (zone in zones) {
        if (normX >= zone.left && normX <= zone.right &&
            normY >= zone.top && normY <= zone.bottom) {
            return zone.label
        }
    }
    return null
}

/**
 * Draw a rectangular ferry zone.
 */
private fun DrawScope.drawFerryZone(
    zone: FerryZone,
    fillColor: Color,
    strokeColor: Color
) {
    val left = zone.left * size.width
    val top = zone.top * size.height
    val width = (zone.right - zone.left) * size.width
    val height = (zone.bottom - zone.top) * size.height

    drawRect(
        color = fillColor,
        topLeft = Offset(left, top),
        size = Size(width, height)
    )
    drawRect(
        color = strokeColor,
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(width = 2f)
    )
}
