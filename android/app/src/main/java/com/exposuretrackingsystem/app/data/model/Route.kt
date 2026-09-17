package com.exposuretrackingsystem.app.data.model

data class Route(
    val id: Long = ModelIdGenerator.next(),
    val startedAtMillis: Long,
    var endedAtMillis: Long? = null,
    val locationPoints: MutableList<LocationPoint> = mutableListOf(),
    val segments: MutableList<RouteSegment> = mutableListOf(),
    var totalDistanceMeters: Double = 0.0,
    var totalDurationSeconds: Double = 0.0,
    var averageSpeedKmh: Double = 0.0
)
