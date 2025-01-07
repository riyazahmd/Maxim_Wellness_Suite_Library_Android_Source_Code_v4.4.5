package com.maximintegrated.maximsensorsapp.sleep.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.maximintegrated.maximsensorsapp.sleep.database.dao.SleepSummary
import com.maximintegrated.maximsensorsapp.sleep.database.entity.SourceAndAllSleeps
import com.maximintegrated.maximsensorsapp.sleep.database.repository.SourceAndAllSleepsRepository

class SourceAndAllSleepsViewModel(application: Application) : AndroidViewModel(application) {
    private val sourceAndAllSleepsRepository: SourceAndAllSleepsRepository =
        SourceAndAllSleepsRepository(application)
    internal val sleepSummaryList: LiveData<List<SleepSummary>>
    internal val sourceWithSleepsList: LiveData<List<SourceAndAllSleeps>>

    private val sourceId = MutableLiveData<Long>()
    internal val sourceList: LiveData<SourceAndAllSleeps> =
        Transformations.switchMap(sourceId) { id ->
            sourceAndAllSleepsRepository.getSourceWithSleepsBySourceId(
                id
            )
        }

    init {
        sleepSummaryList = sourceAndAllSleepsRepository.getSleepSummaryList()
        sourceWithSleepsList = sourceAndAllSleepsRepository.getSourceWithSleeps()
    }

    fun getById(sourceId: Long): LiveData<SourceAndAllSleeps> {
        return sourceAndAllSleepsRepository.getSourceWithSleepsBySourceId(sourceId)
    }

    fun setInput(sourceId: Long) {
        this.sourceId.value = sourceId
    }

}