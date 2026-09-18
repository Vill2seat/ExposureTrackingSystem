package com.exposuretrackingsystem.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.exposuretrackingsystem.app.data.local.entity.RouteSegmentEntity

@Dao
interface RouteSegmentDao {
    @Insert
    fun insertAll(segments: List<RouteSegmentEntity>)

    @Query("SELECT * FROM route_segments WHERE routeId = :routeId ORDER BY startTimeMillis")
    fun forRoute(routeId: Long): List<RouteSegmentEntity>
}
