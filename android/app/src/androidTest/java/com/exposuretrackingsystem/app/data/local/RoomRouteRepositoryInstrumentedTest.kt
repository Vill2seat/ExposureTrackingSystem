package com.exposuretrackingsystem.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.exposuretrackingsystem.app.data.model.LocationPoint
import com.exposuretrackingsystem.app.data.model.Activity
import com.exposuretrackingsystem.app.data.model.ActivityLevel
import com.exposuretrackingsystem.app.data.model.Route
import com.exposuretrackingsystem.app.data.model.RouteSegment
import com.exposuretrackingsystem.app.data.model.TransportType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomRouteRepositoryInstrumentedTest {
    private lateinit var database: ExposureTrackingDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ExposureTrackingDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun savesCompletedRouteAndRelatedRowsInRoom() = runBlocking {
        val route = Route(
            id = 101L,
            startedAtMillis = 1_000L,
            endedAtMillis = 2_000L,
            transportType = TransportType.WALK,
            locationPoints = mutableListOf(
                LocationPoint(
                    id = 201L,
                    routeId = 101L,
                    timestampMillis = 1_000L,
                    latitude = 50.0,
                    longitude = 8.0,
                    altitudeMeters = 0.0,
                    headingDegrees = 0.0,
                    accuracyMeters = 5.0,
                    speedKmh = 1.0
                )
            ),
            segments = mutableListOf(
                RouteSegment(
                    id = 301L,
                    routeId = 101L,
                    startLocationPointId = 201L,
                    endLocationPointId = 201L,
                    startTimeMillis = 1_000L,
                    endTimeMillis = 2_000L,
                    distanceMeters = 0.0,
                    durationSeconds = 1.0,
                    averageSpeedKmh = 0.0
                )
            )
        )
        val activity = Activity(
            id = 401L,
            routeSegmentId = route.segments.single().id,
            activityLevel = ActivityLevel.WALK,
            transportType = TransportType.WALK,
            startedAtMillis = 1_000L,
            endedAtMillis = 2_000L
        )
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        val repository = RoomRouteRepository(RoomRouteEntityStore(database), scope = scope)

        repository.saveCompletedRouteAsync(route, listOf(activity)).join()
        val loaded = repository.loadCompletedRoute(route.id)

        requireNotNull(loaded)
        assertEquals(route, loaded.route)
        assertEquals(listOf(activity), loaded.activities)
        assertEquals(1, database.routeDao().observeAll().size)
        assertEquals(1, database.locationPointDao().forRoute(route.id).size)
        assertEquals(1, database.routeSegmentDao().forRoute(route.id).size)
        assertEquals(1, database.activityDao().forSegments(listOf(route.segments.single().id)).size)
    }
}
