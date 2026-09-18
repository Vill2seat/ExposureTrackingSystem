package com.exposuretrackingsystem.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_points")
data class LocationPointEntity(
    @PrimaryKey val id: Long,
    val routeId: Long?,
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val headingDegrees: Double,
    val accuracyMeters: Double,
    val speedKmh: Double
)
