package com.exposuretrackingsystem.app.data.model

enum class SensorSampleType {
    ACCELEROMETER,
    GYROSCOPE,
    PRESSURE
}

data class SensorSample(
    val timestampMillis: Long,
    val sensorType: SensorSampleType,
    val x: Float? = null,
    val y: Float? = null,
    val z: Float? = null,
    val pressureHpa: Float? = null
)
