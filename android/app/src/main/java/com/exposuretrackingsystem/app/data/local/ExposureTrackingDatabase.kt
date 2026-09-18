package com.exposuretrackingsystem.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.exposuretrackingsystem.app.data.local.entity.ActivityEntity
import com.exposuretrackingsystem.app.data.local.entity.LocationPointEntity
import com.exposuretrackingsystem.app.data.local.entity.RouteEntity
import com.exposuretrackingsystem.app.data.local.entity.RouteSegmentEntity
import com.exposuretrackingsystem.app.data.model.ActivityLevel
import com.exposuretrackingsystem.app.data.model.TransportType

@Database(
    entities = [
        RouteEntity::class,
        LocationPointEntity::class,
        RouteSegmentEntity::class,
        ActivityEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RouteTypeConverters::class)
abstract class ExposureTrackingDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun locationPointDao(): LocationPointDao
    abstract fun routeSegmentDao(): RouteSegmentDao
    abstract fun activityDao(): ActivityDao
}

class RouteTypeConverters {
    @TypeConverter
    fun fromTransportType(value: TransportType): String = value.name

    @TypeConverter
    fun toTransportType(value: String): TransportType = TransportType.valueOf(value)

    @TypeConverter
    fun fromActivityLevel(value: ActivityLevel): String = value.name

    @TypeConverter
    fun toActivityLevel(value: String): ActivityLevel = ActivityLevel.valueOf(value)
}
