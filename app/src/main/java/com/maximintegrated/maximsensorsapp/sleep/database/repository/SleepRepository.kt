package com.maximintegrated.maximsensorsapp.sleep.database.repository

import android.app.Application
import com.maximintegrated.maximsensorsapp.sleep.database.SleepDatabase
import com.maximintegrated.maximsensorsapp.sleep.database.dao.SleepDao
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SleepRepository(application: Application) {
    private val sleepDao: SleepDao

    init {
        val db = SleepDatabase.getInstance(application.applicationContext)
        sleepDao = db.sleepDao()
    }

    suspend fun insertAll(sleepList: List<Sleep>) {
        withContext(Dispatchers.IO) {
            sleepDao.insertAll(sleepList)
        }
    }
}