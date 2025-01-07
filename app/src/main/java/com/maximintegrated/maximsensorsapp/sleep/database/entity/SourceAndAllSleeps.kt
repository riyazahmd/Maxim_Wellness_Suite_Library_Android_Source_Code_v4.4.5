package com.maximintegrated.maximsensorsapp.sleep.database.entity

import androidx.room.Embedded
import androidx.room.Relation

class SourceAndAllSleeps {
    @Embedded
    var source: Source? = null

    @Relation(parentColumn = "id", entityColumn = "source_id")
    var sleepList: List<Sleep>? = null
}