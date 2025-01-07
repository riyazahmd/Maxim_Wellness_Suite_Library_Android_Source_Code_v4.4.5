package com.maximintegrated.maximsensorsapp.sleep.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.maximintegrated.maximsensorsapp.sleep.database.SearchFile
import com.maximintegrated.maximsensorsapp.sleep.database.SleepDatabase
import com.maximintegrated.maximsensorsapp.sleep.database.dao.SourceDao
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SourceRepository(application: Application) {
    private val sourceDao: SourceDao

    init {
        val db = SleepDatabase.getInstance(application.applicationContext)
        sourceDao = db.sourceDao()
    }

    fun listFileExists(nameList: List<String>, listMd5: List<String>): LiveData<List<SearchFile>> {
        return sourceDao.getListByNameAndMd5(nameList, listMd5)
    }

    suspend fun insert(source: Source): Long {
        return withContext(Dispatchers.IO) {
            sourceDao.insert(source)
        }
    }

    suspend fun deleteByFileName(fileName: String) {
        withContext(Dispatchers.IO) {
            sourceDao.deleteByFileName(fileName)
        }
    }
}