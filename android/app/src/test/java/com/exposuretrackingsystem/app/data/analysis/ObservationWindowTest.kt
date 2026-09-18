package com.exposuretrackingsystem.app.data.analysis

import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.exposuretrackingsystem.app.data.model.Route
import com.exposuretrackingsystem.app.data.model.RouteSegment
import com.exposuretrackingsystem.app.data.model.SensorSample
import com.exposuretrackingsystem.app.data.model.SensorSampleType
import com.exposuretrackingsystem.app.data.model.TransportType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationWindowTest {
    @Test
    fun includesGpsAndSensorsAtBothWindowBoundaries() {
        val route = route(
            points = listOf(point(1_000L, 5.0), point(2_000L, 6.0), point(3_000L, 7.0)),
            segments = listOf(segment(1_000L, 2_000L), segment(2_000L, 3_000L))
        )
        val sensors = listOf(
            sensor(999L, SensorSampleType.ACCELEROMETER),
            sensor(2_000L, SensorSampleType.GYROSCOPE),
            sensor(3_001L, SensorSampleType.PRESSURE)
        )

        val window = ObservationWindow.from(route, 2_000L, 3_000L, sensors)

        assertEquals(listOf(2_000L, 3_000L), window.locationPoints.map { it.timestampMillis })
        assertEquals(2, window.segments.size)
        assertEquals(listOf(2_000L), window.sensorSamples.map { it.timestampMillis })
    }

    @Test
    fun excludesOneGpsPointOutsideEachWindowBoundary() {
        val route = route(
            points = listOf(
                point(999L, 5.0),
                point(1_000L, 6.0),
                point(2_000L, 7.0),
                point(2_001L, 8.0)
            )
        )

        val window = ObservationWindow.from(route, 1_000L, 2_000L)

        assertEquals(listOf(1_000L, 2_000L), window.locationPoints.map { it.timestampMillis })
    }

    @Test
    fun selectsOnlySegmentsBeforeAfterAndCrossingWindow() {
        val route = route(
            segments = listOf(
                segment(100L, 900L),
                segment(1_000L, 1_500L),
                segment(1_500L, 2_500L),
                segment(2_500L, 3_000L),
                segment(3_001L, 4_000L)
            )
        )

        val window = ObservationWindow.from(route, 1_000L, 3_000L)

        assertEquals(
            listOf(1_000L, 1_500L, 2_500L),
            window.segments.map { it.startTimeMillis }
        )
    }

    @Test
    fun onePointWindowHasNoGpsGap() {
        val window = ObservationWindow.from(
            route(points = listOf(point(2_000L, 5.0))),
            2_000L,
            2_000L
        )

        assertEquals(1, window.gpsPointCount)
        assertNull(window.largestGpsGapMillis)
    }

    @Test
    fun emptyWindowHasNoDataOrAccuracyStatistics() {
        val window = ObservationWindow.from(route(), 10_000L, 11_000L)

        assertTrue(window.locationPoints.isEmpty())
        assertTrue(window.segments.isEmpty())
        assertTrue(window.sensorSamples.isEmpty())
        assertEquals(0, window.gpsPointCount)
        assertEquals(0, window.validSegmentCount)
        assertNull(window.largestGpsGapMillis)
        assertNull(window.averageAccuracyMeters)
    }

    @Test
    fun reportsLargestGapAndAccuracyStatistics() {
        val route = route(
            points = listOf(point(1_000L, 4.0), point(2_500L, 8.0), point(6_000L, 12.0))
        )

        val window = ObservationWindow.from(route, 1_000L, 6_000L)

        assertEquals(3_500L, window.largestGpsGapMillis)
        assertEquals(4.0, requireNotNull(window.minimumAccuracyMeters), 0.0)
        assertEquals(12.0, requireNotNull(window.maximumAccuracyMeters), 0.0)
        assertEquals(8.0, requireNotNull(window.averageAccuracyMeters), 0.0)
    }

    @Test
    fun excludesInfiniteAccuracyValues() {
        val route = route(
            points = listOf(
                point(1_000L, 5.0),
                point(2_000L, Double.POSITIVE_INFINITY),
                point(3_000L, Double.NEGATIVE_INFINITY)
            )
        )

        val window = ObservationWindow.from(route, 1_000L, 3_000L)

        assertEquals(5.0, requireNotNull(window.minimumAccuracyMeters), 0.0)
        assertEquals(5.0, requireNotNull(window.maximumAccuracyMeters), 0.0)
        assertEquals(5.0, requireNotNull(window.averageAccuracyMeters), 0.0)
    }

    @Test
    fun reportsSensorAvailabilityAndCounts() {
        val samples = listOf(
            sensor(1_000L, SensorSampleType.ACCELEROMETER),
            sensor(1_500L, SensorSampleType.ACCELEROMETER),
            sensor(2_000L, SensorSampleType.PRESSURE)
        )

        val window = ObservationWindow.from(
            route = route(),
            startTimestampMillis = 1_000L,
            endTimestampMillis = 2_000L,
            sensorSamples = samples,
            availableSensorTypes = setOf(SensorSampleType.ACCELEROMETER, SensorSampleType.PRESSURE)
        )

        assertEquals(
            mapOf(
                SensorSampleType.ACCELEROMETER to 2,
                SensorSampleType.GYROSCOPE to 0,
                SensorSampleType.PRESSURE to 1
            ),
            window.sensorSampleCounts
        )
        assertEquals(
            setOf(SensorSampleType.ACCELEROMETER, SensorSampleType.PRESSURE),
            window.availableSensorTypes
        )
    }

    @Test
    fun handlesZeroAndOneSensorSample() {
        val emptyWindow = ObservationWindow.from(route(), 1_000L, 2_000L)
        val oneSampleWindow = ObservationWindow.from(
            route(),
            1_000L,
            2_000L,
            sensorSamples = listOf(sensor(1_000L, SensorSampleType.GYROSCOPE))
        )

        assertTrue(emptyWindow.sensorSamples.isEmpty())
        assertEquals(1, oneSampleWindow.sensorSamples.size)
        assertEquals(1, oneSampleWindow.sensorSampleCounts[SensorSampleType.GYROSCOPE])
    }

    @Test
    fun copiesAvailableSensorTypesAtConstruction() {
        val availableSensorTypes = mutableSetOf(SensorSampleType.ACCELEROMETER)

        val window = ObservationWindow.from(
            route = route(),
            startTimestampMillis = 1_000L,
            endTimestampMillis = 2_000L,
            availableSensorTypes = availableSensorTypes
        )
        availableSensorTypes += SensorSampleType.PRESSURE

        assertEquals(setOf(SensorSampleType.ACCELEROMETER), window.availableSensorTypes)
    }

    @Test
    fun calculatesLargestGapFromUnsortedGpsInput() {
        val route = route(
            points = listOf(
                point(6_000L, 5.0),
                point(1_000L, 5.0),
                point(2_500L, 5.0)
            )
        )

        val window = ObservationWindow.from(route, 1_000L, 6_000L)

        assertEquals(3_500L, window.largestGpsGapMillis)
    }

    private fun route(
        points: List<LocationPoint> = emptyList(),
        segments: List<RouteSegment> = emptyList()
    ) = Route(
        id = 1L,
        startedAtMillis = 1_000L,
        transportType = TransportType.WALK,
        locationPoints = points.toMutableList(),
        segments = segments.toMutableList()
    )

    private fun point(timestamp: Long, accuracy: Double) = LocationPoint(
        id = timestamp,
        routeId = 1L,
        timestampMillis = timestamp,
        latitude = 50.0,
        longitude = 8.0,
        altitudeMeters = 100.0,
        headingDegrees = 90.0,
        accuracyMeters = accuracy,
        speedKmh = 5.0
    )

    private fun segment(start: Long, end: Long) = RouteSegment(
        id = start,
        routeId = 1L,
        startLocationPointId = start,
        endLocationPointId = end,
        startTimeMillis = start,
        endTimeMillis = end,
        distanceMeters = 10.0,
        durationSeconds = (end - start) / 1_000.0,
        averageSpeedKmh = 36.0
    )

    private fun sensor(timestamp: Long, type: SensorSampleType) = SensorSample(
        timestampMillis = timestamp,
        sensorType = type,
        x = 1.0f,
        y = 2.0f,
        z = 3.0f,
        pressureHpa = if (type == SensorSampleType.PRESSURE) 1_000.0f else null
    )
}
