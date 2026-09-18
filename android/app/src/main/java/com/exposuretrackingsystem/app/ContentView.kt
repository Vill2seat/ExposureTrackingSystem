package com.exposuretrackingsystem.app

import android.content.Intent
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.exposuretrackingsystem.app.data.model.SensorSampleType
import com.exposuretrackingsystem.app.data.model.TransportType
import com.exposuretrackingsystem.app.location.LocationTrackingManager
import com.exposuretrackingsystem.app.location.RouteEngine
import com.exposuretrackingsystem.app.data.local.RoomRouteRepository
import com.exposuretrackingsystem.app.sensors.SensorDataManager
import com.exposuretrackingsystem.app.sensors.SensorDiagnosticLogger
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun ContentView() {
    val context = LocalContext.current
    val locationTrackingManager = remember { LocationTrackingManager(context) }
    val sensorDataManager = remember { SensorDataManager(context) }
    val routeRepository = remember { RoomRouteRepository.create(context) }
    val routeEngine = remember {
        RouteEngine(
            locationTrackingManager = locationTrackingManager,
            accelerometerSamplesProvider = sensorDataManager::samplesSnapshot,
            routeRepository = routeRepository
        )
    }
    val sensorDiagnosticLogger = remember { SensorDiagnosticLogger(context) }

    var isTracking by remember { mutableStateOf(false) }
    var permissionRequestPending by remember { mutableStateOf(false) }
    var durationSeconds by remember { mutableStateOf(0.0) }
    var distanceMeters by remember { mutableStateOf(0.0) }
    var averageSpeedKmh by remember { mutableStateOf(0.0) }
    var sensorSampleCounts by remember { mutableStateOf(sensorDataManager.sampleCountsSnapshot()) }

    fun startTrackingSession(): Boolean {
        val trackingStarted = routeEngine.startTracking(TransportType.WALK)
        if (trackingStarted) {
            sensorDataManager.clearSamples()
            sensorDataManager.start()
        }
        return trackingStarted
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val permissionGranted = fineGranted || coarseGranted

        if (permissionRequestPending && permissionGranted) {
            isTracking = startTrackingSession()
        }
        permissionRequestPending = false
    }

    DisposableEffect(routeEngine) {
        onDispose {
            routeEngine.dispose()
        }
    }

    DisposableEffect(sensorDataManager, sensorDiagnosticLogger) {
        sensorDataManager.onSample = sensorDiagnosticLogger::logSample
        onDispose {
            sensorDataManager.onSample = null
            sensorDataManager.stop()
        }
    }

    LaunchedEffect(isTracking) {
        while (isTracking) {
            val route = routeEngine.activeRoute
            if (route == null) {
                sensorDataManager.stop()
                sensorSampleCounts = sensorDataManager.sampleCountsSnapshot()
                isTracking = false
                break
            }

            durationSeconds = route.totalDurationSeconds
            distanceMeters = route.totalDistanceMeters
            averageSpeedKmh = route.averageSpeedKmh
            sensorSampleCounts = sensorDataManager.sampleCountsSnapshot()
            delay(METRICS_REFRESH_MILLIS)
        }
    }

    fun startTracking() {
        if (isTracking || permissionRequestPending) {
            return
        }

        if (locationTrackingManager.hasLocationPermission()) {
            isTracking = startTrackingSession()
        } else {
            permissionRequestPending = true
            permissionLauncher.launch(LocationTrackingManager.LOCATION_PERMISSIONS)
        }
    }

    fun stopTracking() {
        if (!isTracking) {
            return
        }

        sensorDataManager.stop()
        sensorSampleCounts = sensorDataManager.sampleCountsSnapshot()
        val finalizedRoute = routeEngine.stopTracking()
        durationSeconds = finalizedRoute?.totalDurationSeconds ?: durationSeconds
        distanceMeters = finalizedRoute?.totalDistanceMeters ?: distanceMeters
        averageSpeedKmh = finalizedRoute?.averageSpeedKmh ?: averageSpeedKmh
        isTracking = false
    }

    fun exportDiagnosticLog() {
        val logFile = File(context.filesDir, "gps_diagnostic.csv")
        val logUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            logFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, logUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export diagnostic log"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Exposure Tracking System",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Tracking: ${if (isTracking) "Active" else "Stopped"}")
        Text(text = "Duration: ${"%.1f".format(durationSeconds)} s")
        Text(text = "Distance: ${"%.1f".format(distanceMeters)} m")
        Text(text = "Average speed: ${"%.1f".format(averageSpeedKmh)} km/h")
        Text(
            text = "Sensors available: ${sensorDataManager.availableSensorTypes.joinToString { it.name }}"
        )
        Text(
            text = "Sensor samples: ${SensorSampleType.values().joinToString { sampleType ->
                "${sampleType.name}=${sensorSampleCounts[sampleType] ?: 0}"
            }}"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = ::startTracking,
                enabled = !isTracking && !permissionRequestPending,
                modifier = Modifier.weight(1f)
            ) {
                Text("Start Tracking")
            }
            Button(
                onClick = ::stopTracking,
                enabled = isTracking,
                modifier = Modifier.weight(1f)
            ) {
                Text("Stop Tracking")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = ::exportDiagnosticLog,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export diagnostic log")
        }
    }
}

private const val METRICS_REFRESH_MILLIS = 500L
