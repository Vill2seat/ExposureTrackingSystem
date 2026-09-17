package com.exposuretrackingsystem.app.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationTrackingManager(context: Context) {
    internal val appContext = context.applicationContext
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    private var locationCallback: LocationCallback? = null

    var onLocationPoint: ((LocationPoint) -> Unit)? = null
    var onLocationError: ((Exception) -> Unit)? = null

    fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    fun requestLocationPermission(
        activity: Activity,
        requestCode: Int = LOCATION_PERMISSION_REQUEST_CODE
    ) {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                activity,
                LOCATION_PERMISSIONS,
                requestCode
            )
        }
    }

    fun startLocationUpdates(): Boolean {
        if (!hasLocationPermission() || locationCallback != null) {
            return hasLocationPermission()
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MILLIS
        )
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    onLocationPoint?.invoke(location.toLocationPoint())
                }
            }
        }

        locationCallback = callback
        fusedLocationClient
            .requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { exception ->
                locationCallback = null
                onLocationError?.invoke(exception)
            }

        return true
    }

    fun stopLocationUpdates() {
        val callback = locationCallback ?: return
        fusedLocationClient.removeLocationUpdates(callback)
        locationCallback = null
    }

    fun clearLocationPointListener() {
        onLocationPoint = null
        onLocationError = null
    }

    private fun Location.toLocationPoint(): LocationPoint {
        return LocationPoint(
            timestampMillis = time,
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = if (hasAltitude()) altitude else 0.0,
            headingDegrees = if (hasBearing()) bearing.toDouble() else 0.0,
            accuracyMeters = if (hasAccuracy()) accuracy.toDouble() else 0.0,
            speedKmh = if (hasSpeed()) speed.toDouble() * METERS_PER_SECOND_TO_KMH else 0.0
        )
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private const val UPDATE_INTERVAL_MILLIS = 5_000L
        private const val MIN_UPDATE_INTERVAL_MILLIS = 2_000L
        private const val METERS_PER_SECOND_TO_KMH = 3.6
    }
}
