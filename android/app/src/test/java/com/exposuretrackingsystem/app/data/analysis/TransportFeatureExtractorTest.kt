package com.exposuretrackingsystem.app.data.analysis

import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.exposuretrackingsystem.app.data.model.Route
import com.exposuretrackingsystem.app.data.model.RouteSegment
import com.exposuretrackingsystem.app.data.model.SensorSample
import com.exposuretrackingsystem.app.data.model.SensorSampleType
import com.exposuretrackingsystem.app.data.model.TransportType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class TransportFeatureExtractorTest {
    @Test
    fun extractsDeterministicGpsAndSegmentFeatures() {
        val features = TransportFeatureExtractor.extract(windowWithAllData())

        assertEquals(120.0, features.observationDurationSeconds, 0.0)
        assertEquals(3, features.gpsPointCount)
        assertEquals(2, features.validSegmentCount)
        assertEquals(30.0, features.totalDistanceMeters, 0.0)
        assertEquals(3.0, features.medianReportedSpeedKmh!!, 0.0)
        assertEquals(6.0, features.maxReportedSpeedKmh!!, 0.0)
        assertEquals(18.0, features.medianCalculatedSegmentSpeedKmh!!, 0.0)
        assertEquals(24.0, features.maxCalculatedSegmentSpeedKmh!!, 0.0)
        assertEquals(sqrt(6.0), features.speedStandardDeviationKmh!!, 0.0001)
        assertEquals(1.0 / 3.0, features.stopRatio!!, 0.0)
        assertEquals(25.0, features.headingChangeRateDegreesPerMinute!!, 0.0)
        assertEquals(50.0, features.totalHeadingChangeDegrees!!, 0.0)
        assertEquals(20.0, features.altitudeRangeMeters!!, 0.0)
        assertEquals(5.0, features.largestGpsGapSeconds!!, 0.0)
        assertEquals(5.0, features.minAccuracyMeters!!, 0.0)
        assertEquals(10.0, features.averageAccuracyMeters!!, 0.0)
        assertEquals(15.0, features.maxAccuracyMeters!!, 0.0)
        assertEquals(5.0, features.medianSegmentDurationSeconds!!, 0.0)
        assertEquals(15.0, features.medianSegmentDistanceMeters!!, 0.0)
    }

    @Test
    fun extractsSensorMagnitudeAndPressureFeatures() {
        val features = TransportFeatureExtractor.extract(windowWithAllData())

        assertEquals(2, features.accelerometerSampleCount)
        assertEquals(2, features.gyroscopeSampleCount)
        assertEquals(2, features.pressureSampleCount)
        assertEquals(3.5, features.accelerometerMagnitudeMean!!, 0.0)
        assertEquals(1.5, features.accelerometerMagnitudeStdDev!!, 0.0)
        assertEquals(3.0, features.accelerometerMagnitudeRange!!, 0.0)
        assertEquals(3.5, features.gyroscopeMagnitudeMean!!, 0.0)
        assertEquals(0.5, features.gyroscopeMagnitudeStdDev!!, 0.0)
        assertEquals(1.0, features.gyroscopeMagnitudeRange!!, 0.0)
        assertEquals(5.0, features.pressureRangeHpa!!, 0.0)
    }

    @Test
    fun emptyWindowProducesSafeNullableFeatures() {
        val features = TransportFeatureExtractor.extract(
            ObservationWindow.from(emptyRoute(), 1_000L, 2_000L)
        )

        assertEquals(0, features.gpsPointCount)
        assertEquals(0, features.validSegmentCount)
        assertEquals(0.0, features.totalDistanceMeters, 0.0)
        assertNull(features.medianReportedSpeedKmh)
        assertNull(features.stopRatio)
        assertNull(features.totalHeadingChangeDegrees)
        assertNull(features.altitudeRangeMeters)
        assertNull(features.largestGpsGapSeconds)
        assertEquals(0, features.accelerometerSampleCount)
        assertNull(features.accelerometerMagnitudeMean)
        assertFalse(features.hasGpsData)
        assertFalse(features.hasPressureData)
        assertFalse(features.hasGpsGapOver60Seconds)
    }

    @Test
    fun missingSensorValuesRemainNullable() {
        val window = ObservationWindow.from(
            emptyRoute(),
            1_000L,
            2_000L,
            sensorSamples = listOf(
                SensorSample(1_500L, SensorSampleType.ACCELEROMETER),
                SensorSample(1_500L, SensorSampleType.PRESSURE),
                SensorSample(1_500L, SensorSampleType.GYROSCOPE)
            )
        )

        val features = TransportFeatureExtractor.extract(window)

        assertEquals(0, features.accelerometerSampleCount)
        assertEquals(0, features.gyroscopeSampleCount)
        assertEquals(0, features.pressureSampleCount)
        assertNull(features.accelerometerMagnitudeMean)
        assertNull(features.gyroscopeMagnitudeMean)
        assertNull(features.pressureRangeHpa)
    }

    @Test
    fun reportsGpsGapOverSixtySecondsWithoutFilteringPoints() {
        val route = Route(
            id = 1L,
            startedAtMillis = 0L,
            transportType = TransportType.WALK,
            locationPoints = mutableListOf(point(0L, 0.0, 5.0), point(61_000L, 0.0, 5.0))
        )

        val features = TransportFeatureExtractor.extract(
            ObservationWindow.from(route, 0L, 61_000L)
        )

        assertEquals(2, features.gpsPointCount)
        assertEquals(61.0, features.largestGpsGapSeconds!!, 0.0)
        assertTrue(features.hasGpsGapOver60Seconds)
    }

    private fun windowWithAllData(): ObservationWindow {
        val route = Route(
            id = 1L,
            startedAtMillis = 0L,
            transportType = TransportType.WALK,
            locationPoints = mutableListOf(
                point(0L, 350.0, 0.0, 100.0, 5.0),
                point(5_000L, 10.0, 3.0, 110.0, 10.0),
                point(10_000L, 40.0, 6.0, 120.0, 15.0)
            ),
            segments = mutableListOf(
                segment(0L, 5_000L, 10.0, 12.0),
                segment(5_000L, 10_000L, 20.0, 24.0)
            )
        )
        return ObservationWindow.from(
            route,
            0L,
            120_000L,
            sensorSamples = listOf(
                SensorSample(1_000L, SensorSampleType.ACCELEROMETER, 3.0f, 4.0f, 0.0f),
                SensorSample(2_000L, SensorSampleType.ACCELEROMETER, 0.0f, 0.0f, 2.0f),
                SensorSample(3_000L, SensorSampleType.GYROSCOPE, 1.0f, 2.0f, 2.0f),
                SensorSample(4_000L, SensorSampleType.GYROSCOPE, 0.0f, 0.0f, 4.0f),
                SensorSample(5_000L, SensorSampleType.PRESSURE, pressureHpa = 1_000.0f),
                SensorSample(6_000L, SensorSampleType.PRESSURE, pressureHpa = 1_005.0f)
            )
        )
    }

    private fun emptyRoute() = Route(
        id = 1L,
        startedAtMillis = 1_000L,
        transportType = TransportType.WALK
    )

    private fun point(
        timestamp: Long,
        heading: Double,
        speed: Double,
        altitude: Double = 100.0,
        accuracy: Double = 5.0
    ) = LocationPoint(
        id = timestamp,
        routeId = 1L,
        timestampMillis = timestamp,
        latitude = 50.0,
        longitude = 8.0,
        altitudeMeters = altitude,
        headingDegrees = heading,
        accuracyMeters = accuracy,
        speedKmh = speed
    )

    private fun segment(start: Long, end: Long, distance: Double, speed: Double) =
        RouteSegment(
            id = start,
            routeId = 1L,
            startLocationPointId = start,
            endLocationPointId = end,
            startTimeMillis = start,
            endTimeMillis = end,
            distanceMeters = distance,
            durationSeconds = (end - start) / 1_000.0,
            averageSpeedKmh = speed
        )
}
