package com.maximintegrated.polar

data class HeartRateMeasurement(
    val heartRate: Int,
    val contactDetected: Boolean?,
    val energyExpanded: Int?,
    val rrIntervals: List<Int>?,
    val currentTimeMillis: Long = System.currentTimeMillis()
)