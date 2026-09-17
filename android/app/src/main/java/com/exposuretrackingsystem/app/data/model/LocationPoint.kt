package com.exposuretrackingsystem.app.data.model

/**
 * A recorded geographic point belonging to a Route.
 * Distances and measurements use the units defined by the technical specification.
 */
data class LocationPoint(
    val id: Long = ModelIdGenerator.next(),
    val routeId: Long? = null,
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val headingDegrees: Double,
    val accuracyMeters: Double,
    val speedKmh: Double
)
