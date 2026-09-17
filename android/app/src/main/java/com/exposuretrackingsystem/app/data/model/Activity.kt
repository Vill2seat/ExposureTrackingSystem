package com.exposuretrackingsystem.app.data.model

data class Activity(
    val id: Long = ModelIdGenerator.next(),
    val routeSegmentId: Long? = null,
    val activityLevel: ActivityLevel,
    val transportType: TransportType,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null
)

enum class ActivityLevel {
    REST,
    WALK,
    FAST_WALK,
    RUN,
    BIKE
}

enum class TransportType {
    WALK,
    RUN,
    BIKE,
    CAR,
    BUS,
    TRAIN,
    TRAM,
    METRO,
    MOTORCYCLE,
    UNKNOWN,
    STATIONARY
}
