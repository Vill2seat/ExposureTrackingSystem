package com.exposuretrackingsystem.app.data.local

import com.exposuretrackingsystem.app.data.local.entity.ActivityEntity
import com.exposuretrackingsystem.app.data.local.entity.LocationPointEntity
import com.exposuretrackingsystem.app.data.local.entity.RouteEntity
import com.exposuretrackingsystem.app.data.local.entity.RouteSegmentEntity
import com.exposuretrackingsystem.app.data.model.Activity
import com.exposuretrackingsystem.app.data.model.ActivityLevel
import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.exposuretrackingsystem.app.data.model.Route
import com.exposuretrackingsystem.app.data.model.RouteSegment
import com.exposuretrackingsystem.app.data.model.TransportType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutePersistenceTest {
    @Test
    fun mapsCompletedRouteAndRelatedRecordsToEntities() {
        val route = completedRoute()
        val activity = Activity(
            routeSegmentId = route.segments.single().id,
            activityLevel = ActivityLevel.WALK,
            transportType = TransportType.WALK,
            startedAtMillis = 1_000L,
            endedAtMillis = 2_000L
        )
        val store = RecordingRouteEntityStore()
        val repository = RoomRouteRepository(
            store = store,
            scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        )

        runBlocking { repository.saveCompletedRouteAsync(route, listOf(activity)).join() }

        assertEquals(route.toEntity(), store.route)
        assertEquals(route.locationPoints.map(LocationPoint::toEntity), store.locationPoints)
        assertEquals(route.segments.map(RouteSegment::toEntity), store.segments)
        assertEquals(listOf(activity.toEntity()), store.activities)
    }

    @Test
    fun persistenceFailureIsReportedWithoutThrowing() {
        val failure = IllegalStateException("database unavailable")
        var reported: Exception? = null
        val repository = RoomRouteRepository(
            store = failingStore(failure),
            onPersistenceError = { reported = it },
            scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        )

        runBlocking { repository.saveCompletedRouteAsync(completedRoute()).join() }

        assertEquals(failure, reported)
    }

    @Test
    fun persistenceErrorCallbackCannotEscapePersistenceCoroutine() {
        val repository = RoomRouteRepository(
            store = failingStore(IllegalStateException("database unavailable")),
            onPersistenceError = { throw IllegalStateException("reporting failed") },
            scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        )

        runBlocking { repository.saveCompletedRouteAsync(completedRoute()).join() }

        assertTrue(true)
    }

    @Test
    fun loadsPersistedRouteAndAllRelatedRecords() = runBlocking {
        val route = completedRoute()
        val activity = Activity(
            routeSegmentId = route.segments.single().id,
            activityLevel = ActivityLevel.WALK,
            transportType = TransportType.WALK,
            startedAtMillis = 1_000L,
            endedAtMillis = 2_000L
        )
        val store = RecordingRouteEntityStore().apply {
            loaded = PersistedRouteEntities(
                route = route.toEntity(),
                locationPoints = route.locationPoints.map(LocationPoint::toEntity),
                segments = route.segments.map(RouteSegment::toEntity),
                activities = listOf(activity.toEntity())
            )
        }
        val repository = RoomRouteRepository(store)

        val loaded = repository.loadCompletedRoute(route.id)

        requireNotNull(loaded)
        assertEquals(route, loaded.route)
        assertEquals(listOf(activity), loaded.activities)
    }

    @Test
    fun loadsRouteWithEmptyRelatedRecords() = runBlocking {
        val route = completedRoute().copy(
            locationPoints = mutableListOf(),
            segments = mutableListOf()
        )
        val store = RecordingRouteEntityStore().apply {
            loaded = PersistedRouteEntities(route.toEntity(), emptyList(), emptyList(), emptyList())
        }

        val loaded = RoomRouteRepository(store).loadCompletedRoute(route.id)

        requireNotNull(loaded)
        assertTrue(loaded.route.locationPoints.isEmpty())
        assertTrue(loaded.route.segments.isEmpty())
        assertTrue(loaded.activities.isEmpty())
    }

    @Test
    fun missingRouteReturnsNull() = runBlocking {
        val loaded = RoomRouteRepository(RecordingRouteEntityStore()).loadCompletedRoute(999L)

        assertNull(loaded)
    }

    private fun failingStore(failure: Exception) = object : RouteEntityStore {
        override suspend fun save(
            route: RouteEntity,
            locationPoints: List<LocationPointEntity>,
            segments: List<RouteSegmentEntity>,
            activities: List<ActivityEntity>
        ) {
            throw failure
        }

        override suspend fun load(routeId: Long): PersistedRouteEntities? = null
    }

    private fun completedRoute(): Route {
        val firstPoint = LocationPoint(
            routeId = 11L,
            timestampMillis = 1_000L,
            latitude = 50.0,
            longitude = 8.0,
            altitudeMeters = 100.0,
            headingDegrees = 90.0,
            accuracyMeters = 4.0,
            speedKmh = 5.0
        )
        val secondPoint = firstPoint.copy(timestampMillis = 2_000L, latitude = 50.001)
        val segment = RouteSegment(
            routeId = 11L,
            startLocationPointId = firstPoint.id,
            endLocationPointId = secondPoint.id,
            startTimeMillis = firstPoint.timestampMillis,
            endTimeMillis = secondPoint.timestampMillis,
            distanceMeters = 111.0,
            durationSeconds = 1.0,
            averageSpeedKmh = 399.6
        )
        return Route(
            id = 11L,
            startedAtMillis = 1_000L,
            transportType = TransportType.WALK,
            endedAtMillis = 2_000L,
            locationPoints = mutableListOf(firstPoint, secondPoint),
            segments = mutableListOf(segment),
            totalDistanceMeters = 111.0,
            totalDurationSeconds = 1.0,
            averageSpeedKmh = 399.6
        )
    }

    private class RecordingRouteEntityStore : RouteEntityStore {
        var route: RouteEntity? = null
        var locationPoints: List<LocationPointEntity> = emptyList()
        var segments: List<RouteSegmentEntity> = emptyList()
        var activities: List<ActivityEntity> = emptyList()
        var loaded: PersistedRouteEntities? = null

        override suspend fun save(
            route: RouteEntity,
            locationPoints: List<LocationPointEntity>,
            segments: List<RouteSegmentEntity>,
            activities: List<ActivityEntity>
        ) {
            this.route = route
            this.locationPoints = locationPoints
            this.segments = segments
            this.activities = activities
        }

        override suspend fun load(routeId: Long): PersistedRouteEntities? {
            return loaded?.takeIf { it.route.id == routeId }
        }
    }
}
