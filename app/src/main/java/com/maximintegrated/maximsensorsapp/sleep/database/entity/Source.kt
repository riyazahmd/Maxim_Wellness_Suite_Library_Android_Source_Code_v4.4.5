package com.maximintegrated.maximsensorsapp.sleep.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Source(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    @ColumnInfo(name = "file_name")
    var fileName: String,
    @ColumnInfo(name = "md5")
    var md5: String
)