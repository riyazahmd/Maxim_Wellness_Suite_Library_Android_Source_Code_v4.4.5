package com.maximintegrated.maximsensorsapp.sleep.database.entity

import androidx.room.*
import com.maximintegrated.maximsensorsapp.sleep.database.DateConverter
import java.util.*

@Entity(
    foreignKeys = [ForeignKey(
        entity = Source::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("source_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(DateConverter::class)
class Sleep(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    @ColumnInfo(name = "source_id")
    var sourceId: Long,
    @ColumnInfo(name = "userId")
    val userId: String,
    @ColumnInfo(name = "date")
    var date: Date,
    @ColumnInfo(name = "is_sleep")
    var isSleep: Int,
    @ColumnInfo(name = "latency")
    var latency: Int,
    @ColumnInfo(name = "sleep_wake_output")
    var sleepWakeOutput: Int,
    @ColumnInfo(name = "sleep_phases_ready")
    var sleepPhasesReady: Int,
    @ColumnInfo(name = "sleep_phases_output")
    var sleepPhasesOutput: Int,
    @ColumnInfo(name = "encoded_output_sleep_phase_output")
    var encodedOutput_sleepPhaseOutput: Int,
    @ColumnInfo(name = "encoded_output_duration")
    var encodedOutput_duration: Int,
    @ColumnInfo(name = "encoded_output_needs_storage")
    var encodedOutput_needsStorage: Boolean,
    @ColumnInfo(name = "hr")
    var hr: Double,
    @ColumnInfo(name = "ibi")
    var ibi: Double,
    @ColumnInfo(name = "spo2")
    var spo2: Int,
    @ColumnInfo(name = "acc_mag")
    var accMag: Double,
    @ColumnInfo(name = "sleep_resting_hr")
    var sleepRestingHR: Float,
    @ColumnInfo(name = "sleep_phases_output_processed")
    var sleepPhasesOutputProcessed: Int
)