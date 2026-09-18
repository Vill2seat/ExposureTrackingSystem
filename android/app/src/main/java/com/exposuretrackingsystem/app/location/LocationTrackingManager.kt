package com.exposuretrackingsystem.app.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.exposuretrackingsystem.app.data.model.LocationPoint

class LocationTrackingManager(context: Context) {
    internal val appContext = context.applicationContext

    var onLocationPoint: ((LocationPoint) -> Unit)? = null
    var onLocationError: ((Exception) -> Unit)? = null

    init {
        LocationTrackingService.setListener(
            context = appContext,
            onLocationPoint = { point -> onLocationPoint?.invoke(point) },
            onLocationError = { exception -> onLocationError?.invoke(exception) }
        )
    }

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
        if (!hasLocationPermission()) {
            return false
        }

        return try {
            LocationTrackingService.start(appContext)
            true
        } catch (exception: Exception) {
            onLocationError?.invoke(exception)
            false
        }
    }

    fun stopLocationUpdates() {
        LocationTrackingService.stop(appContext)
    }

    fun clearLocationPointListener() {
        onLocationPoint = null
        onLocationError = null
        LocationTrackingService.clearListener(appContext)
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
