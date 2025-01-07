package com.maximintegrated.maximsensorsapp.sleep.database

import androidx.room.TypeConverter
import java.util.*

class DateConverter {

    @TypeConverter
    fun toDate(dateLong: Long?): Date {
        if (dateLong == null) {
            return Date()
        }
        return Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long {
        if (date == null) {
            return 0L
        }
        return date.time
    }
}