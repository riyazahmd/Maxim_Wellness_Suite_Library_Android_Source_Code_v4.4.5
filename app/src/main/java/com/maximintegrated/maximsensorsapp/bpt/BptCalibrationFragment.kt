package com.maximintegrated.maximsensorsapp.bpt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import com.maximintegrated.hsp.*
import com.maximintegrated.hsp.protocol.HspCommand
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.bpt.BptStatus.*
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import com.maximintegrated.maximsensorsapp.view.DataSetInfo
import com.maximintegrated.qardio.BloodPressureMeasurement
import com.maximintegrated.qardio.QardioViewModel
import kotlinx.android.synthetic.main.fragment_bpt_calibration.*
import kotlinx.android.synthetic.main.include_app_bar.*
import kotlinx.android.synthetic.main.include_bpt_calibration_fragment_content.*
import kotlinx.android.synthetic.main.include_bpt_calibration_warning_fragment_content.*
import kotlinx.android.synthetic.main.view_calibration_card.view.*
import kotlinx.android.synthetic.main.view_multi_channel_chart.view.*
import timber.log.Timber
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

class BptCalibrationFragment : Fragment(), IOnBackPressed {
    companion object {
        fun newInstance(): BptCalibrationFragment {
            return BptCalibrationFragment()
        }

        const val NUMBER_OF_DATA_TO_DISCARD = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_bpt_calibration, container, false)

    private lateinit var hspViewModel: HspViewModel
    private lateinit var bptViewModel: BptViewModel
    private lateinit var qardioViewModel: QardioViewModel

    private var csvWriter: CsvWriter? = null
    private var csvWriterBloodPressureMeasurement: CsvWriter? = null
    private var timestampQardio: String? = null
    private val irMovingAverage = MovingAverage(3)
    private val greenMovingAverage = MovingAverage(3)
    private lateinit var statusArray: Array<String>

    private var dataCount = 0

    private var showWarning = false
        set(value) {
            field = value
            chartView.isInvisible = value
            warningLayout.isVisible = value
        }

    private val bloodPressureMeasurementObserver =
        Observer<BloodPressureMeasurement> { bloodPressureMeasurement ->
            if (bloodPressureMeasurement.hasFailed) {
                stopReferenceBloodPressureMeasurement()
                showBloodPressureMeasurementFailure()
            } else if (bloodPressureMeasurement.hasCompleted) {
                csvWriterBloodPressureMeasurement?.write(bloodPressureMeasurement.toCsvModel())
                calibrationCardView.sbp = bloodPressureMeasurement.systolic.roundToInt()
                calibrationCardView.dbp = bloodPressureMeasurement.diastolic.roundToInt()
                stopReferenceBloodPressureMeasurement()
                startMonitoring(calibrationCardView)
            }
            qardioViewCalibrationFragment.bloodPressureMeasurement = bloodPressureMeasurement
            Timber.d("%s", bloodPressureMeasurement)
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusArray = resources.getStringArray(R.array.bpt_algorithm_out_status)

        hspViewModel = ViewModelProviders.of(requireActivity()).get(HspViewModel::class.java)

        bptViewModel = ViewModelProviders.of(requireActivity()).get(BptViewModel::class.java)

        qardioViewModel = ViewModelProviders.of(requireActivity()).get(QardioViewModel::class.java)

        calibrationCardView.isAutoReference = qardioViewModel.isConnected

        hspViewModel.connectionState.observe(this) { (device, connectionState) ->
            toolbar.connectionInfo = if (hspViewModel.bluetoothDevice != null) {
                BleConnectionInfo(connectionState, device?.name, device?.address)
            } else {
                null
            }

            checkDeviceConnection(connectionState)
        }

        hspViewModel.commandResponse.observe(this, Observer {
            if (it.command.name == HspCommand.COMMAND_GET_CFG && it.command.parameters[0].value == "bpt") {
                if (it.command.parameters.size >= 2 && it.command.parameters[1].value == "cal_result") {
                    saveCalibrationData(
                        it.parameters[0].value,
                        Date().time,
                        calibrationCardView.sbp,
                        calibrationCardView.dbp,
                        requireContext().contentResolver
                    )
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.calibration_completed),
                        Toast.LENGTH_SHORT
                    ).show()
                    //bptViewModel.onCalibrationReceived()
                    stopMonitoring(CalibrationStatus.SUCCESS)
                }
            }
        })

        toolbar.apply {
            inflateMenu(R.menu.bpt_menu)
            menu.findItem(R.id.monitoring_start).isVisible = false
            menu.findItem(R.id.monitoring_start).isEnabled = false
            title = getString(R.string.bp_trending)
            pageTitle = getString(R.string.bp_trending)
        }

        bptViewModel.elapsedTime.observe(this) {
            calibrationCircleProgressView.setValue(it / 1000f)
            calibrationCircleProgressView.setText(getFormattedTime(it))
            if (bptViewModel.startTime == "") {
                toolbar.subtitle = getFormattedTime(it)
            } else {
                toolbar.subtitle = "Start Time: ${bptViewModel.startTime} - ${getFormattedTime(it)}"
            }
        }

        bptViewModel.calibrationStates.observe(this) {
            if (it == null) return@observe
            calibrationCardView.status = it
        }

        setupCalibrationView(calibrationCardView)
        setupQardioView()
        setupChart()

        restartTimerButton.setOnClickListener {
            bptViewModel.restartTimer()
        }

        goToCalibrationButton.setOnClickListener {
            bptViewModel.stopTimer()
            calibrationWarningLayout.isVisible = false
            calibrationLayout.isVisible = true
            if (calibrationCardView.isAutoReference) {
                qardioViewCalibrationFragment.isVisible = true
                initCsvWriterBloodPressureMeasurement()
                startReferenceBloodPressureMeasurement()
            }
        }

        bptViewModel.startTimer()
        bptViewModel.resetDataCollection()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onDetach() {
        super.onDetach()
        hspViewModel.bptStreamData.removeObserver(dataStreamObserver)
        qardioViewModel.bloodPressureMeasurement.removeObserver(bloodPressureMeasurementObserver)
    }

    private fun setupCalibrationView(view: CalibrationView) {
        view.calibrationButton.setOnClickListener {
            when (view.calibrationButton.text) {
                getString(R.string.start) -> startMonitoring(view)
                getString(R.string.stop) -> onStopMonitoring()
                getString(R.string.repeat) -> {
                    onStopMonitoring()
                    startReferenceBloodPressureMeasurement()
                }
                getString(R.string.done) -> {
                    onStopMonitoring()
                    requireActivity().onBackPressed()
                }
            }
        }
        view.repeatButton.setOnClickListener {
            var tag = view.tag.toString().toIntOrZero()
            tag += 1
            view.tag = tag
            startMonitoring(view)

        }
        view.newButton.setOnClickListener {
            view.reset()
            if (view.isAutoReference) {
                stopMonitoring()
                startReferenceBloodPressureMeasurement()
            } else {
                bptViewModel.resetDataCollection()
            }
            var tag = view.tag.toString().toIntOrZero()
            tag += 1
            view.tag = tag
        }
    }

    private fun setupChart() {
        chartView.titleView.isVisible = false

        val list: ArrayList<DataSetInfo> = arrayListOf()
        for (sensor in hspViewModel.deviceModel.getPpgSensorsToDisplay()) {
            when (sensor) {
                RED -> list.add(DataSetInfo(R.string.ppg_signal, R.color.channel_red))
                GREEN -> list.add(DataSetInfo(R.string.channel_green, R.color.channel_green))
            }
        }
        chartView.dataSetInfoList = list.toList()

        if (hspViewModel.deviceModel == ME15) {
            chartView.changeCheckStateOfTheChip(0, false)
        }

        chartView.maximumEntryCount = NUMBER_OF_SAMPLES_FOR_CHART
        chartView.line_chart_view.axisLeft.isEnabled = false
    }

    /**
     * Creates a new CSV file and initializes CsvWriter with created file given as a parameter.
     */
    private fun initCsvWriter() {
        val timestamp = if (calibrationCardView.isAutoReference) {
            timestampQardio.toString()
        } else {
            HspBptStreamData.TIMESTAMP_FORMAT.format(Date())
        }
        var userDirectory =
            getDirectoryReference(BPT_DIRECTORY_NAME)!!.findFile(BptSettings.currentUser)

        if (userDirectory == null) {
            userDirectory =
                getDirectoryReference(BPT_DIRECTORY_NAME)!!.createDirectory(BptSettings.currentUser)
        }

        var file =
            userDirectory!!.findFile("${BASE_BPT_FILE_NAME_PREFIX}${timestamp}${BPT_CALIBRATION_SUFFIX}.csv")
        if (file == null) {
            file = userDirectory.createFile(
                "text/csv",
                "${BASE_BPT_FILE_NAME_PREFIX}${timestamp}${BPT_CALIBRATION_SUFFIX}.csv"
            )
        }
        csvWriter = CsvWriter.open(
            file!!.uri,
            HspBptStreamData.CSV_HEADER_ARRAY,
            context = requireContext()
        )
    }

    private fun initCsvWriterBloodPressureMeasurement() {
        timestampQardio = HspBptStreamData.TIMESTAMP_FORMAT.format(Date())
        var userDirectory =
            getDirectoryReference(BPT_DIRECTORY_NAME)!!.findFile(BptSettings.currentUser)

        if (userDirectory == null) {
            userDirectory =
                getDirectoryReference(BPT_DIRECTORY_NAME)!!.createDirectory(BptSettings.currentUser)
        }

        var refFile =
            userDirectory!!.findFile("${BASE_BPT_FILE_NAME_PREFIX}${timestampQardio}_${QARDIO_RESULT_FILE_NAME}${BPT_CALIBRATION_SUFFIX}.csv")
        if (refFile == null) {
            refFile = userDirectory.createFile(
                "text/csv",
                "${BASE_BPT_FILE_NAME_PREFIX}${timestampQardio}_${QARDIO_RESULT_FILE_NAME}${BPT_CALIBRATION_SUFFIX}.csv"
            )
        }

        csvWriterBloodPressureMeasurement = CsvWriter.open(
            refFile!!.uri,
            BloodPressureMeasurement.CSV_HEADER,
            context = requireContext()
        )

    }

    private val dataStreamObserver = Observer<HspBptStreamData> { data ->
        if (bptViewModel.isMonitoring.value == false || bptViewModel.isWaitingForCalibrationResults()) return@Observer
        addStreamData(data)
    }

    private fun startMonitoring(view: CalibrationView) {
        if (view.sbp == 0 || view.dbp == 0) {
            Toast.makeText(
                requireContext(),
                getString(R.string.ref_bp_required_fields_warning),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            startMonitoring(
                view.tag.toString().toInt(),
                view.sbp,
                view.dbp
            )
        }
    }

    private fun startMonitoring(
        index: Int,
        sbp: Int,
        dbp: Int
    ) {
        if (!checkDeviceConnection(hspViewModel.connectionState.value?.second)) return
        if (bptViewModel.isMonitoring.value!!) return
        chartView.clearChart()
        bptViewModel.startDataCollection()
        initCsvWriter()
        hspViewModel.streamType = HspViewModel.StreamType.BPT
        hspViewModel.bptStreamData.observeForever(dataStreamObserver)
        hspViewModel.setBptDateTime()
        hspViewModel.setSysDiaBpValues(index, sbp, dbp)
        hspViewModel.setSpO2Coefficients(
            bptViewModel.spO2Coefficients[0],
            bptViewModel.spO2Coefficients[1],
            bptViewModel.spO2Coefficients[2]
        )
        hspViewModel.startBptCalibrationStreaming()
        showWarning = false
        signalTextView.text = ""
        prevStatus = null
    }

    private fun stopMonitoring(status: CalibrationStatus = CalibrationStatus.IDLE) {
        if (!bptViewModel.isMonitoring.value!!) return
        hspViewModel.stopStreaming()
        hspViewModel.bptStreamData.removeObserver(dataStreamObserver)
        bptViewModel.stopDataCollection(status)
        csvWriter?.close()
        csvWriter = null
        dataCount = 0
    }

    private fun startReferenceBloodPressureMeasurement() {
        qardioViewCalibrationFragment.reset()
        calibrationCardView.reset()
        qardioViewModel.resetBloodPressureMeasurement()
        if (qardioViewModel.isConnected) {
            qardioViewModel.bloodPressureMeasurement.observeForever(bloodPressureMeasurementObserver)
            qardioViewModel.startMeasurement()
            bptViewModel.onRefBloodPressureMeasurementStarted()
        }
    }

    private fun stopReferenceBloodPressureMeasurement() {
        qardioViewModel.stopMeasurement()
        qardioViewModel.bloodPressureMeasurement.removeObserver(bloodPressureMeasurementObserver)
        csvWriterBloodPressureMeasurement?.close()
        csvWriterBloodPressureMeasurement = null
    }

    private fun addStreamData(data: HspBptStreamData) {
        dataCount++
        if (dataCount <= NUMBER_OF_DATA_TO_DISCARD) {
            return
        }
        csvWriter?.write(data.toCsvModel())
        irMovingAverage.add(data.irCnt)
        if (hspViewModel.deviceModel == ME15) {
            greenMovingAverage.add(data.green)
            chartView.addData(irMovingAverage.average(), greenMovingAverage.average())
        } else if (hspViewModel.deviceModel == ME11D) {
            chartView.addData(irMovingAverage.average())
        }
        progressBar.progress = data.progress
        progressTextView.text = getString(R.string.percent_format, data.progress.toString())
        hrTextView.text = data.hr.toString()
        spo2TextView.text = data.spo2.toString()
        onStatusChanged(data)
    }

    override fun onBackPressed(): Boolean {
        return (bptViewModel.isMonitoring.value ?: false) or (qardioViewModel.isMeasuring.value
            ?: false)
    }

    override fun onStopMonitoring() {
        stopMonitoring()
        stopReferenceBloodPressureMeasurement()
    }

    private var prevStatus: BptStatus? = null

    private fun onStatusChanged(data: HspBptStreamData) {
        val flag = min(RESERVED.ordinal, data.status)
        val status = BptStatus.fromInt(flag)
        if (status == prevStatus) {
            return
        } else if (prevStatus == NO_CONTACT_ERROR) {
            showWarning = false
        }
        prevStatus = status
        if (status != CAL_SEGMENT_DONE) {
            signalTextView.text = statusArray[data.status]
        }
        when (status) {
            SUCCESS -> {
                showWarning = false
                hspViewModel.stopStreaming()
                hspViewModel.getBptCalibrationResults()
                bptViewModel.onCalibrationResultsRequested()
                data.sbp = calibrationCardView.sbp
                data.dbp = calibrationCardView.dbp
                val historyData = data.toHistoryModel(true)
                saveHistoryData(historyData, requireContext().contentResolver)
            }
            FAILURE -> stopMonitoring(CalibrationStatus.FAIL)
            INIT_SUBJECT_FAILURE -> showWarningMessage(
                getString(R.string.subject_failure_error),
                getString(R.string.subject_failure_error_message),
                true
            )
            INIT_CAL_REF_BP_TRENDING_ERROR -> showWarningMessage(
                getString(R.string.trending_error),
                getString(R.string.trending_error_message),
                true
            )
            INIT_CAL_REF_BP_INCONSISTENCY_ERROR_1 -> showWarningMessage(
                getString(R.string.inconsistency_error),
                getString(R.string.inconsistency_error_1),
                true
            )
            INIT_CAL_REF_BP_INCONSISTENCY_ERROR_2 -> showWarningMessage(
                getString(R.string.inconsistency_error),
                getString(R.string.inconsistency_error_2),
                true
            )
            INIT_CAL_REF_BP_INCONSISTENCY_ERROR_3 -> showWarningMessage(
                getString(R.string.inconsistency_error),
                getString(R.string.inconsistency_error_3),
                true
            )
            INIT_CAL_REF_CNT_MISMATCH -> showWarningMessage(
                getString(R.string.ref_count_mismatch_error),
                getString(R.string.ref_count_mismatch_error_message),
                true
            )
            INIT_CAL_REF_OUT_OF_LIMIT -> showWarningMessage(
                getString(R.string.ref_out_of_limit_error),
                getString(R.string.ref_out_of_limit_error_message),
                true
            )
            INIT_CAL_REF_MAXNUM_ERROR -> showWarningMessage(
                getString(R.string.ref_max_num_error),
                getString(R.string.ref_max_num_error_message),
                true
            )
            PP_OUT_OF_RANGE_ERROR -> showWarningMessage(
                getString(R.string.pp_out_of_range_error),
                getString(R.string.pp_out_of_range_error_message),
                true
            )
            HR_OUT_OF_RANGE -> showWarningMessage(
                getString(R.string.hr_out_of_range_error),
                getString(R.string.hr_out_of_range_error_message),
                true
            )
            HR_ABOVE_RESTING -> showWarningMessage(
                getString(R.string.hr_above_resting_warning),
                getString(R.string.hr_above_resting_warning_message),
                false
            )
            PI_OUT_OF_RANGE -> showWarningMessage(
                getString(R.string.pi_out_of_range_error),
                getString(R.string.pi_out_of_range_error_message),
                true
            )
            ESTIMATION_ERROR -> showWarningMessage(
                getString(R.string.estimation_error),
                getString(R.string.estimation_error_message),
                true
            )
            OUT_OF_RANGE_ERROR -> showWarningMessage(
                getString(R.string.bp_out_of_range_error),
                getString(R.string.bp_out_of_range_error_message),
                true
            )
            OUT_OF_LIMIT_ERROR -> showWarningMessage(
                getString(R.string.bp_out_of_limit_error),
                getString(R.string.bp_out_of_limit_error_message),
                true
            )
            NO_CONTACT_ERROR -> {
                showWarningMessage(
                    getString(R.string.no_contact_error),
                    getString(R.string.no_contact_error_message),
                    false
                )
            }
            NO_FINGER_ERROR -> {
                showWarningMessage(
                    getString(R.string.no_finger_error),
                    getString(R.string.no_finger_error_message),
                    true
                )
            }
            RESERVED -> {
                showWarningMessage(
                    "Unknown Error",
                    "Unknown Message",
                    true
                )
            }
            else -> {

            }
        }
    }

    private fun showWarningMessage(title: String, message: String, abort: Boolean) {
        bpWarningTitleTextView.text = title
        bpWarningMessageTextView.text = message
        bpWarningSkipButton.isVisible = !abort
        showWarning = true
        if (abort) {
            stopMonitoring(CalibrationStatus.FAIL)
            bpWarningSkipButton.text = getString(R.string.restart)
            bpWarningSkipButton.isVisible = true
            bpWarningStopButton.isVisible = false
            bpWarningSkipButton.setOnClickListener {
                startMonitoring(calibrationCardView)
            }
        } else {
            bpWarningSkipButton.text = getString(R.string.skip_now)
            bpWarningStopButton.text = getString(R.string.stop_calibration)
            bpWarningSkipButton.isVisible = true
            bpWarningStopButton.isVisible = true
            bpWarningStopButton.setOnClickListener {
                stopMonitoring()
                showWarning = false
            }
            bpWarningSkipButton.setOnClickListener {
                showWarning = false
            }
        }
    }

    private fun setupQardioView() {
        qardioViewCalibrationFragment.setOnlyDisplay()
        qardioViewCalibrationFragment.reset()
        qardioViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                checkRefDeviceConnection(qardioViewModel.bluetoothDevice, connectionState)
            }
    }


    private fun showBloodPressureMeasurementFailure() {
        val snackbar = this.view?.let {
            Snackbar.make(
                it,
                R.string.bpt_ref_measurement_failure_message,
                Snackbar.LENGTH_INDEFINITE
            )
        }

        snackbar?.setAction(R.string.bpt_ref_measurement_failure_action) {
            snackbar.dismiss()
            startReferenceBloodPressureMeasurement()
        }

        snackbar?.show()
    }

    private fun checkDeviceConnection(connectionState: Int?): Boolean {
        if (connectionState == BluetoothAdapter.STATE_DISCONNECTED) {
            showAlertDialog(
                requireContext(),
                getString(R.string.ble_connection_lost_title),
                getString(R.string.ble_connection_device_disconnected_message),
                getString(R.string.ble_connection_left_button_text)
            ) {
                stopMonitoring()
                startActivity(
                    Intent(requireActivity(), ScannerActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
            }
            return false
        }
        return true
    }

    private val restartQardioConnectionObserver =
        Observer<Pair<BluetoothDevice?, Int>> { (device, connectionState) ->
            if (connectionState == BluetoothAdapter.STATE_CONNECTED) {
                stopReferenceBloodPressureMeasurement()
                startReferenceBloodPressureMeasurement()
                removeRestartQardioConnectionObserver()
            }
        }

    private fun removeRestartQardioConnectionObserver() {
        qardioViewModel.connectionState.removeObserver(restartQardioConnectionObserver)
    }

    private fun checkRefDeviceConnection(device: BluetoothDevice?, connectionState: Int?): Boolean {
        if ((device != null) and (connectionState == BluetoothAdapter.STATE_DISCONNECTED)) {
            showCustomAlertDialog(requireContext(),
                getString(R.string.ble_connection_lost_title),
                getString(R.string.ble_connection_ref_device_disconnected_message),
                getString(R.string.ble_connection_right_button_text),
                getString(R.string.ble_connection_back_button_text), {
                    qardioViewModel.reconnect()
                    if (qardioViewModel.isMeasuring?.value == true) {
                        qardioViewModel.connectionState.observeForever(
                            restartQardioConnectionObserver
                        )
                    }
                }, {
                    onBackPressed()
                })
            return false
        }
        return true
    }

}