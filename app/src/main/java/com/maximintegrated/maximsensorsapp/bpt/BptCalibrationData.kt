package com.maximintegrated.maximsensorsapp.bpt

import com.maximintegrated.maximsensorsapp.toIntOrZero
import com.maximintegrated.maximsensorsapp.toLongOrZero
import java.util.*
import java.util.concurrent.TimeUnit

class BptCalibrationData {
    var hexString = ""
    var timestamp = 0L
    var sbp: Int = 0
    var dbp: Int = 0

    companion object {
        private const val NUMBER_OF_ELEMENTS_IN_LINE = 4
        private const val CALIBRATION_INDEX = 0
        private const val TIMESTAMP_INDEX = 1
        private const val SBP_INDEX = 2
        private const val DBP_INDEX = 3

        fun parseCalibrationDataFromString(calibrationString: String): BptCalibrationData {
            val parts = calibrationString.split(" ")
            return if (parts.size < NUMBER_OF_ELEMENTS_IN_LINE) {
                BptCalibrationData()
            } else {
                BptCalibrationData().apply {
                    hexString = parts[CALIBRATION_INDEX]
                    timestamp = parts[TIMESTAMP_INDEX].toLongOrZero()
                    sbp = parts[SBP_INDEX].toIntOrZero()
                    dbp = parts[DBP_INDEX].toIntOrZero()
                }
            }

        }
    }

    fun isExpired(): Boolean {
        val today = Date()
        val timeDiffMs = today.time - timestamp
        val timeDiffDays = TimeUnit.MILLISECONDS.toDays(timeDiffMs)
        return timeDiffDays > 28
    }

    fun getDate(): Date {
        return Date(timestamp)
    }
}