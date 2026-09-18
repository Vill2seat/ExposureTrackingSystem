package com.exposuretrackingsystem.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.exposuretrackingsystem.app.data.local.entity.RouteEntity

@Dao
interface RouteDao {
    @Insert
    fun insert(route: RouteEntity)

    @Query("SELECT * FROM routes WHERE id = :routeId LIMIT 1")
    fun findById(routeId: Long): RouteEntity?

    @Query("SELECT * FROM routes ORDER BY startedAtMillis DESC")
    fun observeAll(): List<RouteEntity>
}
