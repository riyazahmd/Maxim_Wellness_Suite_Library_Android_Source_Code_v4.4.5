package com.maximintegrated.maximsensorsapp.sleep.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.maximintegrated.maximsensorsapp.sleep.database.dao.SleepDao
import com.maximintegrated.maximsensorsapp.sleep.database.dao.SourceAndAllSleepsDao
import com.maximintegrated.maximsensorsapp.sleep.database.dao.SourceDao
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Sleep
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Source

@Database(entities = [(Sleep::class), (Source::class)], version = 7, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao

    abstract fun sourceDao(): SourceDao

    abstract fun sourceAndAllSleepsDao(): SourceAndAllSleepsDao

    companion object {
        private var dbInstance: SleepDatabase? = null

        @Synchronized
        fun getInstance(context: Context): SleepDatabase {
            if (dbInstance == null) {
                dbInstance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    "sleepQa"
                )
                    .fallbackToDestructiveMigration().build()
            }
            return dbInstance!!
        }
    }
}