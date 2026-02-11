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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.util.Log
import com.example.vectorscout26.R
import kotlin.math.*

/**
 * Zone types for shoot location selection.
 */
sealed class ShootZone(val label: String)

/**
 * Half circle zone (H zone).
 */
data class HalfCircleZone(
    val zoneLabel: String,
    val centerX: Float,
    val centerY: Float,
    val radiusX: Float,
    val radiusY: Float,
    val startAngle: Float,
    val sweepAngle: Float
) : ShootZone(zoneLabel)

/**
 * Annular sector zone (R1, L1) - ring segment between inner and outer radius.
 */
data class AnnularZone(
    val zoneLabel: String,
    val centerX: Float,
    val centerY: Float,
    val innerRadiusX: Float,
    val innerRadiusY: Float,
    val outerRadiusX: Float,
    val outerRadiusY: Float,
    val startAngle: Float,
    val sweepAngle: Float
) : ShootZone(zoneLabel)

/**
 * Rectangularized zone (R2, L2) - has inner arc and rectangular outer boundary.
 * Corners are in normalized coordinates (0.0 to 1.0).
 */
data class RectArcZone(
    val zoneLabel: String,
    val centerX: Float,
    val centerY: Float,
    val innerRadiusX: Float,
    val innerRadiusY: Float,
    val startAngle: Float,
    val sweepAngle: Float,
    val topLeft: Offset,
    val topRight: Offset,
    val bottomRight: Offset,
    val bottomLeft: Offset
) : ShootZone(zoneLabel)

/**
 * A composable that displays the field map for selecting shooting locations.
 * Zones: H (half circle), R1/L1 (annular sectors), R2/L2 (rectangularized)
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

    // New image resources with _b suffix
    // isBlueRight=true means red on left, blue on right
    val imageRes = when {
        isBlueRobot && isBlueRight -> R.drawable.shoot_locations_blue_right_b
        isBlueRobot && !isBlueRight -> R.drawable.shoot_locations_blue_left_b
        !isBlueRobot && isBlueRight -> R.drawable.shoot_locations_red_left_b
        else -> R.drawable.shoot_locations_red_right_b
    }

    val imageName = when {
        isBlueRobot && isBlueRight -> "blue_right_b"
        isBlueRobot && !isBlueRight -> "blue_left_b"
        !isBlueRobot && isBlueRight -> "red_left_b"
        else -> "red_right_b"
    }
    Log.d("ShootLocation", "robotDesignation=$robotDesignation, isBlueRobot=$isBlueRobot, isBlueRight=$isBlueRight, image=$imageName")

    val useRightImage = (isBlueRobot && isBlueRight) || (!isBlueRobot && !isBlueRight)

    // Image dimensions: 191w x 280h
    // Zone definitions with normalized coordinates

    val zones: List<ShootZone> = if (useRightImage) {
        // RIGHT image variant - H on left side, zones extend to right
        // Center adjusted: (26, 143) -> (36, 138) [right 10, up 5]
        listOf(
            // H - Half circle on left side, facing right
            HalfCircleZone(
                zoneLabel = "H",
                centerX = 36f / 191f,
                centerY = 138f / 280f,
                radiusX = 51f / 191f,
                radiusY = 51f / 280f,
                startAngle = -90f,
                sweepAngle = 180f
            ),
            // R1 - Upper annular sector
            AnnularZone(
                zoneLabel = "R1",
                centerX = 36f / 191f,
                centerY = 138f / 280f,
                innerRadiusX = 51f / 191f,
                innerRadiusY = 51f / 280f,
                outerRadiusX = 107f / 191f,
                outerRadiusY = 107f / 280f,
                startAngle = -90f,
                sweepAngle = 90f
            ),
            // L1 - Lower annular sector
            AnnularZone(
                zoneLabel = "L1",
                centerX = 36f / 191f,
                centerY = 138f / 280f,
                innerRadiusX = 51f / 191f,
                innerRadiusY = 51f / 280f,
                outerRadiusX = 107f / 191f,
                outerRadiusY = 107f / 280f,
                startAngle = 0f,
                sweepAngle = 90f
            ),
            // R2 - Upper rectangularized zone
            RectArcZone(
                zoneLabel = "R2",
                centerX = 36f / 191f,
                centerY = 138f / 280f,
                innerRadiusX = 107f / 191f,
                innerRadiusY = 107f / 280f,
                startAngle = -90f,
                sweepAngle = 90f,
                topLeft = Offset(36f / 191f, 19f / 280f),
                topRight = Offset(166f / 191f, 19f / 280f),
                bottomRight = Offset(166f / 191f, 141f / 280f),
                bottomLeft = Offset(134f / 191f, 142f / 280f)
            ),
            // L2 - Lower rectangularized zone
            RectArcZone(
                zoneLabel = "L2",
                centerX = 36f / 191f,
                centerY = 138f / 280f,
                innerRadiusX = 107f / 191f,
                innerRadiusY = 107f / 280f,
                startAngle = 0f,
                sweepAngle = 90f,
                topLeft = Offset(134f / 191f, 142f / 280f),
                topRight = Offset(166f / 191f, 141f / 280f),
                bottomRight = Offset(166f / 191f, 263f / 280f),
                bottomLeft = Offset(36f / 191f, 264f / 280f)
            )
        )
    } else {
        // LEFT image variant - 180° rotation of right image
        // Center: (36, 138) rotated → (155, 142)
        listOf(
            // H - Half circle on right side, facing left
            HalfCircleZone(
                zoneLabel = "H",
                centerX = (191f - 36f) / 191f,
                centerY = (280f - 138f) / 280f,
                radiusX = 51f / 191f,
                radiusY = 51f / 280f,
                startAngle = 90f,
                sweepAngle = 180f
            ),
            // R1 - Annular sector (rotated 180°)
            AnnularZone(
                zoneLabel = "R1",
                centerX = (191f - 36f) / 191f,
                centerY = (280f - 138f) / 280f,
                innerRadiusX = 51f / 191f,
                innerRadiusY = 51f / 280f,
                outerRadiusX = 107f / 191f,
                outerRadiusY = 107f / 280f,
                startAngle = 90f,
                sweepAngle = 90f
            ),
            // L1 - Annular sector (rotated 180°)
            AnnularZone(
                zoneLabel = "L1",
                centerX = (191f - 36f) / 191f,
                centerY = (280f - 138f) / 280f,
                innerRadiusX = 51f / 191f,
                innerRadiusY = 51f / 280f,
                outerRadiusX = 107f / 191f,
                outerRadiusY = 107f / 280f,
                startAngle = 180f,
                sweepAngle = 90f
            ),
            // R2 - Reflected L2 over y=140
            RectArcZone(
                zoneLabel = "R2",
                centerX = 151f / 191f,
                centerY = 143f / 280f,
                innerRadiusX = 107f / 191f,
                innerRadiusY = 107f / 280f,
                startAngle = 180f,
                sweepAngle = -90f,
                topLeft = Offset(44f / 191f, 143f / 280f),
                topRight = Offset(9f / 191f, 143f / 280f),
                bottomRight = Offset(9f / 191f, 265f / 280f),
                bottomLeft = Offset(150f / 191f, 265f / 280f)
            ),
            // L2 - Rectangularized zone (rotated 180°)
            // Path: TL(arc end) → TR → BR → BL → arc → close to TL
            // Reordered so arc connects properly
            RectArcZone(
                zoneLabel = "L2",
                centerX = 151f / 191f,
                centerY = 137f / 280f,
                innerRadiusX = 107f / 191f,
                innerRadiusY = 107f / 280f,
                startAngle = 180f,
                sweepAngle = 90f,
                topLeft = Offset(44f / 191f, 137f / 280f),      // arc end at 180°
                topRight = Offset(9f / 191f, 137f / 280f),      // bottom left corner
                bottomRight = Offset(9f / 191f, 15f / 280f),    // top left corner
                bottomLeft = Offset(150f / 191f, 15f / 280f)    // top right, near arc start
            )
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

                            val tappedZone = findTappedZone(normX, normY, zones)

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
                    val zone = zones.find { it.label == selectedLocation }
                    if (zone != null) {
                        drawZone(zone, Color(0xFF4CAF50).copy(alpha = 0.5f), Color(0xFF4CAF50))
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
    zones: List<ShootZone>
): String? {
    // Check zones in order (H first, then R1/L1, then R2/L2)
    for (zone in zones) {
        val isInZone = when (zone) {
            is HalfCircleZone -> isPointInHalfCircle(normX, normY, zone)
            is AnnularZone -> isPointInAnnularZone(normX, normY, zone)
            is RectArcZone -> isPointInRectArcZone(normX, normY, zone)
        }
        if (isInZone) return zone.label
    }
    return null
}

/**
 * Check if point is within a half circle zone.
 */
private fun isPointInHalfCircle(
    normX: Float,
    normY: Float,
    zone: HalfCircleZone
): Boolean {
    val dx = normX - zone.centerX
    val dy = normY - zone.centerY

    // Check if within ellipse
    val dist = (dx * dx) / (zone.radiusX * zone.radiusX) +
               (dy * dy) / (zone.radiusY * zone.radiusY)
    if (dist > 1) return false

    // Check angle
    return isAngleInSweep(dx, dy, zone.startAngle, zone.sweepAngle)
}

/**
 * Check if point is within an annular zone.
 */
private fun isPointInAnnularZone(
    normX: Float,
    normY: Float,
    zone: AnnularZone
): Boolean {
    val dx = normX - zone.centerX
    val dy = normY - zone.centerY

    // Check outer boundary
    val outerDist = (dx * dx) / (zone.outerRadiusX * zone.outerRadiusX) +
                    (dy * dy) / (zone.outerRadiusY * zone.outerRadiusY)
    if (outerDist > 1) return false

    // Check inner boundary
    val innerDist = (dx * dx) / (zone.innerRadiusX * zone.innerRadiusX) +
                    (dy * dy) / (zone.innerRadiusY * zone.innerRadiusY)
    if (innerDist < 1) return false

    // Check angle
    return isAngleInSweep(dx, dy, zone.startAngle, zone.sweepAngle)
}

/**
 * Check if point is within a rectangularized arc zone.
 */
private fun isPointInRectArcZone(
    normX: Float,
    normY: Float,
    zone: RectArcZone
): Boolean {
    // First check if point is inside the rectangular boundary (polygon)
    val corners = listOf(zone.topLeft, zone.topRight, zone.bottomRight, zone.bottomLeft)
    if (!isPointInPolygon(normX, normY, corners)) return false

    // Then check if point is outside the inner arc
    val dx = normX - zone.centerX
    val dy = normY - zone.centerY
    val innerDist = (dx * dx) / (zone.innerRadiusX * zone.innerRadiusX) +
                    (dy * dy) / (zone.innerRadiusY * zone.innerRadiusY)

    return innerDist >= 1
}

/**
 * Check if angle from (dx, dy) is within the sweep range.
 */
private fun isAngleInSweep(
    dx: Float,
    dy: Float,
    startAngle: Float,
    sweepAngle: Float
): Boolean {
    var angle = atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI
    if (angle < 0) angle += 360.0

    var start = startAngle.toDouble()
    if (start < 0) start += 360.0

    val end = start + sweepAngle

    return if (end > 360) {
        angle >= start || angle <= (end - 360)
    } else {
        angle >= start && angle <= end
    }
}

/**
 * Check if a point is inside a polygon using ray casting.
 */
private fun isPointInPolygon(
    x: Float,
    y: Float,
    corners: List<Offset>
): Boolean {
    var inside = false
    var j = corners.size - 1

    for (i in corners.indices) {
        val xi = corners[i].x
        val yi = corners[i].y
        val xj = corners[j].x
        val yj = corners[j].y

        if (((yi > y) != (yj > y)) &&
            (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
            inside = !inside
        }
        j = i
    }

    return inside
}

/**
 * Draw a zone with fill and stroke.
 */
private fun DrawScope.drawZone(
    zone: ShootZone,
    fillColor: Color,
    strokeColor: Color
) {
    when (zone) {
        is HalfCircleZone -> drawHalfCircleZone(zone, fillColor, strokeColor)
        is AnnularZone -> drawAnnularZone(zone, fillColor, strokeColor)
        is RectArcZone -> drawRectArcZone(zone, fillColor, strokeColor)
    }
}

/**
 * Draw a half circle zone.
 */
private fun DrawScope.drawHalfCircleZone(
    zone: HalfCircleZone,
    fillColor: Color,
    strokeColor: Color
) {
    val centerX = zone.centerX * size.width
    val centerY = zone.centerY * size.height
    val rx = zone.radiusX * size.width
    val ry = zone.radiusY * size.height

    val path = Path().apply {
        moveTo(centerX, centerY)
        arcTo(
            rect = Rect(centerX - rx, centerY - ry, centerX + rx, centerY + ry),
            startAngleDegrees = zone.startAngle,
            sweepAngleDegrees = zone.sweepAngle,
            forceMoveTo = false
        )
        close()
    }

    drawPath(path, fillColor)
    drawPath(path, strokeColor, style = Stroke(width = 2f))
}

/**
 * Draw an annular zone.
 */
private fun DrawScope.drawAnnularZone(
    zone: AnnularZone,
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
            rect = Rect(centerX - outerRx, centerY - outerRy, centerX + outerRx, centerY + outerRy),
            startAngleDegrees = zone.startAngle,
            sweepAngleDegrees = zone.sweepAngle,
            forceMoveTo = false
        )

        // Line to inner arc end
        val endAngle = zone.startAngle + zone.sweepAngle
        val endRad = Math.toRadians(endAngle.toDouble())
        val innerEndX = centerX + innerRx * cos(endRad).toFloat()
        val innerEndY = centerY + innerRy * sin(endRad).toFloat()
        lineTo(innerEndX, innerEndY)

        // Draw inner arc backwards
        arcTo(
            rect = Rect(centerX - innerRx, centerY - innerRy, centerX + innerRx, centerY + innerRy),
            startAngleDegrees = endAngle,
            sweepAngleDegrees = -zone.sweepAngle,
            forceMoveTo = false
        )

        close()
    }

    drawPath(path, fillColor)
    drawPath(path, strokeColor, style = Stroke(width = 2f))
}

/**
 * Draw a rectangularized arc zone.
 */
private fun DrawScope.drawRectArcZone(
    zone: RectArcZone,
    fillColor: Color,
    strokeColor: Color
) {
    val centerX = zone.centerX * size.width
    val centerY = zone.centerY * size.height
    val innerRx = zone.innerRadiusX * size.width
    val innerRy = zone.innerRadiusY * size.height

    val tl = Offset(zone.topLeft.x * size.width, zone.topLeft.y * size.height)
    val tr = Offset(zone.topRight.x * size.width, zone.topRight.y * size.height)
    val br = Offset(zone.bottomRight.x * size.width, zone.bottomRight.y * size.height)
    val bl = Offset(zone.bottomLeft.x * size.width, zone.bottomLeft.y * size.height)

    val path = Path().apply {
        // Start at top-left corner
        moveTo(tl.x, tl.y)

        // Line along top edge
        lineTo(tr.x, tr.y)

        // Line along right edge
        lineTo(br.x, br.y)

        // Line along bottom edge
        lineTo(bl.x, bl.y)

        // Now draw inner arc back to start
        // Calculate where the inner arc intersects the rectangular boundary
        val endAngle = zone.startAngle + zone.sweepAngle

        // Draw inner arc from end angle back to start angle
        arcTo(
            rect = Rect(centerX - innerRx, centerY - innerRy, centerX + innerRx, centerY + innerRy),
            startAngleDegrees = endAngle,
            sweepAngleDegrees = -zone.sweepAngle,
            forceMoveTo = false
        )

        close()
    }

    drawPath(path, fillColor)
    drawPath(path, strokeColor, style = Stroke(width = 2f))
}
