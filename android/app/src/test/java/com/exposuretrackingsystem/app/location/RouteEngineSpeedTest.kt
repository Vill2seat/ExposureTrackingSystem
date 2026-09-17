package com.exposuretrackingsystem.app.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.exposuretrackingsystem.app.data.model.SensorSample
import com.exposuretrackingsystem.app.data.model.SensorSampleType

class RouteEngineSpeedTest {
    @Test
    fun calculatesWalkingSpeedFromMetersAndSeconds() {
        val speedKmh = calculateSpeedKmh(4.7106, 6.768)

        assertEquals(2.505, speedKmh!!, 0.01)
    }

    @Test
    fun acceptsFiveSecondRoutePointGap() {
        assertTrue(isValidRoutePointGap(5.0))
    }

    @Test
    fun acceptsExactlySixtySecondRoutePointGap() {
        assertTrue(isValidRoutePointGap(RouteEngine.MAX_ROUTE_POINT_GAP_SECONDS))
    }

    @Test
    fun rejectsSixtyOneSecondRoutePointGap() {
        assertFalse(isValidRoutePointGap(61.0))
    }

    @Test
    fun rejectsFourMinuteRoutePointGap() {
        assertFalse(isValidRoutePointGap(240.0))
    }

    @Test
    fun pointAfterLongGapCanStartNextValidSegment() {
        assertFalse(isValidRoutePointGap(240.0))
        assertTrue(isValidRoutePointGap(5.0))
    }

    @Test
    fun rejectsStationaryMotionWithFalseGpsSpeed() {
        val samples = stationaryAccelerometerSamples()

        assertTrue(shouldRejectStationaryMotion(5.0, samples))
    }

    @Test
    fun doesNotRejectGenuineMovementSignal() {
        val samples = stationaryAccelerometerSamples() + SensorSample(
            timestampMillis = 6_000L,
            sensorType = SensorSampleType.ACCELEROMETER,
            x = 4.0f,
            y = 0.0f,
            z = 9.81f
        )

        assertFalse(shouldRejectStationaryMotion(5.0, samples))
    }

    @Test
    fun preservesGpsBehaviorWithoutAccelerometerData() {
        assertFalse(shouldRejectStationaryMotion(5.0, emptyList()))
    }

    private fun stationaryAccelerometerSamples(): List<SensorSample> {
        return (1L..5L).map { timestamp ->
            SensorSample(
                timestampMillis = timestamp * 1_000L,
                sensorType = SensorSampleType.ACCELEROMETER,
                x = 0.0f,
                y = 0.0f,
                z = 9.81f
            )
        }
    }
}