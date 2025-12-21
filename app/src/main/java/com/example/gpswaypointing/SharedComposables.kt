package com.example.gpswaypointing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GPSApplication(
    azimuth: Float,
    waypoints: List<Waypoint>,
    selectedWaypoint: Waypoint?,
    waypointBearing: Float?,              // Task 11
    waypointDistanceMeters: Float?,       // Task 12
    nearbyWaypoints: List<Waypoint>,      // Task 13: within current range
    onTrackingChanged: (Boolean) -> Unit,
    onAddWaypoint: () -> Unit,
    onClearWaypoints: () -> Unit,
    onWaypointSelected: (Waypoint) -> Unit
) {
    val isTracking = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    // Task 17: scale for compass range (1x = 500 m, 4x = 2000 m)
    val scale = remember { mutableStateOf(1f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "GPS Waypointing") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Compass card
            CompassCard(
                azimuth = azimuth,
                waypointBearing = waypointBearing,
                nearbyWaypoints = nearbyWaypoints,
                selectedWaypoint = selectedWaypoint,
                scale = scale.value,
                onScaleChange = { newScale ->
                    scale.value = newScale.coerceIn(1f, 4f)
                },
                onWaypointSelected = onWaypointSelected
            )

            // Controls row
            ControlsRow(
                isTracking = isTracking.value,
                onToggleTracking = {
                    isTracking.value = !isTracking.value
                    onTrackingChanged(isTracking.value)
                },
                onAddWaypoint = onAddWaypoint,
                onClearWaypoints = { showDeleteDialog.value = true }
            )

            // Waypoints card
            WaypointsCard(
                waypoints = waypoints,
                selectedWaypoint = selectedWaypoint,
                distanceText = if (selectedWaypoint != null && waypointDistanceMeters != null) {
                    "Distance to ${selectedWaypoint.name}: ${waypointDistanceMeters.toInt()} m"
                } else null,
                onWaypointSelected = onWaypointSelected
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
fun CompassCard(
    azimuth: Float,
    waypointBearing: Float?,
    nearbyWaypoints: List<Waypoint>,
    selectedWaypoint: Waypoint?,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onWaypointSelected: (Waypoint) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Navigations",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            CompassCanvas(
                azimuth = azimuth,
                waypointBearing = waypointBearing,
                nearbyWaypoints = nearbyWaypoints,
                selectedWaypoint = selectedWaypoint,
                scale = scale,
                onScaleChange = onScaleChange,
                onWaypointSelected = onWaypointSelected
            )
        }
    }
}

@Composable
fun ControlsRow(
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    onAddWaypoint: () -> Unit,
    onClearWaypoints: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier.weight(1f),
            onClick = onToggleTracking
        ) {
            Text(text = if (isTracking) "Stop" else "Start")
        }

        Button(
            modifier = Modifier.weight(1f),
            onClick = onAddWaypoint
        ) {
            Text(text = "Add")
        }

        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onClearWaypoints,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Red
            )
        ) {
            Text(text = "Clear")
        }
    }
}

@Composable
fun WaypointsCard(
    waypoints: List<Waypoint>,
    selectedWaypoint: Waypoint?,
    distanceText: String?,
    onWaypointSelected: (Waypoint) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Waypoints",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (waypoints.isEmpty()) {
                Text(text = "No waypoints saved yet")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    items(waypoints) { wp ->
                        val isSelected = (wp == selectedWaypoint)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    else
                                        Color.Transparent
                                )
                                .clickable { onWaypointSelected(wp) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = wp.name)
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected waypoint",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            if (distanceText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun CompassCanvas(
    azimuth: Float,
    waypointBearing: Float?,
    nearbyWaypoints: List<Waypoint>,      // Task 13
    selectedWaypoint: Waypoint?,          // Task 14
    scale: Float,                         // Task 17: 1–4 (500–2000 m)
    onScaleChange: (Float) -> Unit,       // Task 17
    onWaypointSelected: (Waypoint) -> Unit   // Task 15
) {
    val circleRadius = 18f   // used for hit-test and selected size

    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(1f)
            .padding(8.dp)
            // Task 17: pinch to zoom to change scale
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    onScaleChange(scale * zoom)
                }
            }
            // TASK 15: handle touch input to select waypoint by circle
            .pointerInput(nearbyWaypoints, selectedWaypoint, scale) {
                while (true) {
                    val down = awaitPointerEventScope { awaitFirstDown() }
                    val touchX = down.position.x
                    val touchY = down.position.y

                    if (nearbyWaypoints.isNotEmpty()) {
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val centerX = width / 2f
                        val centerY = height / 2f
                        val minDim = min(width, height)
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

        // Task 17: map scale (1–4) to range (500–2000 m)
        val maxRangeMeters = 500f * scale
        // maxRangeMeters is available if you later want to reflect it in UI

        // TASK 13 & 14: draw coloured circles, highlight selected waypoint
        if (nearbyWaypoints.isNotEmpty()) {
            nearbyWaypoints.forEachIndexed { index, wp ->
                val angle = (360f / nearbyWaypoints.size) * index
                val angleRad = Math.toRadians(angle.toDouble())

                val ringRadius = radius
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
