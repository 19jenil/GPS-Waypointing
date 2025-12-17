package com.example.gpswaypointing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

// UPDATE: Added 'onAddWaypoint' parameter to the function signature
@Composable
fun GPSApplication(
    azimuth: Float,
    onTrackingChanged: (Boolean) -> Unit,
    onAddWaypoint: () -> Unit // New callback for Task 7
) {

    val isTracking = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompassCanvas(azimuth)

        // Start/Stop Tracking Button
        Button(
            onClick = {
                // Toggle state
                isTracking.value = !isTracking.value
                // CALL THE CALLBACK: This sends the signal to MainActivity
                onTrackingChanged(isTracking.value)
            },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(text = if (isTracking.value) "Stop Tracking" else "Start Tracking")
        }

        // --- NEW TASK 7 CODE STARTS HERE ---
        // Only show this button if we are currently tracking
        if (isTracking.value) {
            Button(
                onClick = {
                    onAddWaypoint() // Trigger the save logic in MainActivity
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Add Waypoint")
            }
        }
        // --- NEW TASK 7 CODE ENDS HERE ---
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
