package com.exposuretrackingsystem.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.exposuretrackingsystem.app.data.local.entity.LocationPointEntity

@Dao
interface LocationPointDao {
    @Insert
    fun insertAll(points: List<LocationPointEntity>)

    @Query("SELECT * FROM location_points WHERE routeId = :routeId ORDER BY timestampMillis")
    fun forRoute(routeId: Long): List<LocationPointEntity>
}
