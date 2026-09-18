package com.exposuretrackingsystem.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.exposuretrackingsystem.app.data.local.entity.ActivityEntity

@Dao
interface ActivityDao {
    @Insert
    fun insertAll(activities: List<ActivityEntity>)

    @Query("SELECT * FROM activities WHERE routeSegmentId IN (:segmentIds)")
    fun forSegments(segmentIds: List<Long>): List<ActivityEntity>
}
