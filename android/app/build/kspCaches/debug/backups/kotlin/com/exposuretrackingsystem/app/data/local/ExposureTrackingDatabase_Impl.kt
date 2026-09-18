package com.exposuretrackingsystem.app.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ExposureTrackingDatabase_Impl : ExposureTrackingDatabase() {
  private val _routeDao: Lazy<RouteDao> = lazy {
    RouteDao_Impl(this)
  }

  private val _locationPointDao: Lazy<LocationPointDao> = lazy {
    LocationPointDao_Impl(this)
  }

  private val _routeSegmentDao: Lazy<RouteSegmentDao> = lazy {
    RouteSegmentDao_Impl(this)
  }

  private val _activityDao: Lazy<ActivityDao> = lazy {
    ActivityDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "8f33cb1f52c2bec8feced1245baa9b1b", "c77bd7cc5876cdfce622b46b2eac1627") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `routes` (`id` INTEGER NOT NULL, `startedAtMillis` INTEGER NOT NULL, `endedAtMillis` INTEGER, `transportType` TEXT NOT NULL, `totalDistanceMeters` REAL NOT NULL, `totalDurationSeconds` REAL NOT NULL, `averageSpeedKmh` REAL NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `location_points` (`id` INTEGER NOT NULL, `routeId` INTEGER, `timestampMillis` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `altitudeMeters` REAL NOT NULL, `headingDegrees` REAL NOT NULL, `accuracyMeters` REAL NOT NULL, `speedKmh` REAL NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `route_segments` (`id` INTEGER NOT NULL, `routeId` INTEGER NOT NULL, `startLocationPointId` INTEGER NOT NULL, `endLocationPointId` INTEGER NOT NULL, `activityId` INTEGER, `startTimeMillis` INTEGER NOT NULL, `endTimeMillis` INTEGER NOT NULL, `distanceMeters` REAL NOT NULL, `durationSeconds` REAL NOT NULL, `averageSpeedKmh` REAL NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `activities` (`id` INTEGER NOT NULL, `routeSegmentId` INTEGER, `activityLevel` TEXT NOT NULL, `transportType` TEXT NOT NULL, `startedAtMillis` INTEGER NOT NULL, `endedAtMillis` INTEGER, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8f33cb1f52c2bec8feced1245baa9b1b')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `routes`")
        connection.execSQL("DROP TABLE IF EXISTS `location_points`")
        connection.execSQL("DROP TABLE IF EXISTS `route_segments`")
        connection.execSQL("DROP TABLE IF EXISTS `activities`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsRoutes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRoutes.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutes.put("startedAtMillis", TableInfo.Column("startedAtMillis", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutes.put("endedAtMillis", TableInfo.Column("endedAtMillis", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutes.put("transportType", TableInfo.Column("transportType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutes.put("totalDistanceMeters", TableInfo.Column("totalDistanceMeters", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutes.put("totalDurationSeconds", TableInfo.Column("totalDurationSeconds", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutes.put("averageSpeedKmh", TableInfo.Column("averageSpeedKmh", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRoutes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRoutes: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoRoutes: TableInfo = TableInfo("routes", _columnsRoutes, _foreignKeysRoutes,
            _indicesRoutes)
        val _existingRoutes: TableInfo = read(connection, "routes")
        if (!_infoRoutes.equals(_existingRoutes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |routes(com.exposuretrackingsystem.app.data.local.entity.RouteEntity).
              | Expected:
              |""".trimMargin() + _infoRoutes + """
              |
              | Found:
              |""".trimMargin() + _existingRoutes)
        }
        val _columnsLocationPoints: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLocationPoints.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationPoints.put("routeId", TableInfo.Column("routeId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationPoints.put("timestampMillis", TableInfo.Column("timestampMillis", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationPoints.put("latitude", TableInfo.Column("latitude", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationPoints.put("longitude", TableInfo.Column("longitude", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationPoints.put("altitudeMeters", TableInfo.Column("altitudeMeters", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationPoints.put("headingDegrees", TableInfo.Column("headingDegrees", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationPoints.put("accuracyMeters", TableInfo.Column("accuracyMeters", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationPoints.put("speedKmh", TableInfo.Column("speedKmh", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLocationPoints: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLocationPoints: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoLocationPoints: TableInfo = TableInfo("location_points", _columnsLocationPoints,
            _foreignKeysLocationPoints, _indicesLocationPoints)
        val _existingLocationPoints: TableInfo = read(connection, "location_points")
        if (!_infoLocationPoints.equals(_existingLocationPoints)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |location_points(com.exposuretrackingsystem.app.data.local.entity.LocationPointEntity).
              | Expected:
              |""".trimMargin() + _infoLocationPoints + """
              |
              | Found:
              |""".trimMargin() + _existingLocationPoints)
        }
        val _columnsRouteSegments: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRouteSegments.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("routeId", TableInfo.Column("routeId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("startLocationPointId", TableInfo.Column("startLocationPointId",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("endLocationPointId", TableInfo.Column("endLocationPointId",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("activityId", TableInfo.Column("activityId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("startTimeMillis", TableInfo.Column("startTimeMillis", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("endTimeMillis", TableInfo.Column("endTimeMillis", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("distanceMeters", TableInfo.Column("distanceMeters", "REAL", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("durationSeconds", TableInfo.Column("durationSeconds", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRouteSegments.put("averageSpeedKmh", TableInfo.Column("averageSpeedKmh", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRouteSegments: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRouteSegments: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoRouteSegments: TableInfo = TableInfo("route_segments", _columnsRouteSegments,
            _foreignKeysRouteSegments, _indicesRouteSegments)
        val _existingRouteSegments: TableInfo = read(connection, "route_segments")
        if (!_infoRouteSegments.equals(_existingRouteSegments)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |route_segments(com.exposuretrackingsystem.app.data.local.entity.RouteSegmentEntity).
              | Expected:
              |""".trimMargin() + _infoRouteSegments + """
              |
              | Found:
              |""".trimMargin() + _existingRouteSegments)
        }
        val _columnsActivities: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsActivities.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActivities.put("routeSegmentId", TableInfo.Column("routeSegmentId", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivities.put("activityLevel", TableInfo.Column("activityLevel", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivities.put("transportType", TableInfo.Column("transportType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivities.put("startedAtMillis", TableInfo.Column("startedAtMillis", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivities.put("endedAtMillis", TableInfo.Column("endedAtMillis", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysActivities: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesActivities: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoActivities: TableInfo = TableInfo("activities", _columnsActivities,
            _foreignKeysActivities, _indicesActivities)
        val _existingActivities: TableInfo = read(connection, "activities")
        if (!_infoActivities.equals(_existingActivities)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |activities(com.exposuretrackingsystem.app.data.local.entity.ActivityEntity).
              | Expected:
              |""".trimMargin() + _infoActivities + """
              |
              | Found:
              |""".trimMargin() + _existingActivities)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "routes", "location_points",
        "route_segments", "activities")
  }

  public override fun clearAllTables() {
    super.performClear(false, "routes", "location_points", "route_segments", "activities")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(RouteDao::class, RouteDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(LocationPointDao::class, LocationPointDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RouteSegmentDao::class, RouteSegmentDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ActivityDao::class, ActivityDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun routeDao(): RouteDao = _routeDao.value

  public override fun locationPointDao(): LocationPointDao = _locationPointDao.value

  public override fun routeSegmentDao(): RouteSegmentDao = _routeSegmentDao.value

  public override fun activityDao(): ActivityDao = _activityDao.value
}
