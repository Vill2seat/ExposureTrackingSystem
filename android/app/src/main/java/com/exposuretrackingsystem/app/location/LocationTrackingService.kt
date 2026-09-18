package com.exposuretrackingsystem.app.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.exposuretrackingsystem.app.MainActivity
import com.exposuretrackingsystem.app.R
import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationTrackingService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopTracking()
            ACTION_START, null -> startTracking()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    private fun startTracking() {
        if (!hasLocationPermission()) {
            notifyError(SecurityException("Location permission is not granted"))
            stopSelf()
            return
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        if (locationCallback != null) {
            return
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
                    listener?.onLocationPoint?.invoke(location.toLocationPoint())
                }
            }
        }
        locationCallback = callback
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { exception ->
                locationCallback = null
                notifyError(exception)
                stopSelf()
            }
    }

    private fun stopTracking() {
        stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notifyError(exception: Exception) {
        listener?.onLocationError?.invoke(exception)
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.location_tracking_notification_title))
            .setContentText(getString(R.string.location_tracking_notification_text))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.location_tracking_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
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
        private const val ACTION_START =
            "com.exposuretrackingsystem.app.location.action.START"
        private const val ACTION_STOP =
            "com.exposuretrackingsystem.app.location.action.STOP"
        private const val NOTIFICATION_CHANNEL_ID = "location_tracking"
        private const val NOTIFICATION_ID = 1002
        private const val UPDATE_INTERVAL_MILLIS = 5_000L
        private const val MIN_UPDATE_INTERVAL_MILLIS = 2_000L
        private const val METERS_PER_SECOND_TO_KMH = 3.6

        @Volatile
        private var listener: LocationUpdateListener? = null

        fun setListener(
            context: Context,
            onLocationPoint: (LocationPoint) -> Unit,
            onLocationError: (Exception) -> Unit
        ) {
            listener = LocationUpdateListener(onLocationPoint, onLocationError)
        }

        fun clearListener(context: Context) {
            listener = null
        }

        fun start(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java)
                .setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java)
                .setAction(ACTION_STOP)
            context.startService(intent)
        }
    }

    private class LocationUpdateListener(
        val onLocationPoint: (LocationPoint) -> Unit,
        val onLocationError: (Exception) -> Unit
    )
}
