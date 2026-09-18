package com.exposuretrackingsystem.app.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.sqlite.SQLiteStatement
import com.exposuretrackingsystem.app.`data`.local.entity.ActivityEntity
import com.exposuretrackingsystem.app.`data`.model.ActivityLevel
import com.exposuretrackingsystem.app.`data`.model.TransportType
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ActivityDao_Impl(
  __db: RoomDatabase,
) : ActivityDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfActivityEntity: EntityInsertAdapter<ActivityEntity>

  private val __routeTypeConverters: RouteTypeConverters = RouteTypeConverters()
  init {
    this.__db = __db
    this.__insertAdapterOfActivityEntity = object : EntityInsertAdapter<ActivityEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `activities` (`id`,`routeSegmentId`,`activityLevel`,`transportType`,`startedAtMillis`,`endedAtMillis`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ActivityEntity) {
        statement.bindLong(1, entity.id)
        val _tmpRouteSegmentId: Long? = entity.routeSegmentId
        if (_tmpRouteSegmentId == null) {
          statement.bindNull(2)
        } else {
          statement.bindLong(2, _tmpRouteSegmentId)
        }
        val _tmp: String = __routeTypeConverters.fromActivityLevel(entity.activityLevel)
        statement.bindText(3, _tmp)
        val _tmp_1: String = __routeTypeConverters.fromTransportType(entity.transportType)
        statement.bindText(4, _tmp_1)
        statement.bindLong(5, entity.startedAtMillis)
        val _tmpEndedAtMillis: Long? = entity.endedAtMillis
        if (_tmpEndedAtMillis == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpEndedAtMillis)
        }
      }
    }
  }

  public override fun insertAll(activities: List<ActivityEntity>): Unit = performBlocking(__db,
      false, true) { _connection ->
    __insertAdapterOfActivityEntity.insert(_connection, activities)
  }

  public override fun forSegments(segmentIds: List<Long>): List<ActivityEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM activities WHERE routeSegmentId IN (")
    val _inputSize: Int = segmentIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in segmentIds) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRouteSegmentId: Int = getColumnIndexOrThrow(_stmt, "routeSegmentId")
        val _columnIndexOfActivityLevel: Int = getColumnIndexOrThrow(_stmt, "activityLevel")
        val _columnIndexOfTransportType: Int = getColumnIndexOrThrow(_stmt, "transportType")
        val _columnIndexOfStartedAtMillis: Int = getColumnIndexOrThrow(_stmt, "startedAtMillis")
        val _columnIndexOfEndedAtMillis: Int = getColumnIndexOrThrow(_stmt, "endedAtMillis")
        val _result: MutableList<ActivityEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: ActivityEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRouteSegmentId: Long?
          if (_stmt.isNull(_columnIndexOfRouteSegmentId)) {
            _tmpRouteSegmentId = null
          } else {
            _tmpRouteSegmentId = _stmt.getLong(_columnIndexOfRouteSegmentId)
          }
          val _tmpActivityLevel: ActivityLevel
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfActivityLevel)
          _tmpActivityLevel = __routeTypeConverters.toActivityLevel(_tmp)
          val _tmpTransportType: TransportType
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfTransportType)
          _tmpTransportType = __routeTypeConverters.toTransportType(_tmp_1)
          val _tmpStartedAtMillis: Long
          _tmpStartedAtMillis = _stmt.getLong(_columnIndexOfStartedAtMillis)
          val _tmpEndedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndedAtMillis)) {
            _tmpEndedAtMillis = null
          } else {
            _tmpEndedAtMillis = _stmt.getLong(_columnIndexOfEndedAtMillis)
          }
          _item_1 =
              ActivityEntity(_tmpId,_tmpRouteSegmentId,_tmpActivityLevel,_tmpTransportType,_tmpStartedAtMillis,_tmpEndedAtMillis)
          _result.add(_item_1)
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
