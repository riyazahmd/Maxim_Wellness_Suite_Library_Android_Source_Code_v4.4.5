package com.maximintegrated.qardio

import java.util.*

data class BloodPressureMeasurement(
    val systolic: Float,
    val diastolic: Float,
    val meanArterialPressure: Float,
    val unit: Int,
    val pulseRate: Float?,
    val userID: Int?,
    val status: Status?,
    val calendar: Calendar?,
    val currentTimeMillis: Long = System.currentTimeMillis()
) {
    companion object {
        val CSV_HEADER = arrayOf("timestamp", "systolic", "diastolic", "pulse_rate")
    }

    data class Status(
        val bodyMovementDetected: Boolean,
        val cuffTooLoose: Boolean,
        val irregularPulseDetected: Boolean,
        val pulseRateInRange: Boolean,
        val pulseRateExceedsUpperLimit: Boolean,
        val pulseRateIsLessThenLowerLimit: Boolean,
        val improperMeasurementPosition: Boolean
    )

    val hasFailed: Boolean
        get() = systolic.isNaN()

    val hasCompleted: Boolean
        get() = diastolic != 0f

    // overridden not to include timestamp
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BloodPressureMeasurement) return false

        if (systolic != other.systolic) return false
        if (diastolic != other.diastolic) return false
        if (meanArterialPressure != other.meanArterialPressure) return false
        if (unit != other.unit) return false
        if (pulseRate != other.pulseRate) return false
        if (userID != other.userID) return false
        if (status != other.status) return false
        if (calendar != other.calendar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = systolic.hashCode()
        result = 31 * result + diastolic.hashCode()
        result = 31 * result + meanArterialPressure.hashCode()
        result = 31 * result + unit
        result = 31 * result + (pulseRate?.hashCode() ?: 0)
        result = 31 * result + (userID ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (calendar?.hashCode() ?: 0)
        return result
    }

    fun toCsvModel(): String {
        return arrayOf(
            currentTimeMillis,
            systolic,
            diastolic,
            pulseRate ?: 0
        ).joinToString(separator = ",")
    }

}