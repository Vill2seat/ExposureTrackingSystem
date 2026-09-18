package com.exposuretrackingsystem.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.exposuretrackingsystem.app.data.local.entity.ActivityEntity
import com.exposuretrackingsystem.app.data.local.entity.LocationPointEntity
import com.exposuretrackingsystem.app.data.local.entity.RouteEntity
import com.exposuretrackingsystem.app.data.local.entity.RouteSegmentEntity
import com.exposuretrackingsystem.app.data.model.Activity
import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.exposuretrackingsystem.app.data.model.Route
import com.exposuretrackingsystem.app.data.model.RouteSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

interface RouteRepository {
    fun saveCompletedRoute(route: Route, activities: List<Activity> = emptyList())
    suspend fun loadCompletedRoute(routeId: Long): PersistedRoute?
}

data class PersistedRoute(
    val route: Route,
    val activities: List<Activity>
)

class RoomRouteRepository(
    private val store: RouteEntityStore,
    private val onPersistenceError: (Exception) -> Unit = {},
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : RouteRepository {
    override fun saveCompletedRoute(route: Route, activities: List<Activity>) {
        saveCompletedRouteAsync(route, activities)
    }

    override suspend fun loadCompletedRoute(routeId: Long): PersistedRoute? {
        return withContext(Dispatchers.IO) {
            try {
                store.load(routeId)?.toDomain()
            } catch (exception: Exception) {
                reportPersistenceError(exception)
                null
            }
        }
    }

    internal fun saveCompletedRouteAsync(
        route: Route,
        activities: List<Activity> = emptyList()
    ): Job {
        val routeEntity = route.toEntity()
        val locationPointEntities = route.locationPoints.map(LocationPoint::toEntity)
        val segmentEntities = route.segments.map(RouteSegment::toEntity)
        val activityEntities = activities.map(Activity::toEntity)
        return scope.launch {
            try {
                store.save(
                    route = routeEntity,
                    locationPoints = locationPointEntities,
                    segments = segmentEntities,
                    activities = activityEntities
                )
            } catch (exception: Exception) {
                reportPersistenceError(exception)
            }
        }
    }

    companion object {
        fun create(context: Context, onPersistenceError: (Exception) -> Unit = {}): RoomRouteRepository {
            return RoomRouteRepository(
                store = RoomRouteEntityStore(
                    ExposureTrackingDatabaseProvider.get(context)
                ),
                onPersistenceError = onPersistenceError
            )
        }
    }

    private fun reportPersistenceError(exception: Exception) {
        runCatching { onPersistenceError(exception) }
    }
}

interface RouteEntityStore {
    suspend fun save(
        route: RouteEntity,
        locationPoints: List<LocationPointEntity>,
        segments: List<RouteSegmentEntity>,
        activities: List<ActivityEntity>
    )

    suspend fun load(routeId: Long): PersistedRouteEntities?
}

data class PersistedRouteEntities(
    val route: RouteEntity,
    val locationPoints: List<LocationPointEntity>,
    val segments: List<RouteSegmentEntity>,
    val activities: List<ActivityEntity>
)

internal class RoomRouteEntityStore(
    private val database: ExposureTrackingDatabase
) : RouteEntityStore {
    override suspend fun save(
        route: RouteEntity,
        locationPoints: List<LocationPointEntity>,
        segments: List<RouteSegmentEntity>,
        activities: List<ActivityEntity>
    ) {
        database.withTransaction {
            database.routeDao().insert(route)
            database.locationPointDao().insertAll(locationPoints)
            database.routeSegmentDao().insertAll(segments)
            database.activityDao().insertAll(activities)
        }
    }

    override suspend fun load(routeId: Long): PersistedRouteEntities? {
        val route = database.routeDao().findById(routeId) ?: return null
        val locationPoints = database.locationPointDao().forRoute(routeId)
        val segments = database.routeSegmentDao().forRoute(routeId)
        val activities = if (segments.isEmpty()) {
            emptyList()
        } else {
            database.activityDao().forSegments(segments.map { it.id })
        }
        return PersistedRouteEntities(route, locationPoints, segments, activities)
    }

}

object ExposureTrackingDatabaseProvider {
    @Volatile
    private var instance: ExposureTrackingDatabase? = null

    fun get(context: Context): ExposureTrackingDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ExposureTrackingDatabase::class.java,
                "exposure_tracking.db"
            ).build().also { instance = it }
        }
    }
}

fun Route.toEntity() = RouteEntity(
    id = id,
    startedAtMillis = startedAtMillis,
    endedAtMillis = endedAtMillis,
    transportType = transportType,
    totalDistanceMeters = totalDistanceMeters,
    totalDurationSeconds = totalDurationSeconds,
    averageSpeedKmh = averageSpeedKmh
)

fun LocationPoint.toEntity() = LocationPointEntity(
    id = id,
    routeId = routeId,
    timestampMillis = timestampMillis,
    latitude = latitude,
    longitude = longitude,
    altitudeMeters = altitudeMeters,
    headingDegrees = headingDegrees,
    accuracyMeters = accuracyMeters,
    speedKmh = speedKmh
)

fun RouteSegment.toEntity() = RouteSegmentEntity(
    id = id,
    routeId = routeId,
    startLocationPointId = startLocationPointId,
    endLocationPointId = endLocationPointId,
    activityId = activityId,
    startTimeMillis = startTimeMillis,
    endTimeMillis = endTimeMillis,
    distanceMeters = distanceMeters,
    durationSeconds = durationSeconds,
    averageSpeedKmh = averageSpeedKmh
)

fun Activity.toEntity() = ActivityEntity(
    id = id,
    routeSegmentId = routeSegmentId,
    activityLevel = activityLevel,
    transportType = transportType,
    startedAtMillis = startedAtMillis,
    endedAtMillis = endedAtMillis
)

private fun PersistedRouteEntities.toDomain(): PersistedRoute {
    val restoredRoute = Route(
        id = route.id,
        startedAtMillis = route.startedAtMillis,
        transportType = route.transportType,
        endedAtMillis = route.endedAtMillis,
        locationPoints = locationPoints.map(LocationPointEntity::toDomain).toMutableList(),
        segments = segments.map(RouteSegmentEntity::toDomain).toMutableList(),
        totalDistanceMeters = route.totalDistanceMeters,
        totalDurationSeconds = route.totalDurationSeconds,
        averageSpeedKmh = route.averageSpeedKmh
    )
    return PersistedRoute(
        route = restoredRoute,
        activities = activities.map(ActivityEntity::toDomain)
    )
}

private fun LocationPointEntity.toDomain() = LocationPoint(
    id = id,
    routeId = routeId,
    timestampMillis = timestampMillis,
    latitude = latitude,
    longitude = longitude,
    altitudeMeters = altitudeMeters,
    headingDegrees = headingDegrees,
    accuracyMeters = accuracyMeters,
    speedKmh = speedKmh
)

private fun RouteSegmentEntity.toDomain() = RouteSegment(
    id = id,
    routeId = routeId,
    startLocationPointId = startLocationPointId,
    endLocationPointId = endLocationPointId,
    activityId = activityId,
    startTimeMillis = startTimeMillis,
    endTimeMillis = endTimeMillis,
    distanceMeters = distanceMeters,
    durationSeconds = durationSeconds,
    averageSpeedKmh = averageSpeedKmh
)

private fun ActivityEntity.toDomain() = Activity(
    id = id,
    routeSegmentId = routeSegmentId,
    activityLevel = activityLevel,
    transportType = transportType,
    startedAtMillis = startedAtMillis,
    endedAtMillis = endedAtMillis
)
