package com.exposuretrackingsystem.app.data.analysis

import com.exposuretrackingsystem.app.data.model.SensorSample
import kotlin.math.abs
import kotlin.math.sqrt

data class TransportFeatures(
    val observationDurationSeconds: Double,
    val gpsPointCount: Int,
    val validSegmentCount: Int,
    val totalDistanceMeters: Double,
    val medianReportedSpeedKmh: Double?,
    val maxReportedSpeedKmh: Double?,
    val medianCalculatedSegmentSpeedKmh: Double?,
    val maxCalculatedSegmentSpeedKmh: Double?,
    val speedStandardDeviationKmh: Double?,
    val stopRatio: Double?,
    val headingChangeRateDegreesPerMinute: Double?,
    val totalHeadingChangeDegrees: Double?,
    val altitudeRangeMeters: Double?,
    val largestGpsGapSeconds: Double?,
    val minAccuracyMeters: Double?,
    val averageAccuracyMeters: Double?,
    val maxAccuracyMeters: Double?,
    val medianSegmentDurationSeconds: Double?,
    val medianSegmentDistanceMeters: Double?,
    val accelerometerSampleCount: Int,
    val gyroscopeSampleCount: Int,
    val pressureSampleCount: Int,
    val accelerometerMagnitudeMean: Double?,
    val accelerometerMagnitudeStdDev: Double?,
    val accelerometerMagnitudeRange: Double?,
    val gyroscopeMagnitudeMean: Double?,
    val gyroscopeMagnitudeStdDev: Double?,
    val gyroscopeMagnitudeRange: Double?,
    val pressureRangeHpa: Double?,
    val hasGpsData: Boolean,
    val hasAccelerometerData: Boolean,
    val hasGyroscopeData: Boolean,
    val hasPressureData: Boolean,
    val hasGpsGapOver60Seconds: Boolean
)

object TransportFeatureExtractor {
    private const val STOP_SPEED_THRESHOLD_KMH = 2.0
    private const val SECONDS_PER_MINUTE = 60.0
    private const val GAP_THRESHOLD_SECONDS = 60.0

    fun extract(window: ObservationWindow): TransportFeatures {
        val points = window.locationPoints
        val segments = window.segments
        val reportedSpeeds = points.map { it.speedKmh }.filter(Double::isFinite)
        val calculatedSpeeds = segments.map { it.averageSpeedKmh }.filter(Double::isFinite)
        val accuracyValues = points.map { it.accuracyMeters }.filter { it.isFinite() && it > 0.0 }
        val altitudeValues = points.map { it.altitudeMeters }
            .filter { it.isFinite() && it != 0.0 }
        val headingValues = points.map { it.headingDegrees }
            .filter { it.isFinite() && it != 0.0 }
        val headingChanges = headingValues.zipWithNext { first, second ->
            normalizedHeadingChange(first, second)
        }
        val durationMinutes = window.endTimestampMillis
            .minus(window.startTimestampMillis)
            .toDouble() / 1_000.0 / SECONDS_PER_MINUTE
        val accelerometerMagnitudes = magnitudes(window, SensorKind.ACCELEROMETER)
        val gyroscopeMagnitudes = magnitudes(window, SensorKind.GYROSCOPE)
        val pressureValues = window.sensorSamples
            .filter { it.sensorType == SensorKind.PRESSURE.type }
            .mapNotNull { it.pressureHpa?.toDouble() }
            .filter(Double::isFinite)

        return TransportFeatures(
            observationDurationSeconds = (window.endTimestampMillis -
                window.startTimestampMillis).toDouble() / 1_000.0,
            gpsPointCount = window.gpsPointCount,
            validSegmentCount = window.validSegmentCount,
            totalDistanceMeters = segments.sumOf { it.distanceMeters },
            medianReportedSpeedKmh = reportedSpeeds.medianOrNull(),
            maxReportedSpeedKmh = reportedSpeeds.maxOrNull(),
            medianCalculatedSegmentSpeedKmh = calculatedSpeeds.medianOrNull(),
            maxCalculatedSegmentSpeedKmh = calculatedSpeeds.maxOrNull(),
            speedStandardDeviationKmh = reportedSpeeds.standardDeviationOrNull(),
            stopRatio = points.takeIf { it.isNotEmpty() }
                ?.count { it.speedKmh <= STOP_SPEED_THRESHOLD_KMH }
                ?.toDouble()
                ?.div(points.size),
            headingChangeRateDegreesPerMinute =
                if (headingChanges.isNotEmpty() && durationMinutes > 0.0) {
                    headingChanges.sum() / durationMinutes
                } else {
                    null
                },
            totalHeadingChangeDegrees = headingChanges.takeIf { it.isNotEmpty() }?.sum(),
            altitudeRangeMeters = altitudeValues.rangeOrNull(),
            largestGpsGapSeconds = window.largestGpsGapMillis?.toDouble()?.div(1_000.0),
            minAccuracyMeters = accuracyValues.minOrNull(),
            averageAccuracyMeters = accuracyValues.takeIf { it.isNotEmpty() }?.average(),
            maxAccuracyMeters = accuracyValues.maxOrNull(),
            medianSegmentDurationSeconds = segments.map { it.durationSeconds }
                .filter(Double::isFinite).medianOrNull(),
            medianSegmentDistanceMeters = segments.map { it.distanceMeters }
                .filter(Double::isFinite).medianOrNull(),
            accelerometerSampleCount = accelerometerMagnitudes.size,
            gyroscopeSampleCount = gyroscopeMagnitudes.size,
            pressureSampleCount = pressureValues.size,
            accelerometerMagnitudeMean = accelerometerMagnitudes.meanOrNull(),
            accelerometerMagnitudeStdDev = accelerometerMagnitudes.standardDeviationOrNull(),
            accelerometerMagnitudeRange = accelerometerMagnitudes.rangeOrNull(),
            gyroscopeMagnitudeMean = gyroscopeMagnitudes.meanOrNull(),
            gyroscopeMagnitudeStdDev = gyroscopeMagnitudes.standardDeviationOrNull(),
            gyroscopeMagnitudeRange = gyroscopeMagnitudes.rangeOrNull(),
            pressureRangeHpa = pressureValues.rangeOrNull(),
            hasGpsData = points.isNotEmpty(),
            hasAccelerometerData = accelerometerMagnitudes.isNotEmpty(),
            hasGyroscopeData = gyroscopeMagnitudes.isNotEmpty(),
            hasPressureData = pressureValues.isNotEmpty(),
            hasGpsGapOver60Seconds =
                (window.largestGpsGapMillis ?: 0L) > GAP_THRESHOLD_SECONDS * 1_000.0
        )
    }

    private enum class SensorKind(val type: com.exposuretrackingsystem.app.data.model.SensorSampleType) {
        ACCELEROMETER(com.exposuretrackingsystem.app.data.model.SensorSampleType.ACCELEROMETER),
        GYROSCOPE(com.exposuretrackingsystem.app.data.model.SensorSampleType.GYROSCOPE),
        PRESSURE(com.exposuretrackingsystem.app.data.model.SensorSampleType.PRESSURE)
    }

    private fun magnitudes(window: ObservationWindow, kind: SensorKind): List<Double> =
        window.sensorSamples
            .asSequence()
            .filter { it.sensorType == kind.type }
            .mapNotNull { it.magnitudeOrNull() }
            .toList()

    private fun SensorSample.magnitudeOrNull(): Double? {
        val x = x?.toDouble() ?: return null
        val y = y?.toDouble() ?: return null
        val z = z?.toDouble() ?: return null
        return sqrt(x * x + y * y + z * z).takeIf(Double::isFinite)
    }

    private fun normalizedHeadingChange(first: Double, second: Double): Double {
        val delta = abs(second - first) % 360.0
        return minOf(delta, 360.0 - delta)
    }
}

private fun List<Double>.medianOrNull(): Double? {
    if (isEmpty()) return null
    val sorted = sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 0) {
        (sorted[middle - 1] + sorted[middle]) / 2.0
    } else {
        sorted[middle]
    }
}

private fun List<Double>.meanOrNull(): Double? = takeIf { it.isNotEmpty() }?.average()

private fun List<Double>.standardDeviationOrNull(): Double? {
    if (isEmpty()) return null
    val mean = average()
    return sqrt(sumOf { value -> (value - mean) * (value - mean) } / size)
}

private fun List<Double>.rangeOrNull(): Double? =
    if (isEmpty()) null else (maxOrNull()!! - minOrNull()!!)
