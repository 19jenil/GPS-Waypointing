package com.example.gpswaypoint

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.rotate // Import for rotation
import androidx.compose.ui.unit.dp

// Update function signature to accept azimuth
@Composable
fun GPSApplication(azimuth: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pass azimuth to the canvas
        CompassCanvas(azimuth)
    }
}

@Composable
fun CompassCanvas(azimuth: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
            .background(Color.LightGray)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)
        val radius = size.minDimension / 2.5f

        // ROTATE THE ENTIRE CANVAS
        // We invert the azimuth (-) so the compass rotates opposite to the user
        rotate(degrees = -azimuth, pivot = center) {

            // Draw North (Highlighted Red)
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "N",
                    center.x,
                    center.y - radius,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.RED
                        textSize = 60f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }

            // Draw South
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "S",
                    center.x,
                    center.y + radius + 40f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 60f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // Draw East
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "E",
                    center.x + radius,
                    center.y + 20f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 60f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // Draw West
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "W",
                    center.x - radius,
                    center.y + 20f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 60f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
