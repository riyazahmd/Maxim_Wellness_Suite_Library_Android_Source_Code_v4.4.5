package com.maximintegrated.nonin

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PlxMeasurement(
    val flags: Int,
    val spo2Normal: Float,
    val prNormal: Float,
    val spo2Fast: Float,
    val prFast: Float,
    val spo2Slow: Float,
    val prSlow: Float,
    val measurementStatus: Int,
    val deviceAndSensorStatus: Int,
    private val deviceAndSensorStatusReserved: Int,
    val pulseAmplitudeIndex: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromPacket(packet: ByteArray): PlxMeasurement {
            val buffer = ByteBuffer.wrap(packet)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            return with(buffer) {
                PlxMeasurement(
                    flags = get().toInt(),
                    spo2Normal = short.toSFloat(),
                    prNormal = short.toSFloat(),
                    spo2Fast = short.toSFloat(),
                    prFast = short.toSFloat(),
                    spo2Slow = short.toSFloat(),
                    prSlow = short.toSFloat(),
                    measurementStatus = short.toInt(),
                    deviceAndSensorStatus = short.toInt(),
                    deviceAndSensorStatusReserved = get().toInt(),
                    pulseAmplitudeIndex = short.toSFloat()
                )
            }
        }

        val CSV_HEADER = arrayOf(
            "timestamp",
            "flags",
            "spo2Normal",
            "prNormal",
            "spo2Fast",
            "prFast",
            "spo2Slow",
            "prSlow",
            "measurementStatus",
            "deviceAndSensorStatus",
            "deviceAndSensorStatusReserved",
            "pulseAmplitudeIndex"
        )
    }

    fun toCsvModel(): String {
        return arrayOf(
            timestamp,
            flags,
            spo2Normal,
            prNormal,
            spo2Fast,
            prFast,
            spo2Slow,
            prSlow,
            measurementStatus,
            deviceAndSensorStatus,
            deviceAndSensorStatusReserved,
            pulseAmplitudeIndex
        ).joinToString(separator = ",")
    }
}
