package com.exposuretrackingsystem.app.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.sqlite.SQLiteStatement
import com.exposuretrackingsystem.app.`data`.local.entity.RouteEntity
import com.exposuretrackingsystem.app.`data`.model.TransportType
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
public class RouteDao_Impl(
  __db: RoomDatabase,
) : RouteDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRouteEntity: EntityInsertAdapter<RouteEntity>

  private val __routeTypeConverters: RouteTypeConverters = RouteTypeConverters()
  init {
    this.__db = __db
    this.__insertAdapterOfRouteEntity = object : EntityInsertAdapter<RouteEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `routes` (`id`,`startedAtMillis`,`endedAtMillis`,`transportType`,`totalDistanceMeters`,`totalDurationSeconds`,`averageSpeedKmh`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RouteEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.startedAtMillis)
        val _tmpEndedAtMillis: Long? = entity.endedAtMillis
        if (_tmpEndedAtMillis == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpEndedAtMillis)
        }
        val _tmp: String = __routeTypeConverters.fromTransportType(entity.transportType)
        statement.bindText(4, _tmp)
        statement.bindDouble(5, entity.totalDistanceMeters)
        statement.bindDouble(6, entity.totalDurationSeconds)
        statement.bindDouble(7, entity.averageSpeedKmh)
      }
    }
  }

  public override fun insert(route: RouteEntity): Unit = performBlocking(__db, false, true) {
      _connection ->
    __insertAdapterOfRouteEntity.insert(_connection, route)
  }

  public override fun findById(routeId: Long): RouteEntity? {
    val _sql: String = "SELECT * FROM routes WHERE id = ? LIMIT 1"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, routeId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartedAtMillis: Int = getColumnIndexOrThrow(_stmt, "startedAtMillis")
        val _columnIndexOfEndedAtMillis: Int = getColumnIndexOrThrow(_stmt, "endedAtMillis")
        val _columnIndexOfTransportType: Int = getColumnIndexOrThrow(_stmt, "transportType")
        val _columnIndexOfTotalDistanceMeters: Int = getColumnIndexOrThrow(_stmt,
            "totalDistanceMeters")
        val _columnIndexOfTotalDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "totalDurationSeconds")
        val _columnIndexOfAverageSpeedKmh: Int = getColumnIndexOrThrow(_stmt, "averageSpeedKmh")
        val _result: RouteEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartedAtMillis: Long
          _tmpStartedAtMillis = _stmt.getLong(_columnIndexOfStartedAtMillis)
          val _tmpEndedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndedAtMillis)) {
            _tmpEndedAtMillis = null
          } else {
            _tmpEndedAtMillis = _stmt.getLong(_columnIndexOfEndedAtMillis)
          }
          val _tmpTransportType: TransportType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfTransportType)
          _tmpTransportType = __routeTypeConverters.toTransportType(_tmp)
          val _tmpTotalDistanceMeters: Double
          _tmpTotalDistanceMeters = _stmt.getDouble(_columnIndexOfTotalDistanceMeters)
          val _tmpTotalDurationSeconds: Double
          _tmpTotalDurationSeconds = _stmt.getDouble(_columnIndexOfTotalDurationSeconds)
          val _tmpAverageSpeedKmh: Double
          _tmpAverageSpeedKmh = _stmt.getDouble(_columnIndexOfAverageSpeedKmh)
          _result =
              RouteEntity(_tmpId,_tmpStartedAtMillis,_tmpEndedAtMillis,_tmpTransportType,_tmpTotalDistanceMeters,_tmpTotalDurationSeconds,_tmpAverageSpeedKmh)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAll(): List<RouteEntity> {
    val _sql: String = "SELECT * FROM routes ORDER BY startedAtMillis DESC"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartedAtMillis: Int = getColumnIndexOrThrow(_stmt, "startedAtMillis")
        val _columnIndexOfEndedAtMillis: Int = getColumnIndexOrThrow(_stmt, "endedAtMillis")
        val _columnIndexOfTransportType: Int = getColumnIndexOrThrow(_stmt, "transportType")
        val _columnIndexOfTotalDistanceMeters: Int = getColumnIndexOrThrow(_stmt,
            "totalDistanceMeters")
        val _columnIndexOfTotalDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "totalDurationSeconds")
        val _columnIndexOfAverageSpeedKmh: Int = getColumnIndexOrThrow(_stmt, "averageSpeedKmh")
        val _result: MutableList<RouteEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RouteEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartedAtMillis: Long
          _tmpStartedAtMillis = _stmt.getLong(_columnIndexOfStartedAtMillis)
          val _tmpEndedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndedAtMillis)) {
            _tmpEndedAtMillis = null
          } else {
            _tmpEndedAtMillis = _stmt.getLong(_columnIndexOfEndedAtMillis)
          }
          val _tmpTransportType: TransportType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfTransportType)
          _tmpTransportType = __routeTypeConverters.toTransportType(_tmp)
          val _tmpTotalDistanceMeters: Double
          _tmpTotalDistanceMeters = _stmt.getDouble(_columnIndexOfTotalDistanceMeters)
          val _tmpTotalDurationSeconds: Double
          _tmpTotalDurationSeconds = _stmt.getDouble(_columnIndexOfTotalDurationSeconds)
          val _tmpAverageSpeedKmh: Double
          _tmpAverageSpeedKmh = _stmt.getDouble(_columnIndexOfAverageSpeedKmh)
          _item =
              RouteEntity(_tmpId,_tmpStartedAtMillis,_tmpEndedAtMillis,_tmpTransportType,_tmpTotalDistanceMeters,_tmpTotalDurationSeconds,_tmpAverageSpeedKmh)
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
