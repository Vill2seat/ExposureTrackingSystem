package com.exposuretrackingsystem.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_segments")
data class RouteSegmentEntity(
    @PrimaryKey val id: Long,
    val routeId: Long,
    val startLocationPointId: Long,
    val endLocationPointId: Long,
    val activityId: Long?,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val averageSpeedKmh: Double
)
