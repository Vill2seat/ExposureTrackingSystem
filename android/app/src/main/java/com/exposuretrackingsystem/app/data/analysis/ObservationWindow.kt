package com.exposuretrackingsystem.app.data.analysis

import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.exposuretrackingsystem.app.data.model.Route
import com.exposuretrackingsystem.app.data.model.RouteSegment
import com.exposuretrackingsystem.app.data.model.SensorSample
import com.exposuretrackingsystem.app.data.model.SensorSampleType

/**
 * A read-only analysis snapshot. GPS timestamps come from Android Location.time.
 * Sensor timestamps come from System.currentTimeMillis() when the callback is handled.
 * Timestamp synchronization is intentionally not attempted here.
 */
data class ObservationWindow(
    val startTimestampMillis: Long,
    val endTimestampMillis: Long,
    val locationPoints: List<LocationPoint>,
    val segments: List<RouteSegment>,
    val sensorSamples: List<SensorSample>,
    val availableSensorTypes: Set<SensorSampleType>,
    val gpsPointCount: Int,
    val validSegmentCount: Int,
    val largestGpsGapMillis: Long?,
    val minimumAccuracyMeters: Double?,
    val maximumAccuracyMeters: Double?,
    val averageAccuracyMeters: Double?
) {
    val sensorSampleCounts: Map<SensorSampleType, Int>
        get() = SensorSampleType.values().associateWith { type ->
            sensorSamples.count { it.sensorType == type }
        }

    companion object {
        fun from(
            route: Route,
            startTimestampMillis: Long,
            endTimestampMillis: Long,
            sensorSamples: List<SensorSample> = emptyList(),
            availableSensorTypes: Set<SensorSampleType> = emptySet()
        ): ObservationWindow {
            require(startTimestampMillis <= endTimestampMillis) {
                "Observation window start must not be after its end"
            }

            val points = route.locationPoints.locationPointsInWindow(startTimestampMillis, endTimestampMillis)
            val segments = route.segments.routeSegmentsInWindow(startTimestampMillis, endTimestampMillis)
            val accuracies = points
                .map { it.accuracyMeters }
                .filter { it.isFinite() && it > 0.0 }
            val sortedPointTimestamps = points.map { it.timestampMillis }.sorted()

            return ObservationWindow(
                startTimestampMillis = startTimestampMillis,
                endTimestampMillis = endTimestampMillis,
                locationPoints = points,
                segments = segments,
                sensorSamples = sensorSamples.sensorSamplesInWindow(startTimestampMillis, endTimestampMillis),
                availableSensorTypes = availableSensorTypes.toSet(),
                gpsPointCount = points.size,
                validSegmentCount = segments.size,
                largestGpsGapMillis = sortedPointTimestamps
                    .zipWithNext()
                    .maxOfOrNull { (first, second) -> second - first },
                minimumAccuracyMeters = accuracies.minOrNull(),
                maximumAccuracyMeters = accuracies.maxOrNull(),
                averageAccuracyMeters = accuracies.takeIf { it.isNotEmpty() }?.average()
            )
        }
    }
}

fun List<LocationPoint>.locationPointsInWindow(
    startTimestampMillis: Long,
    endTimestampMillis: Long
): List<LocationPoint> =
    filter { it.timestampMillis in startTimestampMillis..endTimestampMillis }

fun List<RouteSegment>.routeSegmentsInWindow(
    startTimestampMillis: Long,
    endTimestampMillis: Long
): List<RouteSegment> =
    filter {
        it.startTimeMillis <= endTimestampMillis &&
            it.endTimeMillis >= startTimestampMillis
    }

fun List<SensorSample>.sensorSamplesInWindow(
    startTimestampMillis: Long,
    endTimestampMillis: Long
): List<SensorSample> =
    filter { it.timestampMillis in startTimestampMillis..endTimestampMillis }
