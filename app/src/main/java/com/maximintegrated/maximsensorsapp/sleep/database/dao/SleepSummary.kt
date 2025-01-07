package com.maximintegrated.maximsensorsapp.sleep.database.dao

data class SleepSummary(
    var sourceId: Long,
    var sleepDate: Long,
    var sleepPhaseId: Int,
    var count: Long
)