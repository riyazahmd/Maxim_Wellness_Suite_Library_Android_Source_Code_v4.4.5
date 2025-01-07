package com.maximintegrated.maximsensorsapp.sleep.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.maximintegrated.maximsensorsapp.sleep.database.SleepDatabase
import com.maximintegrated.maximsensorsapp.sleep.database.dao.SleepSummary
import com.maximintegrated.maximsensorsapp.sleep.database.dao.SourceAndAllSleepsDao
import com.maximintegrated.maximsensorsapp.sleep.database.entity.SourceAndAllSleeps

class SourceAndAllSleepsRepository(application: Application) {
    private val sourceAndAllSleepsDao: SourceAndAllSleepsDao
    private val sleepSummaryList: LiveData<List<SleepSummary>>
    private val allList: LiveData<List<SourceAndAllSleeps>>

    init {
        val db = SleepDatabase.getInstance(application.applicationContext)
        sourceAndAllSleepsDao = db.sourceAndAllSleepsDao()
        sleepSummaryList = sourceAndAllSleepsDao.getSleepSummary()
        allList = sourceAndAllSleepsDao.getSourceWithSleeps()
    }

    fun getSleepSummaryList(): LiveData<List<SleepSummary>> {
        return sleepSummaryList
    }

    fun getSourceWithSleepsBySourceId(sourceId: Long): LiveData<SourceAndAllSleeps> {
        return sourceAndAllSleepsDao.getSourceWithSleepsBySourceId(sourceId)
    }

    fun getSourceWithSleeps(): LiveData<List<SourceAndAllSleeps>> {
        return allList
    }
}