package com.exposuretrackingsystem.app.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.sqlite.SQLiteStatement
import com.exposuretrackingsystem.app.`data`.local.entity.LocationPointEntity
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
public class LocationPointDao_Impl(
  __db: RoomDatabase,
) : LocationPointDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLocationPointEntity: EntityInsertAdapter<LocationPointEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfLocationPointEntity = object : EntityInsertAdapter<LocationPointEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `location_points` (`id`,`routeId`,`timestampMillis`,`latitude`,`longitude`,`altitudeMeters`,`headingDegrees`,`accuracyMeters`,`speedKmh`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LocationPointEntity) {
        statement.bindLong(1, entity.id)
        val _tmpRouteId: Long? = entity.routeId
        if (_tmpRouteId == null) {
          statement.bindNull(2)
        } else {
          statement.bindLong(2, _tmpRouteId)
        }
        statement.bindLong(3, entity.timestampMillis)
        statement.bindDouble(4, entity.latitude)
        statement.bindDouble(5, entity.longitude)
        statement.bindDouble(6, entity.altitudeMeters)
        statement.bindDouble(7, entity.headingDegrees)
        statement.bindDouble(8, entity.accuracyMeters)
        statement.bindDouble(9, entity.speedKmh)
      }
    }
  }

  public override fun insertAll(points: List<LocationPointEntity>): Unit = performBlocking(__db,
      false, true) { _connection ->
    __insertAdapterOfLocationPointEntity.insert(_connection, points)
  }

  public override fun forRoute(routeId: Long): List<LocationPointEntity> {
    val _sql: String = "SELECT * FROM location_points WHERE routeId = ? ORDER BY timestampMillis"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, routeId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRouteId: Int = getColumnIndexOrThrow(_stmt, "routeId")
        val _columnIndexOfTimestampMillis: Int = getColumnIndexOrThrow(_stmt, "timestampMillis")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfAltitudeMeters: Int = getColumnIndexOrThrow(_stmt, "altitudeMeters")
        val _columnIndexOfHeadingDegrees: Int = getColumnIndexOrThrow(_stmt, "headingDegrees")
        val _columnIndexOfAccuracyMeters: Int = getColumnIndexOrThrow(_stmt, "accuracyMeters")
        val _columnIndexOfSpeedKmh: Int = getColumnIndexOrThrow(_stmt, "speedKmh")
        val _result: MutableList<LocationPointEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LocationPointEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRouteId: Long?
          if (_stmt.isNull(_columnIndexOfRouteId)) {
            _tmpRouteId = null
          } else {
            _tmpRouteId = _stmt.getLong(_columnIndexOfRouteId)
          }
          val _tmpTimestampMillis: Long
          _tmpTimestampMillis = _stmt.getLong(_columnIndexOfTimestampMillis)
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpAltitudeMeters: Double
          _tmpAltitudeMeters = _stmt.getDouble(_columnIndexOfAltitudeMeters)
          val _tmpHeadingDegrees: Double
          _tmpHeadingDegrees = _stmt.getDouble(_columnIndexOfHeadingDegrees)
          val _tmpAccuracyMeters: Double
          _tmpAccuracyMeters = _stmt.getDouble(_columnIndexOfAccuracyMeters)
          val _tmpSpeedKmh: Double
          _tmpSpeedKmh = _stmt.getDouble(_columnIndexOfSpeedKmh)
          _item =
              LocationPointEntity(_tmpId,_tmpRouteId,_tmpTimestampMillis,_tmpLatitude,_tmpLongitude,_tmpAltitudeMeters,_tmpHeadingDegrees,_tmpAccuracyMeters,_tmpSpeedKmh)
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
