package com.maximintegrated.maximsensorsapp.bpt

import android.app.Application
import android.os.Handler
import android.os.SystemClock
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maximintegrated.maximsensorsapp.BPT_DIRECTORY_NAME
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.getDirectoryReference
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

enum class BptStatus {
    NO_SIGNAL,
    PROGRESS,
    SUCCESS,
    BAD_SIGNAL,
    MOTION,
    FAILURE,
    CAL_SEGMENT_DONE,
    INIT_SUBJECT_FAILURE,
    INIT_SUCCESS,
    INIT_CAL_REF_BP_TRENDING_ERROR,
    INIT_CAL_REF_BP_INCONSISTENCY_ERROR_1,
    INIT_CAL_REF_BP_INCONSISTENCY_ERROR_2,
    INIT_CAL_REF_BP_INCONSISTENCY_ERROR_3,
    INIT_CAL_REF_CNT_MISMATCH,
    INIT_CAL_REF_OUT_OF_LIMIT,
    INIT_CAL_REF_MAXNUM_ERROR,
    PP_OUT_OF_RANGE_ERROR,
    HR_OUT_OF_RANGE,
    HR_ABOVE_RESTING,
    PI_OUT_OF_RANGE,
    ESTIMATION_ERROR,
    OUT_OF_RANGE_ERROR,
    OUT_OF_LIMIT_ERROR,
    NO_CONTACT_ERROR,
    NO_FINGER_ERROR,
    RESERVED;

    companion object {
        private val map = values().associateBy(BptStatus::ordinal)
        fun fromInt(index: Int) = map[index]
    }
}

private const val CALIBRATION_TIMEOUT_IN_SEC = 10

class BptViewModel(private val app: Application) : AndroidViewModel(app) {

    private val _userList = MutableLiveData<MutableList<String>>(arrayListOf())
    val userList: LiveData<MutableList<String>>
        get() = _userList

    private var startElapsedTime = 0L
    private var handler = Handler()
    private var timerStarted = false

    private val _elapsedTime = MutableLiveData<Long>(0)
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime

    var startTime = ""
        private set

    private val _calibrationStates = MutableLiveData<CalibrationStatus>()
    val calibrationStates: LiveData<CalibrationStatus?>
        get() = _calibrationStates

    private val _isMonitoring = MutableLiveData<Boolean>(false)
    val isMonitoring: LiveData<Boolean>
        get() = _isMonitoring

    val spO2Coefficients =
        floatArrayOf(1.5958422407923467f, -34.6596622470280020f, 112.6898759138307500f)

    private var calibrationTimePassed = AtomicInteger(-1)

    private val _historyDataList = MutableLiveData<List<BptHistoryData>?>(emptyList())
    val historyDataList: LiveData<List<BptHistoryData>?>
        get() = _historyDataList

    private val _calibrationDataList = MutableLiveData<List<BptCalibrationData>?>(emptyList())
    val calibrationDataList: LiveData<List<BptCalibrationData>?>
        get() = _calibrationDataList

    private val job = Job()

    private val uiScope = CoroutineScope(job + Dispatchers.Main)

    init {
        prepareUserList()
        readSpO2ConfigFile()
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    private fun readSpO2ConfigFile() {
        var file = getDirectoryReference(BPT_DIRECTORY_NAME)!!.findFile("SPO2.conf")
        if (file == null) {
            file = getDirectoryReference(BPT_DIRECTORY_NAME)!!.createFile("text/conf", "SPO2.conf")
            app.contentResolver.openOutputStream(file!!.uri)!!.bufferedWriter().use { out -> out.write("${spO2Coefficients[0]},${spO2Coefficients[1]},${spO2Coefficients[2]}") }
        } else {
            var str = ""
            app.contentResolver.openInputStream(file.uri)!!.bufferedReader().use { input ->
                str = input.readText()
            }
            val coeffs = str.split(",")
            for (i in spO2Coefficients.indices)
                spO2Coefficients[i] = coeffs[i].toFloatOrNull() ?: spO2Coefficients[i]
        }
    }

    fun addNewUser(name: String) {
        if (BptSettings.users.contains(name)) {
            Toast.makeText(
                app,
                app.getString(R.string.username_already_exists),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            BptSettings.users.add(name)
        }
        prepareUserList()
    }

    private fun prepareUserList() {
        _userList.value?.clear()
        _userList.value?.add(app.getString(R.string.select_user))
        if (BptSettings.users.isNotEmpty()) {
            _userList.value?.addAll(BptSettings.users)
            _userList.value = _userList.value
        }
    }

    fun startTimer() {
        if (!timerStarted) {
            startTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            startElapsedTime = SystemClock.elapsedRealtime()
            _elapsedTime.value = SystemClock.elapsedRealtime() - startElapsedTime
            handler.postDelayed(tickRunnable, 1000)
            timerStarted = true
        }
    }

    fun restartTimer() {
        handler.removeCallbacks(tickRunnable)
        startTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        startElapsedTime = SystemClock.elapsedRealtime()
        _elapsedTime.value = SystemClock.elapsedRealtime() - startElapsedTime
        handler.postDelayed(tickRunnable, 1000)
        timerStarted = true
    }

    fun stopTimer() {
        handler.removeCallbacks(tickRunnable)
        timerStarted = false
        startTime = ""
        startElapsedTime = 0
        _elapsedTime.value = 0
        calibrationTimePassed.set(-1)
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (timerStarted) {
                _elapsedTime.value = SystemClock.elapsedRealtime() - startElapsedTime
                handler.postDelayed(this, 1000)
                if (calibrationTimePassed.get() >= 0) {

                    if (calibrationTimePassed.incrementAndGet() == CALIBRATION_TIMEOUT_IN_SEC) {
                        onCalibrationTimeout()
                    }
                }
            } else {
                handler.removeCallbacks(this)
            }
        }
    }

    fun resetDataCollection() {
        _calibrationStates.value = CalibrationStatus.IDLE
    }

    fun startDataCollection() {
        if (!_isMonitoring.value!!) {
            _calibrationStates.value = CalibrationStatus.STARTED
            _isMonitoring.value = true
            startTimer()
        }
    }

    fun stopDataCollection(status: CalibrationStatus = CalibrationStatus.IDLE) {
        _calibrationStates.value = status
        _isMonitoring.value = false
        stopTimer()
        if (status == CalibrationStatus.SUCCESS) {
            calibrationTimePassed.set(-1)
        }
    }

    fun onCalibrationResultsRequested() {
        _calibrationStates.value = CalibrationStatus.PROCESSING
        calibrationTimePassed.set(0)
    }

    /*fun onCalibrationReceived() {
        val pair = _calibrationStates.value
        _calibrationStates.value = Pair(pair?.first ?: 0, CalibrationStatus.SUCCESS)
        calibrationTimePassed.set(-1)
    }*/

    fun onCalibrationTimeout() {
        _calibrationStates.value = CalibrationStatus.FAIL
        calibrationTimePassed.set(-1)
    }

    fun onRefBloodPressureMeasurementStarted() {
        _calibrationStates.value = CalibrationStatus.REF_STARTED
        calibrationTimePassed.set(-1)
    }

    fun startMeasurement() {
        _isMonitoring.value = true
        startTimer()
    }

    fun stopMeasurement() {
        _isMonitoring.value = false
        stopTimer()
    }

    fun isWaitingForCalibrationResults(): Boolean {
        return calibrationStates.value == CalibrationStatus.PROCESSING
    }

    fun refreshUserData() {
        uiScope.launch {
            _historyDataList.value = getHistoryData()
            _calibrationDataList.value = getCalibrationData()
        }
    }

    private suspend fun getHistoryData(): List<BptHistoryData> {
        return withContext(Dispatchers.IO) {
            readHistoryData(app.contentResolver)
        }
    }

    private suspend fun getCalibrationData(): List<BptCalibrationData> {
        return withContext(Dispatchers.IO) {
            readCalibrationData(app.contentResolver)
        }
    }

    fun getValidCalibrationList(): List<BptCalibrationData> {
        return _calibrationDataList.value?.filter { !it.isExpired() }?.takeLast(
            MAX_NUMBER_OF_CALIBRATION
        ) ?: emptyList()
    }
}