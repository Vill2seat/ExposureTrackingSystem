package com.exposuretrackingsystem.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.exposuretrackingsystem.app.data.model.SensorSample
import com.exposuretrackingsystem.app.data.model.SensorSampleType

class SensorDataManager(context: Context) : SensorEventListener {
    private val sensorManager = context.applicationContext
        .getSystemService(SensorManager::class.java)

    private val sensorsByType = mapOf(
        SensorSampleType.ACCELEROMETER to sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorSampleType.GYROSCOPE to sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
        SensorSampleType.PRESSURE to sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
    )

    private val samples = mutableListOf<SensorSample>()
    private val registeredSensorTypes = mutableSetOf<SensorSampleType>()

    val availableSensorTypes: Set<SensorSampleType> = sensorsByType
        .filterValues { it != null }
        .keys

    var onSample: ((SensorSample) -> Unit)? = null

    val isStarted: Boolean
        get() = registeredSensorTypes.isNotEmpty()

    fun start(): Boolean {
        if (sensorManager == null) {
            return false
        }

        if (registeredSensorTypes.isNotEmpty()) {
            return true
        }

        sensorsByType.forEach { (sampleType, sensor) ->
            if (sensor != null && sensorManager.registerListener(this, sensor, SENSOR_DELAY_US)) {
                registeredSensorTypes += sampleType
            }
        }
        return registeredSensorTypes.isNotEmpty()
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        registeredSensorTypes.clear()
    }

    fun clearSamples() {
        synchronized(samples) {
            samples.clear()
        }
    }

    fun sampleCountsSnapshot(): Map<SensorSampleType, Int> = synchronized(samples) {
        SensorSampleType.values().associateWith { sampleType ->
            samples.count { it.sensorType == sampleType }
        }
    }

    fun samplesSnapshot(): List<SensorSample> = synchronized(samples) {
        samples.toList()
    }

    fun samplesSnapshot(
        startTimestampMillis: Long,
        endTimestampMillis: Long
    ): List<SensorSample> = synchronized(samples) {
        samples.filter { sample ->
            sample.timestampMillis in startTimestampMillis..endTimestampMillis
        }
    }

    fun sampleCountsSnapshot(
        startTimestampMillis: Long,
        endTimestampMillis: Long
    ): Map<SensorSampleType, Int> = synchronized(samples) {
        SensorSampleType.values().associateWith { sampleType ->
            samples.count {
                it.sensorType == sampleType &&
                    it.timestampMillis in startTimestampMillis..endTimestampMillis
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sample = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> SensorSample(
                timestampMillis = System.currentTimeMillis(),
                sensorType = SensorSampleType.ACCELEROMETER,
                x = event.values[0],
                y = event.values[1],
                z = event.values[2]
            )
            Sensor.TYPE_GYROSCOPE -> SensorSample(
                timestampMillis = System.currentTimeMillis(),
                sensorType = SensorSampleType.GYROSCOPE,
                x = event.values[0],
                y = event.values[1],
                z = event.values[2]
            )
            Sensor.TYPE_PRESSURE -> SensorSample(
                timestampMillis = System.currentTimeMillis(),
                sensorType = SensorSampleType.PRESSURE,
                pressureHpa = event.values[0]
            )
            else -> return
        }

        synchronized(samples) {
            samples += sample
        }
        onSample?.invoke(sample)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        private const val SENSOR_DELAY_US = 20_000
    }
}
