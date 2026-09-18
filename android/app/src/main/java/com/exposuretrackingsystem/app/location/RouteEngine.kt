package com.exposuretrackingsystem.app.location

import android.util.Log
import com.exposuretrackingsystem.app.data.local.RoomRouteRepository
import android.location.Location
import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.exposuretrackingsystem.app.data.model.Route
import com.exposuretrackingsystem.app.data.model.RouteSegment
import com.exposuretrackingsystem.app.data.model.SensorSample
import com.exposuretrackingsystem.app.data.model.SensorSampleType
import com.exposuretrackingsystem.app.data.model.TransportType
import com.exposuretrackingsystem.app.data.local.RouteRepository

class RouteEngine(
    private val locationTrackingManager: LocationTrackingManager,
    private val diagnosticLogger: GpsDiagnosticLogger =
        GpsDiagnosticLogger(locationTrackingManager.appContext),
    private val accelerometerSamplesProvider: () -> List<SensorSample> = { emptyList() },
    private val routeRepository: RouteRepository? =
        RoomRouteRepository.create(locationTrackingManager.appContext) { exception ->
            Log.e(TAG, "Failed to persist completed Route", exception)
        }
) {
    var activeRoute: Route? = null
        private set

    var lastCompletedRoute: Route? = null
        private set

    private var acceptedPointCount = 0
    private var rejectedPointCount = 0

    init {
        locationTrackingManager.onLocationPoint = ::handleLocationPoint
        locationTrackingManager.onLocationError = {
            activeRoute = null
        }
    }

    fun startTracking(transportType: TransportType = TransportType.WALK): Boolean {
        if (activeRoute != null) {
            return false
        }

        val route = Route(
            startedAtMillis = System.currentTimeMillis(),
            transportType = transportType
        )
        activeRoute = route
        acceptedPointCount = 0
        rejectedPointCount = 0

        if (!locationTrackingManager.startLocationUpdates()) {
            activeRoute = null
            return false
        }

        diagnosticLogger.logStart(route.id)
        return true
    }

    fun stopTracking(): Route? {
        locationTrackingManager.stopLocationUpdates()

        val route = activeRoute ?: return null
        route.endedAtMillis = System.currentTimeMillis()
        updateDurationAndAverageSpeed(route, route.endedAtMillis!!)
        diagnosticLogger.logStop(
            routeId = route.id,
            totalDistanceMeters = route.totalDistanceMeters,
            durationSeconds = route.totalDurationSeconds,
            averageSpeedKmh = route.averageSpeedKmh,
            acceptedPoints = acceptedPointCount,
            rejectedPoints = rejectedPointCount
        )
        activeRoute = null
        lastCompletedRoute = route
        routeRepository?.saveCompletedRoute(route)
        return route
    }

    fun dispose() {
        locationTrackingManager.stopLocationUpdates()
        locationTrackingManager.clearLocationPointListener()
        activeRoute = null
    }

    private fun handleLocationPoint(locationPoint: LocationPoint) {
        val route = activeRoute ?: return
        val previousValidPoint = route.locationPoints.lastOrNull()
        val distanceMeters = previousValidPoint?.let { distanceBetween(it, locationPoint) }
        val segmentSpeedKmh = previousValidPoint?.let { calculateSpeedKmh(it, locationPoint, distanceMeters) }
        val rejectionReason = rejectionReason(
            point = locationPoint,
            previousValidPoint = previousValidPoint,
            distanceMeters = distanceMeters,
            segmentSpeedKmh = segmentSpeedKmh,
            accelerometerSamples = accelerometerSamplesProvider()
        )

        if (rejectionReason != null) {
            rejectedPointCount += 1
            diagnosticLogger.logPoint(
                point = locationPoint,
                decision = "REJECTED",
                rejectionReason = rejectionReason,
                distanceMeters = distanceMeters,
                segmentSpeedKmh = segmentSpeedKmh
            )
            return
        }

        acceptedPointCount += 1
        diagnosticLogger.logPoint(
            point = locationPoint,
            decision = "ACCEPTED",
            rejectionReason = "",
            distanceMeters = distanceMeters,
            segmentSpeedKmh = segmentSpeedKmh
        )
        val routePoint = locationPoint.copy(routeId = route.id)
        route.locationPoints += routePoint
        route.locationPoints.sortBy { it.timestampMillis }
        rebuildSegmentsAndMetrics(route)
    }

    private fun rebuildSegmentsAndMetrics(route: Route) {
        route.segments.clear()
        route.totalDistanceMeters = 0.0

        for (index in 1 until route.locationPoints.size) {
            val startPoint = route.locationPoints[index - 1]
            val endPoint = route.locationPoints[index]
            if (isValidConsecutivePair(startPoint, endPoint)) {
                val segment = createSegment(route, startPoint, endPoint)
                route.segments += segment
                route.totalDistanceMeters += segment.distanceMeters
            }
        }

        val lastTimestamp = route.locationPoints.lastOrNull()?.timestampMillis
            ?: route.startedAtMillis
        updateDurationAndAverageSpeed(route, lastTimestamp)
    }

    private fun createSegment(
        route: Route,
        startPoint: LocationPoint,
        endPoint: LocationPoint
    ): RouteSegment {
        val distanceMeters = distanceBetween(startPoint, endPoint)
        val durationSeconds = maxOf(
            0.0,
            (endPoint.timestampMillis - startPoint.timestampMillis) / MILLIS_PER_SECOND.toDouble()
        )
        val averageSpeedKmh = if (durationSeconds > 0.0) {
            distanceMeters / durationSeconds * METERS_PER_SECOND_TO_KMH
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
            0.0,
            (endTimeMillis - route.startedAtMillis) / MILLIS_PER_SECOND.toDouble()
        )
        val validDurationSeconds = route.segments.sumOf { it.durationSeconds }
        route.averageSpeedKmh = if (validDurationSeconds > 0.0) {
            route.totalDistanceMeters / validDurationSeconds * METERS_PER_SECOND_TO_KMH
        } else {
            0.0
        }
    }

    private fun isValidConsecutivePair(
        startPoint: LocationPoint,
        endPoint: LocationPoint
    ): Boolean {
        if (startPoint.accuracyMeters <= 0.0 ||
            endPoint.accuracyMeters <= 0.0 ||
            startPoint.accuracyMeters > MAX_ACCEPTABLE_ACCURACY_METERS ||
            endPoint.accuracyMeters > MAX_ACCEPTABLE_ACCURACY_METERS
        ) {
            return false
        }

        val durationSeconds =
            (endPoint.timestampMillis - startPoint.timestampMillis) / MILLIS_PER_SECOND.toDouble()
        if (!isValidRoutePointGap(durationSeconds)) {
            return false
        }

        if (startPoint.speedKmh > MAX_PLAUSIBLE_SPEED_KMH ||
            endPoint.speedKmh > MAX_PLAUSIBLE_SPEED_KMH
        ) {
            return false
        }

        val distanceMeters = distanceBetween(startPoint, endPoint)
        if (distanceMeters <= MIN_MEANINGFUL_DISTANCE_METERS) {
            return false
        }

        val computedSpeedKmh = distanceMeters / durationSeconds * METERS_PER_SECOND_TO_KMH
        if (computedSpeedKmh > MAX_PLAUSIBLE_SPEED_KMH) {
            return false
        }

        val bothPointsReportStationary =
            startPoint.speedKmh <= STATIONARY_SPEED_THRESHOLD_KMH &&
                endPoint.speedKmh <= STATIONARY_SPEED_THRESHOLD_KMH
        return !bothPointsReportStationary ||
            distanceMeters > MAX_STATIONARY_DRIFT_DISTANCE_METERS
    }

    private fun rejectionReason(
        point: LocationPoint,
        previousValidPoint: LocationPoint?,
        distanceMeters: Double?,
        segmentSpeedKmh: Double?,
        accelerometerSamples: List<SensorSample>
    ): String? {
        if (point.accuracyMeters <= 0.0 || point.accuracyMeters > MAX_ACCEPTABLE_ACCURACY_METERS) {
            return "unreliable_accuracy"
        }

        if (shouldRejectStationaryMotion(point.speedKmh, accelerometerSamples)) {
            return "stationary_motion"
        }

        if (previousValidPoint == null) {
            return null
        }

        if (point.timestampMillis <= previousValidPoint.timestampMillis) {
            return "non_positive_duration"
        }
        if (previousValidPoint.speedKmh > MAX_PLAUSIBLE_SPEED_KMH ||
            point.speedKmh > MAX_PLAUSIBLE_SPEED_KMH
        ) {
            return "reported_speed_spike"
        }
        if (distanceMeters == null || distanceMeters <= MIN_MEANINGFUL_DISTANCE_METERS) {
            return "insignificant_distance"
        }
        if (segmentSpeedKmh == null || segmentSpeedKmh > MAX_PLAUSIBLE_SPEED_KMH) {
            return "calculated_speed_spike"
        }

        val bothPointsReportStationary =
            previousValidPoint.speedKmh <= STATIONARY_SPEED_THRESHOLD_KMH &&
                point.speedKmh <= STATIONARY_SPEED_THRESHOLD_KMH
        if (bothPointsReportStationary && distanceMeters <= MAX_STATIONARY_DRIFT_DISTANCE_METERS) {
            return "stationary_gps_drift"
        }

        return null
    }

    private fun calculateSpeedKmh(
        first: LocationPoint,
        second: LocationPoint,
        distanceMeters: Double?
    ): Double? {
        val elapsedMillis = second.timestampMillis - first.timestampMillis
        val durationSeconds = elapsedMillis.toDouble() / MILLIS_PER_SECOND.toDouble()
        return distanceMeters?.let { calculateSpeedKmh(it, durationSeconds) }
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
        private const val TAG = "RouteEngine"
        private const val MILLIS_PER_SECOND = 1_000L
        private const val METERS_PER_SECOND_TO_KMH = 3.6
        internal const val MAX_ROUTE_POINT_GAP_SECONDS = 60.0
        private const val MAX_ACCEPTABLE_ACCURACY_METERS = 50.0
        private const val MIN_MEANINGFUL_DISTANCE_METERS = 3.0
        private const val MAX_STATIONARY_DRIFT_DISTANCE_METERS = 20.0
        private const val STATIONARY_SPEED_THRESHOLD_KMH = 2.0
        private const val MAX_PLAUSIBLE_SPEED_KMH = 200.0
    }
}

internal fun isStationaryMotion(samples: List<SensorSample>): Boolean {
    val accelerometerSamples = samples
        .asSequence()
        .filter { it.sensorType == SensorSampleType.ACCELEROMETER }
        .mapNotNull { sample ->
            val x = sample.x?.toDouble()
            val y = sample.y?.toDouble()
            val z = sample.z?.toDouble()
            if (x == null || y == null || z == null) {
                null
            } else {
                kotlin.math.sqrt(x * x + y * y + z * z)
            }
        }
        .toList()

    if (accelerometerSamples.size < MIN_STATIONARY_ACCELEROMETER_SAMPLES) {
        return false
    }

    val recentSamples = accelerometerSamples.takeLast(MIN_STATIONARY_ACCELEROMETER_SAMPLES)
    val magnitudeRange = recentSamples.maxOrNull()!! - recentSamples.minOrNull()!!
    val meanMagnitude = recentSamples.average()

    return magnitudeRange <= MAX_STATIONARY_ACCELERATION_RANGE_MPS2 &&
        kotlin.math.abs(meanMagnitude - STANDARD_GRAVITY_MPS2) <= MAX_STATIONARY_GRAVITY_DEVIATION_MPS2
}

private const val MIN_STATIONARY_ACCELEROMETER_SAMPLES = 5
private const val STANDARD_GRAVITY_MPS2 = 9.81
private const val MAX_STATIONARY_ACCELERATION_RANGE_MPS2 = 0.25
private const val MAX_STATIONARY_GRAVITY_DEVIATION_MPS2 = 0.30

internal fun shouldRejectStationaryMotion(
    gpsSpeedKmh: Double,
    accelerometerSamples: List<SensorSample>
): Boolean {
    return gpsSpeedKmh > 2.0 && isStationaryMotion(accelerometerSamples)
}

internal fun isValidRoutePointGap(elapsedSeconds: Double): Boolean {
    return elapsedSeconds > 0.0 &&
        elapsedSeconds <= RouteEngine.MAX_ROUTE_POINT_GAP_SECONDS
}

internal fun calculateSpeedKmh(distanceMeters: Double, durationSeconds: Double): Double? {
    return if (durationSeconds > 0.0) {
        distanceMeters / durationSeconds * 3.6
    } else {
        null
    }
}