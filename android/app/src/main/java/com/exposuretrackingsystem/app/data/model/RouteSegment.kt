package com.exposuretrackingsystem.app.data.model

data class RouteSegment(
    val id: Long = ModelIdGenerator.next(),
    val routeId: Long,
    val startLocationPointId: Long,
    val endLocationPointId: Long,
    val activityId: Long? = null,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val averageSpeedKmh: Double
)
