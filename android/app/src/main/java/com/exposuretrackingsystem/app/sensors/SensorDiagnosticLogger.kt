package com.exposuretrackingsystem.app.sensors

import android.content.Context
import com.exposuretrackingsystem.app.data.model.SensorSample
import java.io.File

class SensorDiagnosticLogger(context: Context) {
    val file: File = File(context.applicationContext.filesDir, FILE_NAME)

    init {
        if (!file.exists()) {
            file.writeText(HEADER, Charsets.UTF_8)
        }
    }

    @Synchronized
    fun logSample(sample: SensorSample) {
        file.appendText(
            listOf(
                sample.timestampMillis,
                sample.sensorType,
                sample.x ?: "",
                sample.y ?: "",
                sample.z ?: "",
                sample.pressureHpa ?: ""
            ).joinToString(",") + "\n",
            Charsets.UTF_8
        )
    }

    companion object {
        private const val FILE_NAME = "sensor_diagnostic.csv"
        private const val HEADER =
            "timestamp,sensorType,x,y,z,pressureHpa\n"
    }
}
