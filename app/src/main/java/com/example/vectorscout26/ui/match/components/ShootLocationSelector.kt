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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.R
import kotlin.math.*

/**
 * Represents an elliptical arc zone for shoot location selection.
 * Uses separate X and Y radii to handle elliptical shapes from the SVG.
 */
data class EllipseZone(
    val label: String,
    val centerX: Float,       // Normalized center X (0.0 to 1.0)
    val centerY: Float,       // Normalized center Y (0.0 to 1.0)
    val innerRadiusX: Float,  // Inner radius as fraction of width
    val innerRadiusY: Float,  // Inner radius as fraction of height
    val outerRadiusX: Float,  // Outer radius as fraction of width
    val outerRadiusY: Float,  // Outer radius as fraction of height
    val startAngle: Float,    // Start angle in degrees (0 = right, 90 = down)
    val sweepAngle: Float     // Sweep angle in degrees (positive = clockwise)
)

/**
 * A composable that displays the field map for selecting shooting locations.
 */
@Composable
fun ShootLocationSelector(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    robotDesignation: String,
    isBlueRight: Boolean,
    modifier: Modifier = Modifier
) {
    val isBlueRobot = robotDesignation.startsWith("Blue")

    val imageRes = when {
        isBlueRobot && isBlueRight -> R.drawable.shoot_location_blue_right
        isBlueRobot && !isBlueRight -> R.drawable.shoot_location_blue_left
        !isBlueRobot && isBlueRight -> R.drawable.shoot_location_red_left
        else -> R.drawable.shoot_location_red_right
    }

    val useRightImage = (isBlueRobot && isBlueRight) || (!isBlueRobot && !isBlueRight)

    // Zone definitions from user-provided pixel coordinates
    // PNG dimensions: 196w x 324h
    // Coordinates converted to normalized values (0.0 to 1.0)
    val zones = if (useRightImage) {
        listOf(
            // PZ - Half ellipse on LEFT side
            // Center: (55, 161) → (0.281, 0.497), radius: 22px
            EllipseZone(
                label = "PZ",
                centerX = 0.281f,
                centerY = 0.497f,
                innerRadiusX = 0f,
                innerRadiusY = 0f,
                outerRadiusX = 0.112f,  // 22/196
                outerRadiusY = 0.068f,  // 22/324
                startAngle = -90f,
                sweepAngle = 180f
            ),
            // CRZ - Inner quarter from top-right corner
            // Center: (192, 14) → (0.98, 0.043), inner radius: 0, outer: 65px
            EllipseZone(
                label = "CRZ",
                centerX = 0.98f,
                centerY = 0.043f,
                innerRadiusX = 0f,
                innerRadiusY = 0f,
                outerRadiusX = 0.332f,  // 65/196
                outerRadiusY = 0.201f,  // 65/324
                startAngle = 90f,
                sweepAngle = 90f
            ),
            // FRZ - Outer quarter from top-right corner
            // Center: (192, 14), inner: 65px, outer: 120px
            EllipseZone(
                label = "FRZ",
                centerX = 0.98f,
                centerY = 0.043f,
                innerRadiusX = 0.332f,  // 65/196
                innerRadiusY = 0.201f,  // 65/324
                outerRadiusX = 0.612f,  // 120/196
                outerRadiusY = 0.370f,  // 120/324
                startAngle = 90f,
                sweepAngle = 90f
            ),
            // CLZ - Inner quarter from bottom-right corner
            // Center: (192, 300) → (0.98, 0.926), inner: 0, outer: 65px
            EllipseZone(
                label = "CLZ",
                centerX = 0.98f,
                centerY = 0.926f,
                innerRadiusX = 0f,
                innerRadiusY = 0f,
                outerRadiusX = 0.332f,  // 65/196
                outerRadiusY = 0.201f,  // 65/324
                startAngle = 180f,
                sweepAngle = 90f
            ),
            // FLZ - Outer quarter from bottom-right corner
            // Center: (192, 300), inner: 65px, outer: 120px
            EllipseZone(
                label = "FLZ",
                centerX = 0.98f,
                centerY = 0.926f,
                innerRadiusX = 0.332f,  // 65/196
                innerRadiusY = 0.201f,  // 65/324
                outerRadiusX = 0.612f,  // 120/196
                outerRadiusY = 0.370f,  // 120/324
                startAngle = 180f,
                sweepAngle = 90f
            )
        )
    } else {
        // LEFT image - user-provided coordinates
        listOf(
            // PZ - Half ellipse on RIGHT side
            // Adjusted center: (141, 165) → (0.719, 0.509), radius: 22px
            EllipseZone(
                label = "PZ",
                centerX = 0.719f,
                centerY = 0.509f,
                innerRadiusX = 0f,
                innerRadiusY = 0f,
                outerRadiusX = 0.112f,  // 22/196
                outerRadiusY = 0.068f,  // 22/324
                startAngle = 90f,
                sweepAngle = 180f
            ),
            // CLZ - Inner quarter from top-left corner
            // Center: (5, 21) → (0.026, 0.065), inner: 0, outer: 65px
            EllipseZone(
                label = "CLZ",
                centerX = 0.026f,
                centerY = 0.065f,
                innerRadiusX = 0f,
                innerRadiusY = 0f,
                outerRadiusX = 0.332f,  // 65/196
                outerRadiusY = 0.201f,  // 65/324
                startAngle = 0f,
                sweepAngle = 90f
            ),
            // FLZ - Outer quarter from top-left corner
            // Center: (5, 21), inner: 65px, outer: 120px
            EllipseZone(
                label = "FLZ",
                centerX = 0.026f,
                centerY = 0.065f,
                innerRadiusX = 0.332f,  // 65/196
                innerRadiusY = 0.201f,  // 65/324
                outerRadiusX = 0.612f,  // 120/196
                outerRadiusY = 0.370f,  // 120/324
                startAngle = 0f,
                sweepAngle = 90f
            ),
            // CRZ - Inner quarter from bottom-left corner
            // Center: (5, 308) → (0.026, 0.951), inner: 0, outer: 65px
            EllipseZone(
                label = "CRZ",
                centerX = 0.026f,
                centerY = 0.951f,
                innerRadiusX = 0f,
                innerRadiusY = 0f,
                outerRadiusX = 0.332f,  // 65/196
                outerRadiusY = 0.201f,  // 65/324
                startAngle = 270f,
                sweepAngle = 90f
            ),
            // FRZ - Outer quarter from bottom-left corner
            // Center: (5, 308), inner: 65px, outer: 120px
            EllipseZone(
                label = "FRZ",
                centerX = 0.026f,
                centerY = 0.951f,
                innerRadiusX = 0.332f,  // 65/196
                innerRadiusY = 0.201f,  // 65/324
                outerRadiusX = 0.612f,  // 120/196
                outerRadiusY = 0.370f,  // 120/324
                startAngle = 270f,
                sweepAngle = 90f
            )
        )
    }

    // MZ boundaries - the main playing rectangle
    // Should not overlap with PZ (which extends to ~0.393 on right, ~0.607 on left)
    val mzRect = if (useRightImage) {
        Rect(
            left = 0.40f,   // Past PZ right edge
            top = 0.043f,   // Top edge aligned with corner zone centers
            right = 0.98f,  // Right edge
            bottom = 0.926f // Bottom edge aligned with corner zone centers
        )
    } else {
        Rect(
            left = 0.026f,  // Left edge aligned with corner zone centers
            top = 0.065f,   // Top edge aligned with top corner zone center
            right = 0.607f, // Touch PZ left edge (0.719 - 0.112)
            bottom = 0.951f // Bottom edge aligned with bottom corner zone center
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .pointerInput(zones, selectedLocation, useRightImage) {
                    detectTapGestures { offset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()

                        if (width > 0 && height > 0) {
                            val normX = offset.x / width
                            val normY = offset.y / height

                            val tappedZone = findTappedZone(normX, normY, zones, mzRect)

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
                contentDescription = "Shoot location zones",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                // Highlight selected zone
                if (selectedLocation.isNotEmpty()) {
                    if (selectedLocation == "MZ") {
                        val mzPath = createMzPath(mzRect, zones, useRightImage)
                        drawPath(mzPath, Color(0xFF4CAF50).copy(alpha = 0.5f))
                    } else {
                        zones.find { it.label == selectedLocation }?.let { zone ->
                            drawEllipseZone(zone, Color(0xFF4CAF50).copy(alpha = 0.5f), Color(0xFF4CAF50))
                        }
                    }
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
private fun findTappedZone(
    normX: Float,
    normY: Float,
    zones: List<EllipseZone>,
    mzRect: Rect
): String? {
    // Check ellipse zones first (they take priority over MZ)
    for (zone in zones) {
        if (isPointInEllipseZone(normX, normY, zone)) {
            return zone.label
        }
    }

    // Check if in MZ rectangle
    if (normX >= mzRect.left && normX <= mzRect.right &&
        normY >= mzRect.top && normY <= mzRect.bottom) {
        return "MZ"
    }

    return null
}

/**
 * Check if point is within an elliptical arc zone.
 */
private fun isPointInEllipseZone(
    normX: Float,
    normY: Float,
    zone: EllipseZone
): Boolean {
    // Distance from center, normalized by the outer radii (ellipse equation)
    val dx = normX - zone.centerX
    val dy = normY - zone.centerY

    // Check outer boundary (point must be inside outer ellipse)
    if (zone.outerRadiusX > 0 && zone.outerRadiusY > 0) {
        val outerDist = (dx * dx) / (zone.outerRadiusX * zone.outerRadiusX) +
                        (dy * dy) / (zone.outerRadiusY * zone.outerRadiusY)
        if (outerDist > 1) return false
    }

    // Check inner boundary (point must be outside inner ellipse)
    if (zone.innerRadiusX > 0 && zone.innerRadiusY > 0) {
        val innerDist = (dx * dx) / (zone.innerRadiusX * zone.innerRadiusX) +
                        (dy * dy) / (zone.innerRadiusY * zone.innerRadiusY)
        if (innerDist < 1) return false
    }

    // Check angle - calculate angle from center to point
    var angle = atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI
    if (angle < 0) angle += 360.0

    // Normalize start angle to 0-360
    var startAngle = zone.startAngle.toDouble()
    if (startAngle < 0) startAngle += 360.0

    val endAngle = startAngle + zone.sweepAngle

    // Check if angle is within sweep
    return if (endAngle > 360) {
        angle >= startAngle || angle <= (endAngle - 360)
    } else {
        angle >= startAngle && angle <= endAngle
    }
}

/**
 * Draw an elliptical arc zone.
 */
private fun DrawScope.drawEllipseZone(
    zone: EllipseZone,
    fillColor: Color,
    strokeColor: Color
) {
    val centerX = zone.centerX * size.width
    val centerY = zone.centerY * size.height
    val innerRx = zone.innerRadiusX * size.width
    val innerRy = zone.innerRadiusY * size.height
    val outerRx = zone.outerRadiusX * size.width
    val outerRy = zone.outerRadiusY * size.height

    val path = Path().apply {
        // Start at outer arc start
        val startRad = Math.toRadians(zone.startAngle.toDouble())
        val outerStartX = centerX + outerRx * cos(startRad).toFloat()
        val outerStartY = centerY + outerRy * sin(startRad).toFloat()
        moveTo(outerStartX, outerStartY)

        // Draw outer arc
        arcTo(
            rect = Rect(
                centerX - outerRx,
                centerY - outerRy,
                centerX + outerRx,
                centerY + outerRy
            ),
            startAngleDegrees = zone.startAngle,
            sweepAngleDegrees = zone.sweepAngle,
            forceMoveTo = false
        )

        // Line to inner arc end (or center if no inner radius)
        val endAngle = zone.startAngle + zone.sweepAngle
        val endRad = Math.toRadians(endAngle.toDouble())

        if (innerRx > 0 && innerRy > 0) {
            val innerEndX = centerX + innerRx * cos(endRad).toFloat()
            val innerEndY = centerY + innerRy * sin(endRad).toFloat()
            lineTo(innerEndX, innerEndY)

            // Draw inner arc backwards
            arcTo(
                rect = Rect(
                    centerX - innerRx,
                    centerY - innerRy,
                    centerX + innerRx,
                    centerY + innerRy
                ),
                startAngleDegrees = endAngle,
                sweepAngleDegrees = -zone.sweepAngle,
                forceMoveTo = false
            )
        } else {
            // No inner radius - line to center
            lineTo(centerX, centerY)
        }

        close()
    }

    drawPath(path, fillColor)
    drawPath(path, strokeColor, style = Stroke(width = 2f))
}

/**
 * Create a path for MZ that excludes the quarter circle zones.
 * MZ is a rectangle with quarter-circle cutouts at two corners.
 */
private fun DrawScope.createMzPath(
    mzRect: Rect,
    zones: List<EllipseZone>,
    useRightImage: Boolean
): Path {
    val left = mzRect.left * size.width
    val top = mzRect.top * size.height
    val right = mzRect.right * size.width
    val bottom = mzRect.bottom * size.height

    // Find the outer radius of the corner zones (FRZ and FLZ have the outer radius)
    val topCornerZone = zones.find { if (useRightImage) it.label == "FRZ" else it.label == "FLZ" }
    val bottomCornerZone = zones.find { if (useRightImage) it.label == "FLZ" else it.label == "FRZ" }

    val topOuterRx = (topCornerZone?.outerRadiusX ?: 0f) * size.width
    val topOuterRy = (topCornerZone?.outerRadiusY ?: 0f) * size.height
    val bottomOuterRx = (bottomCornerZone?.outerRadiusX ?: 0f) * size.width
    val bottomOuterRy = (bottomCornerZone?.outerRadiusY ?: 0f) * size.height

    val topCornerCenterX = (topCornerZone?.centerX ?: 0f) * size.width
    val topCornerCenterY = (topCornerZone?.centerY ?: 0f) * size.height
    val bottomCornerCenterX = (bottomCornerZone?.centerX ?: 0f) * size.width
    val bottomCornerCenterY = (bottomCornerZone?.centerY ?: 0f) * size.height

    return Path().apply {
        if (useRightImage) {
            // Right image: cutouts at top-right and bottom-right corners
            // Start at top-left
            moveTo(left, top)
            // Line to where top-right arc begins
            lineTo(topCornerCenterX - topOuterRx, top)
            // Arc around the top-right corner (quarter circle going down)
            arcTo(
                rect = Rect(
                    topCornerCenterX - topOuterRx,
                    topCornerCenterY - topOuterRy,
                    topCornerCenterX + topOuterRx,
                    topCornerCenterY + topOuterRy
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            // Line down to where bottom-right arc begins
            lineTo(right, bottomCornerCenterY - bottomOuterRy)
            // Arc around the bottom-right corner (quarter circle going left)
            arcTo(
                rect = Rect(
                    bottomCornerCenterX - bottomOuterRx,
                    bottomCornerCenterY - bottomOuterRy,
                    bottomCornerCenterX + bottomOuterRx,
                    bottomCornerCenterY + bottomOuterRy
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            // Line to bottom-left
            lineTo(left, bottom)
            // Close back to top-left
            close()
        } else {
            // Left image: cutouts at top-left and bottom-left corners
            // In screen coordinates: 0°=right, 90°=down, 180°=left, 270°=up

            // Calculate intersection angles with MZ right edge
            // cos(angle) = (right - centerX) / radiusX
            val bottomCosAngle = ((right - bottomCornerCenterX) / bottomOuterRx).coerceIn(-1f, 1f)
            val bottomIntersectAngle = Math.toDegrees(acos(bottomCosAngle.toDouble())).toFloat()
            // Use negative angle since we want the upper intersection (y < centerY)
            val bottomStartAngle = -bottomIntersectAngle  // e.g., -18° means 342°

            val topCosAngle = ((right - topCornerCenterX) / topOuterRx).coerceIn(-1f, 1f)
            val topIntersectAngle = Math.toDegrees(acos(topCosAngle.toDouble())).toFloat()
            // Use positive angle since we want the lower intersection (y > centerY)
            val topEndAngle = topIntersectAngle  // e.g., 18°

            // Start at top-right
            moveTo(right, top)
            // Line down to bottom-right
            lineTo(right, bottom)

            // Line to where bottom arc intersects MZ (approximately)
            val bottomArcStartY = bottomCornerCenterY + bottomOuterRy * sin(Math.toRadians(bottomStartAngle.toDouble())).toFloat()
            lineTo(right, bottomArcStartY.coerceIn(top, bottom))

            // Bottom-left corner arc: from intersection with MZ right edge to 270° (left edge)
            // bottomStartAngle is negative (e.g., -18° = 342°), need counterclockwise to 270°
            // Normalize: -18° → 342°, then counterclockwise to 270° = -(342-270) = -72°
            val normalizedBottomStart = if (bottomStartAngle < 0) bottomStartAngle + 360f else bottomStartAngle
            val bottomSweep = -(normalizedBottomStart - 270f)  // Counterclockwise
            arcTo(
                rect = Rect(
                    bottomCornerCenterX - bottomOuterRx,
                    bottomCornerCenterY - bottomOuterRy,
                    bottomCornerCenterX + bottomOuterRx,
                    bottomCornerCenterY + bottomOuterRy
                ),
                startAngleDegrees = bottomStartAngle,
                sweepAngleDegrees = bottomSweep,
                forceMoveTo = false
            )

            // Line up the left edge to where top-left arc starts (at 90°)
            lineTo(left, topCornerCenterY + topOuterRy)

            // Top-left corner arc: from 90° (left edge) to intersection with MZ right edge (~18°)
            // Counterclockwise from 90° to 18° = -72°
            arcTo(
                rect = Rect(
                    topCornerCenterX - topOuterRx,
                    topCornerCenterY - topOuterRy,
                    topCornerCenterX + topOuterRx,
                    topCornerCenterY + topOuterRy
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -(90f - topEndAngle),  // Negative for counterclockwise
                forceMoveTo = false
            )

            // Line back to top-right to close
            lineTo(right, top)
            // Close back to top-right
            close()
        }
    }
}
