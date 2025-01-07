package com.maximintegrated.maximsensorsapp.sleep.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.maximintegrated.maximsensorsapp.sleep.database.entity.SourceAndAllSleeps

@Dao
interface SourceAndAllSleepsDao {

    @Query("SELECT * FROM Source")
    fun getSourceWithSleeps(): LiveData<List<SourceAndAllSleeps>>

    @Query("SELECT * FROM Source where id=:sourceId")
    fun getSourceWithSleepsBySourceId(sourceId: Long): LiveData<SourceAndAllSleeps>

    @Query("select a.id as sourceId, max(b.date) as sleepDate,b.sleep_phases_output as sleepPhaseId,count(b.sleep_phases_output) as count from Source a, sleep b where a.id = b.source_id group by a.id, b.sleep_phases_output order by b.sleep_phases_output asc")
    fun getSleepSummary(): LiveData<List<SleepSummary>>

}