package com.exposuretrackingsystem.app.data.model

data class Route(
    val id: Long = 0L,
    val startedAtMillis: Long,
    var endedAtMillis: Long? = null,
    val locationPoints: MutableList<LocationPoint> = mutableListOf(),
    val segments: MutableList<RouteSegment> = mutableListOf(),
    var totalDistanceMeters: Double = 0.0,
    var totalDurationSeconds: Long = 0L,
    var averageSpeedKmh: Double = 0.0
)
