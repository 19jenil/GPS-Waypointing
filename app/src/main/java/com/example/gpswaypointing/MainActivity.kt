package com.example.gpswaypointing

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.gpswaypoint.GPSApplication
import com.example.gpswaypointing.ui.theme.GPSWaypointingTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    // Store the current azimuth (rotation) value
    private var azimuth = mutableStateOf(0f)
    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Sensor Manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        setContent {
            GPSWaypointingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the azimuth value to the Composable
                    GPSApplication(azimuth.value)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register listener for Rotation Vector sensor
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.also { rotationVector ->
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop listening when app is paused to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            // Convert rotation vector to rotation matrix
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            // Get orientation angles from the matrix
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // The first value is the azimuth (rotation around Z-axis)
            // Convert from radians to degrees
            val azimuthInDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

            // Update the state (triggers UI redraw)
            azimuth.value = azimuthInDegrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this assignment
    }
}
