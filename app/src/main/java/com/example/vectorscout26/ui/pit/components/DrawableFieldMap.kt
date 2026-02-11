package com.example.vectorscout26.ui.pit.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Environment
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.R
import java.io.File
import java.io.FileOutputStream

@Composable
fun DrawableFieldMap(
    teamNumber: String,
    pathName: String,
    strokes: List<List<Pair<Float, Float>>>,  // Strokes from ViewModel
    onStrokesChanged: (List<List<Pair<Float, Float>>>) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onDrawingSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStroke by remember { mutableStateOf(listOf<Offset>()) }
    var canvasSize by remember { mutableStateOf(Pair(0f, 0f)) }

    val density = LocalDensity.current
    val strokeWidth = with(density) { 4.dp.toPx() }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Draw Auto Path",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Drawing canvas with field image background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f) // Field aspect ratio (wider than tall)
                .border(2.dp, MaterialTheme.colorScheme.outline)
        ) {
            // Field background image
            Image(
                painter = painterResource(id = R.drawable.combined_field),
                contentDescription = "Field",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Drawing overlay
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(pathName) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStroke = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke = currentStroke + change.position
                            },
                            onDragEnd = {
                                if (currentStroke.isNotEmpty()) {
                                    // Convert Offset to Pair and add to strokes
                                    val newStroke = currentStroke.map { Pair(it.x, it.y) }
                                    onStrokesChanged(strokes + listOf(newStroke))
                                    currentStroke = emptyList()
                                }
                            }
                        )
                    }
            ) {
                canvasSize = Pair(size.width, size.height)

                // Draw completed strokes from ViewModel
                strokes.forEach { strokePoints ->
                    if (strokePoints.size > 1) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(strokePoints.first().first, strokePoints.first().second)
                            strokePoints.drop(1).forEach { point ->
                                lineTo(point.first, point.second)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color.Yellow,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Draw current stroke being drawn
                if (currentStroke.size > 1) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(currentStroke.first().x, currentStroke.first().y)
                        currentStroke.drop(1).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color.Yellow,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Undo button
            OutlinedButton(
                onClick = onUndo,
                enabled = strokes.isNotEmpty(),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Undo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo")
            }

            // Clear button
            OutlinedButton(
                onClick = onClear,
                enabled = strokes.isNotEmpty(),
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear")
            }

            // Save button
            Button(
                onClick = {
                    val filePath = saveDrawingToPng(
                        context = context,
                        teamNumber = teamNumber,
                        pathName = pathName,
                        strokes = strokes,
                        width = canvasSize.first.toInt(),
                        height = canvasSize.second.toInt()
                    )
                    if (filePath != null) {
                        onDrawingSaved(filePath)
                    }
                },
                enabled = strokes.isNotEmpty() && teamNumber.isNotBlank(),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("Save $pathName")
            }
        }
    }
}

private fun saveDrawingToPng(
    context: Context,
    teamNumber: String,
    pathName: String,
    strokes: List<List<Pair<Float, Float>>>,
    width: Int,
    height: Int
): String? {
    if (width <= 0 || height <= 0) return null

    try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Load and draw field background image
        val fieldBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.combined_field)
        if (fieldBitmap != null) {
            val scaledField = Bitmap.createScaledBitmap(fieldBitmap, width, height, true)
            canvas.drawBitmap(scaledField, 0f, 0f, null)
            if (scaledField != fieldBitmap) {
                scaledField.recycle()
            }
            fieldBitmap.recycle()
        }

        // Draw strokes
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            color = android.graphics.Color.YELLOW
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        strokes.forEach { strokePoints ->
            if (strokePoints.size > 1) {
                val path = Path()
                path.moveTo(strokePoints.first().first, strokePoints.first().second)
                strokePoints.drop(1).forEach { point ->
                    path.lineTo(point.first, point.second)
                }
                canvas.drawPath(path, paint)
            }
        }

        // Save to Pictures directory
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: return null

        val fileName = "${teamNumber}_${pathName}.png"
        val file = File(picturesDir, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        bitmap.recycle()
        return file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
