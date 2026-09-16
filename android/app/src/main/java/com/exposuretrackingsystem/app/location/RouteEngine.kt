package com.exposuretrackingsystem.app.location

import android.location.Location
import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.exposuretrackingsystem.app.data.model.Route
import com.exposuretrackingsystem.app.data.model.RouteSegment

class RouteEngine(
    private val locationTrackingManager: LocationTrackingManager
) {
    var activeRoute: Route? = null
        private set

    var lastCompletedRoute: Route? = null
        private set

    init {
        locationTrackingManager.onLocationPoint = ::handleLocationPoint
    }

    fun startTracking(): Boolean {
        if (activeRoute != null) {
            return false
        }

        val route = Route(startedAtMillis = System.currentTimeMillis())
        activeRoute = route

        if (!locationTrackingManager.startLocationUpdates()) {
            activeRoute = null
            return false
        }

        return true
    }

    fun stopTracking(): Route? {
        locationTrackingManager.stopLocationUpdates()

        val route = activeRoute ?: return null
        route.endedAtMillis = System.currentTimeMillis()
        updateDurationAndAverageSpeed(route, route.endedAtMillis!!)
        activeRoute = null
        lastCompletedRoute = route
        return route
    }

    private fun handleLocationPoint(locationPoint: LocationPoint) {
        val route = activeRoute ?: return
        val previousPoint = route.locationPoints.lastOrNull()

        if (previousPoint != null) {
            val segment = createSegment(route, previousPoint, locationPoint)
            route.segments += segment
            route.totalDistanceMeters += segment.distanceMeters
        }

        route.locationPoints += locationPoint
        updateDurationAndAverageSpeed(route, locationPoint.timestampMillis)
    }

    private fun createSegment(
        route: Route,
        startPoint: LocationPoint,
        endPoint: LocationPoint
    ): RouteSegment {
        val distanceMeters = distanceBetween(startPoint, endPoint)
        val durationSeconds = maxOf(
            0L,
            (endPoint.timestampMillis - startPoint.timestampMillis) / MILLIS_PER_SECOND
        )
        val averageSpeedKmh = if (durationSeconds > 0L) {
            distanceMeters / durationSeconds * SECONDS_TO_HOURS
        } else {
            0.0
        }

        return RouteSegment(
            routeId = route.id,
            startLocationPointId = startPoint.id,
            endLocationPointId = endPoint.id,
            activityId = null,
            startTimeMillis = startPoint.timestampMillis,
            endTimeMillis = endPoint.timestampMillis,
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            averageSpeedKmh = averageSpeedKmh
        )
    }

    private fun updateDurationAndAverageSpeed(route: Route, endTimeMillis: Long) {
        route.totalDurationSeconds = maxOf(
            0L,
            (endTimeMillis - route.startedAtMillis) / MILLIS_PER_SECOND
        )
        route.averageSpeedKmh = if (route.totalDurationSeconds > 0L) {
            route.totalDistanceMeters / route.totalDurationSeconds * SECONDS_TO_HOURS
        } else {
            0.0
        }
    }

    private fun distanceBetween(first: LocationPoint, second: LocationPoint): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            first.latitude,
            first.longitude,
            second.latitude,
            second.longitude,
            results
        )
        return results[0].toDouble()
    }

    companion object {
        private const val MILLIS_PER_SECOND = 1_000L
        private const val SECONDS_TO_HOURS = 3_600.0
    }
}