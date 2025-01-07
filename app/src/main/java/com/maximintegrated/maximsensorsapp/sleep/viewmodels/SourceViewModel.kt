package com.maximintegrated.maximsensorsapp.sleep.viewmodels

import android.app.Application
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import com.maximintegrated.maximsensorsapp.SQA_OUTPUT_DIRECTORY
import com.maximintegrated.maximsensorsapp.sleep.database.SearchFile
import com.maximintegrated.maximsensorsapp.sleep.database.repository.SourceRepository
import com.maximintegrated.maximsensorsapp.sleep.utils.CsvUtil
import kotlinx.coroutines.*

class SourceViewModel(application: Application) : AndroidViewModel(application) {


    private val sourceRepository: SourceRepository =
        SourceRepository(application)

    private val listParameter: MutableLiveData<SearchListParameter> = MutableLiveData()

    private val listFilePresent: LiveData<List<SearchFile>> =
        Transformations.switchMap(listParameter) {
            sourceRepository.listFileExists(it.nameList, it.md5List)
        }

    private val job = Job()

    private val uiScope = CoroutineScope(job + Dispatchers.Main)

    private var files: Array<DocumentFile>? = null

    private var filesImported = false

    private val _userId = MutableLiveData<String>()

    private val listFilePresentObserver = Observer<List<SearchFile>> { list ->
        if (!filesImported) {
            val nameList = list?.map { it.fileName }?.toMutableList()
            val fileNameList = files?.map { it.name!! }?.toMutableList()
            if (nameList != null) {
                fileNameList?.removeAll(nameList)
            }
            if (fileNameList != null) {
                importCsvFiles(fileNameList.toTypedArray())
            }
            filesImported = true
        }
    }

    private val _busy = MutableLiveData<Boolean>(true)
    val busy: LiveData<Boolean>
        get() = _busy

    init {
        listFilePresent.observeForever(listFilePresentObserver)
    }

    fun setUserId(userId: String) {
        if (_userId.value != userId) {
            files = null
            filesImported = false
            _userId.value = userId
        }
    }

    fun getSleepData() {
        if (files.isNullOrEmpty()) {
            _busy.value = true
            files = SQA_OUTPUT_DIRECTORY.listFiles()
            filesImported = false
            if (files != null) {
                uiScope.launch {
                    listParameter.value = getSleepData(files!!)
                }
            } else {
                _busy.value = false
            }
        } else {
            _busy.value = false
        }
    }

    private suspend fun getSleepData(files: Array<DocumentFile>): SearchListParameter{
        return withContext(Dispatchers.IO){
            SearchListParameter(files.map { it.name!! }, CsvUtil.listCalculateMD5(files.toList(), getApplication<Application>().contentResolver))
        }
    }

    private fun importCsvFiles(fileNames: Array<String>){
        uiScope.launch {
            for (fileName in fileNames) {
                val file = SQA_OUTPUT_DIRECTORY.findFile(fileName)
                CsvUtil.importFromCsv(getApplication(), file!!)
            }
            _busy.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        listFilePresent.removeObserver(listFilePresentObserver)
        job.cancel()
    }

    inner class SearchListParameter(
        var nameList: List<String>,
        var md5List: List<String>
    )
}