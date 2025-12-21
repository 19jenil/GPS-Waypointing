package com.example.gpswaypointing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun GPSApplication(
    azimuth: Float,
    waypoints: List<Waypoint>,
    selectedWaypoint: Waypoint?,
    waypointBearing: Float?,              // Task 11
    waypointDistanceMeters: Float?,       // Task 12
    nearbyWaypoints: List<Waypoint>,      // Task 13: within 500 m
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
        // Compass with waypoint direction and nearby waypoint circles
        CompassCanvas(
            azimuth = azimuth,
            waypointBearing = waypointBearing,
            nearbyWaypoints = nearbyWaypoints,
            selectedWaypoint = selectedWaypoint,
            onWaypointSelected = onWaypointSelected      // Task 15
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

        // TASK 12: Distance to selected waypoint
        if (selectedWaypoint != null && waypointDistanceMeters != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Distance to ${selectedWaypoint.name}: ${waypointDistanceMeters.toInt()} m"
            )
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
    waypointBearing: Float?,
    nearbyWaypoints: List<Waypoint>,      // Task 13
    selectedWaypoint: Waypoint?,          // Task 14
    onWaypointSelected: (Waypoint) -> Unit   // Task 15
) {
    val circleRadius = 18f   // used for hit-test and selected size

    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
            .background(Color.LightGray)
            .padding(16.dp)
            // TASK 15: handle touch input to select waypoint by circle
            .pointerInput(nearbyWaypoints, selectedWaypoint) {
                while (true) {
                    val down = awaitPointerEventScope { awaitFirstDown() }
                    val touchX = down.position.x
                    val touchY = down.position.y

                    if (nearbyWaypoints.isNotEmpty()) {
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val centerX = width / 2f
                        val centerY = height / 2f
                        val minDim = kotlin.math.min(width, height)
                        val radius = minDim / 2.5f
                        val ringRadius = radius

                        nearbyWaypoints.forEachIndexed { index, wp ->
                            val angle = (360f / nearbyWaypoints.size) * index
                            val angleRad = Math.toRadians(angle.toDouble())
                            val cx = centerX + (ringRadius * Math.sin(angleRad)).toFloat()
                            val cy = centerY - (ringRadius * Math.cos(angleRad)).toFloat()
                            val dx = touchX - cx
                            val dy = touchY - cy
                            if (dx * dx + dy * dy <= circleRadius * circleRadius) {
                                onWaypointSelected(wp)
                                return@pointerInput
                            }
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)
        val radius = size.minDimension / 2.5f

        // TASK 13 & 14: draw coloured circles, highlight selected waypoint
        if (nearbyWaypoints.isNotEmpty()) {
            nearbyWaypoints.forEachIndexed { index, wp ->
                val angle = (360f / nearbyWaypoints.size) * index
                val angleRad = Math.toRadians(angle.toDouble())

                val ringRadius = radius  // edge corresponds to 500 m
                val cx = center.x + (ringRadius * Math.sin(angleRad)).toFloat()
                val cy = center.y - (ringRadius * Math.cos(angleRad)).toFloat()

                val isSelected = (wp == selectedWaypoint)

                drawCircle(
                    color = if (isSelected) Color.Green else Color.Magenta,
                    radius = if (isSelected) circleRadius else 12f,
                    center = Offset(cx, cy)
                )
            }
        }

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

            // TASK 11: draw waypoint direction arrow
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
