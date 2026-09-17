package com.exposuretrackingsystem.app.location

import com.exposuretrackingsystem.app.data.model.LocationPoint
import java.io.File
import java.time.Instant
import java.util.Locale

class GpsDiagnosticLogger(context: android.content.Context) {
    val file: File = File(context.applicationContext.filesDir, FILE_NAME)

    init {
        if (!file.exists()) {
            file.writeText(HEADER, Charsets.UTF_8)
        }
    }

    @Synchronized
    fun logStart(routeId: Long) {
        append("EVENT,${now()},START,,,,,,,,routeId=$routeId")
    }

    @Synchronized
    fun logPoint(
        point: LocationPoint,
        decision: String,
        rejectionReason: String,
        distanceMeters: Double?,
        segmentSpeedKmh: Double?
    ) {
        append(
            listOf(
                "POINT",
                Instant.ofEpochMilli(point.timestampMillis),
                decision,
                point.latitude,
                point.longitude,
                point.accuracyMeters,
                point.speedKmh,
                point.headingDegrees,
                point.altitudeMeters,
                distanceMeters ?: "",
                segmentSpeedKmh ?: "",
                rejectionReason
            ).joinToString(",")
        )
    }

    @Synchronized
    fun logStop(
        routeId: Long,
        totalDistanceMeters: Double,
        durationSeconds: Double,
        averageSpeedKmh: Double,
        acceptedPoints: Int,
        rejectedPoints: Int
    ) {
        append(
            listOf(
                "EVENT",
                now(),
                "STOP",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                String.format(
                    Locale.US,
                    "routeId=%d;totalDistanceMeters=%.3f;durationSeconds=%.3f;averageSpeedKmh=%.3f;acceptedPoints=%d;rejectedPoints=%d",
                    routeId,
                    totalDistanceMeters,
                    durationSeconds,
                    averageSpeedKmh,
                    acceptedPoints,
                    rejectedPoints
                )
            ).joinToString(",")
        )
    }

    private fun append(line: String) {
        file.appendText("$line\n", Charsets.UTF_8)
    }

    private fun now(): String = Instant.now().toString()

    companion object {
        private const val FILE_NAME = "gps_diagnostic.csv"
        private const val HEADER =
            "recordType,timestamp,decision,latitude,longitude,accuracyMeters,gpsSpeedKmh,headingDegrees,altitudeMeters,distanceFromPreviousValidMeters,calculatedSegmentSpeedKmh,details\n"
    }
}