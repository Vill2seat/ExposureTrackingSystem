package com.exposuretrackingsystem.app.location

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteEngineSpeedTest {
    @Test
    fun calculatesWalkingSpeedFromMetersAndSeconds() {
        val speedKmh = calculateSpeedKmh(4.7106, 6.768)

        assertEquals(2.505, speedKmh!!, 0.01)
    }
}