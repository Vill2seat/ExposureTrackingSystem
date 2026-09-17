package com.exposuretrackingsystem.app.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}