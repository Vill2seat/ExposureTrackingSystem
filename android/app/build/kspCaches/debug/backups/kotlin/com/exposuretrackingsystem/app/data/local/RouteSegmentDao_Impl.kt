package com.exposuretrackingsystem.app.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.sqlite.SQLiteStatement
import com.exposuretrackingsystem.app.`data`.local.entity.RouteSegmentEntity
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class RouteSegmentDao_Impl(
  __db: RoomDatabase,
) : RouteSegmentDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRouteSegmentEntity: EntityInsertAdapter<RouteSegmentEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfRouteSegmentEntity = object : EntityInsertAdapter<RouteSegmentEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `route_segments` (`id`,`routeId`,`startLocationPointId`,`endLocationPointId`,`activityId`,`startTimeMillis`,`endTimeMillis`,`distanceMeters`,`durationSeconds`,`averageSpeedKmh`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RouteSegmentEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.routeId)
        statement.bindLong(3, entity.startLocationPointId)
        statement.bindLong(4, entity.endLocationPointId)
        val _tmpActivityId: Long? = entity.activityId
        if (_tmpActivityId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpActivityId)
        }
        statement.bindLong(6, entity.startTimeMillis)
        statement.bindLong(7, entity.endTimeMillis)
        statement.bindDouble(8, entity.distanceMeters)
        statement.bindDouble(9, entity.durationSeconds)
        statement.bindDouble(10, entity.averageSpeedKmh)
      }
    }
  }

  public override fun insertAll(segments: List<RouteSegmentEntity>): Unit = performBlocking(__db,
      false, true) { _connection ->
    __insertAdapterOfRouteSegmentEntity.insert(_connection, segments)
  }

  public override fun forRoute(routeId: Long): List<RouteSegmentEntity> {
    val _sql: String = "SELECT * FROM route_segments WHERE routeId = ? ORDER BY startTimeMillis"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, routeId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRouteId: Int = getColumnIndexOrThrow(_stmt, "routeId")
        val _columnIndexOfStartLocationPointId: Int = getColumnIndexOrThrow(_stmt,
            "startLocationPointId")
        val _columnIndexOfEndLocationPointId: Int = getColumnIndexOrThrow(_stmt,
            "endLocationPointId")
        val _columnIndexOfActivityId: Int = getColumnIndexOrThrow(_stmt, "activityId")
        val _columnIndexOfStartTimeMillis: Int = getColumnIndexOrThrow(_stmt, "startTimeMillis")
        val _columnIndexOfEndTimeMillis: Int = getColumnIndexOrThrow(_stmt, "endTimeMillis")
        val _columnIndexOfDistanceMeters: Int = getColumnIndexOrThrow(_stmt, "distanceMeters")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "durationSeconds")
        val _columnIndexOfAverageSpeedKmh: Int = getColumnIndexOrThrow(_stmt, "averageSpeedKmh")
        val _result: MutableList<RouteSegmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RouteSegmentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRouteId: Long
          _tmpRouteId = _stmt.getLong(_columnIndexOfRouteId)
          val _tmpStartLocationPointId: Long
          _tmpStartLocationPointId = _stmt.getLong(_columnIndexOfStartLocationPointId)
          val _tmpEndLocationPointId: Long
          _tmpEndLocationPointId = _stmt.getLong(_columnIndexOfEndLocationPointId)
          val _tmpActivityId: Long?
          if (_stmt.isNull(_columnIndexOfActivityId)) {
            _tmpActivityId = null
          } else {
            _tmpActivityId = _stmt.getLong(_columnIndexOfActivityId)
          }
          val _tmpStartTimeMillis: Long
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis)
          val _tmpEndTimeMillis: Long
          _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis)
          val _tmpDistanceMeters: Double
          _tmpDistanceMeters = _stmt.getDouble(_columnIndexOfDistanceMeters)
          val _tmpDurationSeconds: Double
          _tmpDurationSeconds = _stmt.getDouble(_columnIndexOfDurationSeconds)
          val _tmpAverageSpeedKmh: Double
          _tmpAverageSpeedKmh = _stmt.getDouble(_columnIndexOfAverageSpeedKmh)
          _item =
              RouteSegmentEntity(_tmpId,_tmpRouteId,_tmpStartLocationPointId,_tmpEndLocationPointId,_tmpActivityId,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpDistanceMeters,_tmpDurationSeconds,_tmpAverageSpeedKmh)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
