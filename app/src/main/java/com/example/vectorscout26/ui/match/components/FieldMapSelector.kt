package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vectorscout26.utils.FieldZones

@Composable
fun FieldMapSelector(
    selectedZone: String,
    onZoneSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFE0E0E0))
                .border(2.dp, MaterialTheme.colorScheme.outline)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val zone = FieldZones.getZoneFromCoordinates(
                            offset.x,
                            offset.y,
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                        onZoneSelected(zone)
                    }
                }
        ) {
            val cellWidthDp = this.maxWidth / 5
            val cellHeightDp = this.maxHeight / 5

            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasCellWidth = size.width / 5f
                val canvasCellHeight = size.height / 5f

                // Draw grid lines
                for (i in 1..4) {
                    // Vertical lines
                    drawLine(
                        color = Color.Gray,
                        start = Offset(canvasCellWidth * i, 0f),
                        end = Offset(canvasCellWidth * i, size.height),
                        strokeWidth = 2f
                    )
                    // Horizontal lines
                    drawLine(
                        color = Color.Gray,
                        start = Offset(0f, canvasCellHeight * i),
                        end = Offset(size.width, canvasCellHeight * i),
                        strokeWidth = 2f
                    )
                }

                // Highlight selected zone
                if (selectedZone.isNotEmpty()) {
                    val row = selectedZone[0] - 'A'
                    val col = selectedZone.getOrNull(1)?.digitToIntOrNull()?.minus(1) ?: 0

                    drawRect(
                        color = Color(0xFF4CAF50).copy(alpha = 0.5f),
                        topLeft = Offset(col * canvasCellWidth, row * canvasCellHeight),
                        size = Size(canvasCellWidth, canvasCellHeight)
                    )
                }
            }

            // Draw zone labels
            for (row in 0..4) {
                for (col in 0..4) {
                    val zone = "${('A' + row)}${col + 1}"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = cellWidthDp * col,
                                top = cellHeightDp * row
                            )
                            .size(cellWidthDp, cellHeightDp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = zone,
                            fontSize = 12.sp,
                            color = if (zone == selectedZone) Color.White else Color.DarkGray
                        )
                    }
                }
            }
        }

        if (selectedZone.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selected: $selectedZone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
