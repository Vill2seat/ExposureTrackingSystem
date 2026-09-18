package com.exposuretrackingsystem.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exposuretrackingsystem.app.data.model.TransportType

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
    val transportType: TransportType,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Double,
    val averageSpeedKmh: Double
)
