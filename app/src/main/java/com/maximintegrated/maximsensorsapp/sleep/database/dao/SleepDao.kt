package com.maximintegrated.maximsensorsapp.sleep.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Sleep

@Dao
interface SleepDao {
    @Query("SELECT * FROM Sleep")
    fun getAll(): LiveData<List<Sleep>>

    @Query("SELECT COUNT(sleep_phases_output) FROM Sleep WHERE sleep_wake_output = 0")
    fun getTotalWakeDuration(): Long

    @Insert
    fun insertAll(sleeps: List<Sleep>)

    @Delete
    fun delete(sleep: Sleep)
}