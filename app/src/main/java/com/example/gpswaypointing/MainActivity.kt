package com.example.gpswaypointing

import android.Manifest
import android.content.Context
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
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity(), SensorEventListener, LocationListener {

    // Store the current azimuth (rotation) value
    private var azimuth = mutableStateOf(0f)
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    // Store the latest location object
    private var currentLocation: Location? = null

    // List to store saved waypoints
    private val waypoints = mutableStateListOf<Waypoint>()

    // Currently selected waypoint (Task 10)
    private var selectedWaypoint: Waypoint? = null

    // NEW FOR TASK 11: relative bearing from user to selected waypoint
    private var waypointRelativeBearing = mutableStateOf<Float?>(null)

    // Name of the file to save data to
    private val FILENAME = "waypoints.txt"

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

        // Load saved waypoints when app starts
        loadWaypointsFromFile()

        setContent {
            GPSWaypointingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // UPDATED CALL: now also passes waypointBearing
                    GPSApplication(
                        azimuth = azimuth.value,
                        waypoints = waypoints,
                        selectedWaypoint = selectedWaypoint,
                        waypointBearing = waypointRelativeBearing.value,
                        onTrackingChanged = { shouldTrack ->
                            if (shouldTrack) {
                                startLocationTracking()
                            } else {
                                stopLocationTracking()
                            }
                        },
                        onAddWaypoint = {
                            addWaypoint()
                        },
                        onClearWaypoints = {
                            clearWaypoints()
                        },
                        onWaypointSelected = { wp ->
                            selectedWaypoint = wp
                            Log.d("GPS_APP", "Selected waypoint: $wp")
                            updateWaypointBearing()   // recompute direction to this waypoint
                        }
                    )
                }
            }
        }
    }

    // --- TASK 11: compute bearing from user to selected waypoint ---
    private fun updateWaypointBearing() {
        val loc = currentLocation
        val wp = selectedWaypoint
        if (loc == null || wp == null) {
            waypointRelativeBearing.value = null
            return
        }

        val wpLocation = Location("waypoint").apply {
            latitude = wp.latitude
            longitude = wp.longitude
        }

        // Bearing from user to waypoint (may be negative)
        var bearing = loc.bearingTo(wpLocation)  // degrees from north, clockwise
        if (bearing < 0f) bearing += 360f

        // Adjust by device heading so 0° is "straight ahead" on the compass
        val relative = bearing - azimuth.value
        waypointRelativeBearing.value = ((relative + 360f) % 360f)
    }

    // --- FUNCTION for Task 7 & 8 ---
    private fun addWaypoint() {
        val location = currentLocation
        if (location != null) {
            val newWaypoint = Waypoint(
                id = waypoints.size + 1,
                latitude = location.latitude,
                longitude = location.longitude
            )
            waypoints.add(newWaypoint)
            saveWaypointsToFile()
            Log.d("GPS_APP", "Waypoint Added: $newWaypoint")
        } else {
            Log.d("GPS_APP", "Cannot add waypoint: No location data yet.")
        }
    }

    // --- NEW FUNCTION for Task 9: Clear Data ---
    private fun clearWaypoints() {
        waypoints.clear()
        selectedWaypoint = null
        waypointRelativeBearing.value = null
        try {
            openFileOutput(FILENAME, Context.MODE_PRIVATE).use {
                it.write("".toByteArray())
            }
            Log.d("GPS_APP", "All waypoints cleared.")
        } catch (e: IOException) {
            Log.e("GPS_APP", "Error clearing file: ${e.message}")
        }
    }

    // --- TASK 8: FILE I/O FUNCTIONS ---

    private fun saveWaypointsToFile() {
        try {
            val fileContent = StringBuilder()
            for (wp in waypoints) {
                fileContent.append("${wp.id},${wp.latitude},${wp.longitude}\n")
            }
            openFileOutput(FILENAME, Context.MODE_PRIVATE).use {
                it.write(fileContent.toString().toByteArray())
            }
            Log.d("GPS_APP", "Saved ${waypoints.size} waypoints to $FILENAME")
        } catch (e: IOException) {
            Log.e("GPS_APP", "Error saving file: ${e.message}")
        }
    }

    private fun loadWaypointsFromFile() {
        try {
            val file = File(filesDir, FILENAME)
            if (file.exists()) {
                waypoints.clear()
                file.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.size >= 3) {
                        val id = parts[0].toIntOrNull()
                        val lat = parts[1].toDoubleOrNull()
                        val long = parts[2].toDoubleOrNull()
                        if (id != null && lat != null && long != null) {
                            waypoints.add(Waypoint(id, lat, long))
                        }
                    }
                }
                Log.d("GPS_APP", "Loaded ${waypoints.size} waypoints from file")
            }
        } catch (e: IOException) {
            Log.e("GPS_APP", "Error loading file: ${e.message}")
        }
    }

    // --- GPS TRACKING LOGIC ---

    private fun startLocationTracking() {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 0f, this)
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
        currentLocation = location
        Log.d("GPS_APP", "New Location: ${location.latitude}, ${location.longitude}")
        updateWaypointBearing()   // keep direction updated as user moves
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    // --- SENSOR LOGIC (Compass) ---

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.also { rotationVector ->
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        stopLocationTracking()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            azimuth.value = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            updateWaypointBearing()   // bearing depends on azimuth as well
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
