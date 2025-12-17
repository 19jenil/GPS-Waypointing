package com.example.gpswaypointing

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.gpswaypointing.ui.theme.GPSWaypointingTheme
import androidx.compose.runtime.mutableStateListOf

class MainActivity : ComponentActivity(), SensorEventListener, LocationListener {

    // Store the current azimuth (rotation) value
    private var azimuth = mutableStateOf(0f)
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    // Store the latest location object
    private var currentLocation: Location? = null

    // List to store saved waypoints
    private val waypoints = mutableListOf<Waypoint>()

    // Permission launcher to ask user for GPS access
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }
            else -> {
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Managers
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        setContent {
            GPSWaypointingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // UPDATED CALL: Now includes onTrackingChanged AND onAddWaypoint
                    GPSApplication(
                        azimuth = azimuth.value,
                        onTrackingChanged = { shouldTrack ->
                            if (shouldTrack) {
                                startLocationTracking()
                            } else {
                                stopLocationTracking()
                            }
                        },
                        // NEW: Callback for adding a waypoint
                        onAddWaypoint = {
                            addWaypoint()
                        }
                    )
                }
            }
        }
    }

    // --- NEW FUNCTION for Task 7 ---
    private fun addWaypoint() {
        val location = currentLocation
        if (location != null) {
            val newWaypoint = Waypoint(
                id = waypoints.size + 1,
                latitude = location.latitude,
                longitude = location.longitude
            )
            waypoints.add(newWaypoint)
            Log.d("GPS_APP", "Waypoint Added: $newWaypoint")
        } else {
            Log.d("GPS_APP", "Cannot add waypoint: No location data yet.")
        }
    }

    // --- GPS TRACKING LOGIC ---

    private fun startLocationTracking() {
        // 1. Check permissions safely
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // 2. If NO permission, ask for it and EXIT function
        if (!hasFineLocation && !hasCoarseLocation) {
            Log.d("GPS_APP", "No permission. Requesting now...")
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        // 3. If YES permission, start tracking
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L, // 5 seconds
                0f,
                this
            )
            Log.d("GPS_APP", "Tracking Started")
        } catch (e: SecurityException) {
            Log.e("GPS_APP", "Security Exception: ${e.message}")
        }
    }

    private fun stopLocationTracking() {
        locationManager.removeUpdates(this)
        Log.d("GPS_APP", "Tracking Stopped")
    }

    // --- LocationListener Overrides ---
    override fun onLocationChanged(location: Location) {
        // UPDATE: Store location for adding waypoints
        currentLocation = location
        Log.d("GPS_APP", "New Location: ${location.latitude}, ${location.longitude}")
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    // --- SENSOR LOGIC (Compass) ---

    override fun onResume() {
        super.onResume()
        // Register listener for Rotation Vector sensor
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.also { rotationVector ->
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop all listeners when paused
        sensorManager.unregisterListener(this)
        stopLocationTracking()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthInDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            azimuth.value = azimuthInDegrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this assignment
    }
}
