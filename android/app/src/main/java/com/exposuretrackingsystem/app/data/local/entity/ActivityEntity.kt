package com.exposuretrackingsystem.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exposuretrackingsystem.app.data.model.ActivityLevel
import com.exposuretrackingsystem.app.data.model.TransportType

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: Long,
    val routeSegmentId: Long?,
    val activityLevel: ActivityLevel,
    val transportType: TransportType,
    val startedAtMillis: Long,
    val endedAtMillis: Long?
)
