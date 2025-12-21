package com.example.gpswaypointing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun GPSApplication(
    azimuth: Float,
    waypoints: List<Waypoint>,
    selectedWaypoint: Waypoint?,
    waypointBearing: Float?,                 // NEW for Task 11
    onTrackingChanged: (Boolean) -> Unit,
    onAddWaypoint: () -> Unit,
    onClearWaypoints: () -> Unit,
    onWaypointSelected: (Waypoint) -> Unit
) {

    val isTracking = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PASS bearing into canvas
        CompassCanvas(
            azimuth = azimuth,
            waypointBearing = waypointBearing
        )

        // Start/Stop Tracking Button
        Button(
            onClick = {
                isTracking.value = !isTracking.value
                onTrackingChanged(isTracking.value)
            },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(text = if (isTracking.value) "Stop Tracking" else "Start Tracking")
        }

        if (isTracking.value) {
            // TASK 7: Add Waypoint Button
            Button(
                onClick = { onAddWaypoint() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Add Waypoint")
            }

            // TASK 9: Clear Waypoints Button
            Button(
                onClick = { showDeleteDialog.value = true },
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(text = "Clear Waypoints")
            }
        }

        // TASK 10: Waypoint selection list
        if (waypoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Select Waypoint:")

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(horizontal = 32.dp)
            ) {
                items(waypoints) { wp ->
                    val isSelected = (wp == selectedWaypoint)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWaypointSelected(wp) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = wp.name)
                        if (isSelected) {
                            Text(text = "●", color = Color.Green)
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog (Task 9)
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(text = "Confirm Delete") },
            text = { Text(text = "Are you sure you want to delete all saved waypoints?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearWaypoints()
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog.value = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CompassCanvas(
    azimuth: Float,
    waypointBearing: Float?          // NEW for Task 11
) {
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

        // Rotate compass according to device heading
        rotate(degrees = -azimuth, pivot = center) {

            // Compass letters
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

            // --- TASK 11: draw waypoint direction arrow ---
            if (waypointBearing != null) {
                val arrowLength = radius * 0.8f
                val angleRad = Math.toRadians(waypointBearing.toDouble())
                val endX = center.x + (arrowLength * Math.sin(angleRad)).toFloat()
                val endY = center.y - (arrowLength * Math.cos(angleRad)).toFloat()

                drawLine(
                    color = Color.Blue,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 8f
                )
            }
        }
    }
}
