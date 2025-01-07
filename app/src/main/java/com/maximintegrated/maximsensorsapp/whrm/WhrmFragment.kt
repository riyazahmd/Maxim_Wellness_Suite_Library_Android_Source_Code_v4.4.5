package com.maximintegrated.maximsensorsapp.whrm

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.bluetooth.ble.BleScannerDialog
import com.maximintegrated.bluetooth.devicelist.OnBluetoothDeviceClickListener
import com.maximintegrated.hsp.*
import com.maximintegrated.hsp.protocol.SetConfigurationCommand
import com.maximintegrated.polar.HeartRateMeasurement
import com.maximintegrated.polar.PolarViewModel
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.view.DataSetInfo
import com.maximintegrated.maximsensorsapp.view.MultiChannelChartView
import com.maximintegrated.maximsensorsapp.view.ReferenceDeviceView
import kotlinx.android.synthetic.main.include_whrm_fragment_content.*
import kotlinx.android.synthetic.main.view_measurement_result.view.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.max

class WhrmFragment : MeasurementBaseFragment(), OnBluetoothDeviceClickListener {

    companion object {
        fun newInstance() = WhrmFragment()

        //val HR_MEASURING_PERIOD_IN_MILLIS = TimeUnit.SECONDS.toMillis(13)
        val TIMEOUT_INTERVAL_IN_MILLIS = TimeUnit.SECONDS.toMillis(40)
        val MIN_CYCLE_TIME_IN_MILLIS = TimeUnit.SECONDS.toMillis(60)
    }

    private lateinit var viewReferenceDevice: ReferenceDeviceView

    private lateinit var polarViewModel: PolarViewModel
    private var bleScannerDialog: BleScannerDialog? = null

    private lateinit var chartView: MultiChannelChartView
    //private var dataRecorder: DataRecorder? = null

    private var measurementStartTimestamp: Long? = null
    private var lastValidHrTimestamp: Long = 0L

    private var countDownTimer: CountDownTimer? = null

    private var hrConfidence: Int? = null
        set(value) {
            field = value
//            hrConfidenceView.value = value?.toFloat()
        }

    private var hrReadyToDisplay = false

    private var ibi: String? = null
        set(value) {
            field = value
            ibiView.emptyValue = value ?: ResultCardView.EMPTY_VALUE
        }

    private var stepCount: Int? = null
        set(value) {
            field = value
            stepsView.emptyValue = value.toString()
        }

    private var energy: String? = null
        set(value) {
            field = value
            energyView.emptyValue = value.toString()
        }

    private var activity: String? = null
        set(value) {
            field = value
            activityView.emptyValue = value ?: ResultCardView.EMPTY_VALUE
        }

    private var scd: String? = null
        set(value) {
            field = value
            scdView.emptyValue = value ?: ResultCardView.EMPTY_VALUE
        }

    private var totalActEnergy: String? = null
        set(value) {
            field = value
            totalActEnergyView.emptyValue = value.toString()
        }

    private val heartRateMeasurementObserver =
        androidx.lifecycle.Observer<HeartRateMeasurement> { heartRateMeasurement ->
            dataRecorder?.record(heartRateMeasurement)
            viewReferenceDevice.heartRateMeasurement = heartRateMeasurement
            notificationResults[REF_KEY] = "REF HR: ${heartRateMeasurement.heartRate}bpm"
            updateNotification()
            Timber.d("%s", heartRateMeasurement)
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        radioButtonSampledMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                setupTimer()
            }
        }

        viewReferenceDevice = referenceDeviceView

        setupReferenceDeviceView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_whrm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chartView = view.findViewById(R.id.chart_view)

        setupToolbar(getString(R.string.whrm_toolbar))
        setupChart()
        hrResultView.measuringWarningMessageView.text = ""
    }

    private fun setupChart() {

        val list: ArrayList<DataSetInfo> = arrayListOf()

        for (sensor in hspViewModel.deviceModel.getPpgSensorsToDisplay()) {
            when (sensor) {
                IR -> list.add(DataSetInfo(R.string.channel_ir, R.color.channel_ir))
                RED -> list.add(DataSetInfo(R.string.channel_red, R.color.channel_red))
                GREEN -> list.add(DataSetInfo(R.string.channel_green, R.color.channel_green))
                GREEN2 -> list.add(DataSetInfo(R.string.channel_green2, R.color.channel_green2))
            }
        }
        chartView.dataSetInfoList = list.toList()
        chartView.maximumEntryCount = 100
    }

    override fun sendAlgoMode() {
        if (radioButtonNormalMode.isChecked) {
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "algomode", "2"))
        } else if (radioButtonSampledMode.isChecked) {
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "algomode", "3"))
        }
    }

    override fun isMonitoringChanged() {
        hrResultView.isMeasuring = isMonitoring
    }



    override fun startMonitoring(): Boolean {
        if(!super.startMonitoring()) return false

        clearChart()

        countDownTimer?.cancel()
        if (radioButtonSampledMode.isChecked) {
            countDownTimer?.start()
        }

        hrReadyToDisplay = false

        measurementStartTimestamp = null
        hrResultView.measurementProgress = 0 //getMeasurementProgress()
        hrResultView.result = null

        setAlgorithmModeRadioButtonsEnabled(false)

        hspViewModel.isDeviceSupported
            .observe(this) {
                sendDefaultSettings()
                sendAlgoMode()
                sendLogToFlashCommand()
                hspViewModel.startStreaming(
                    DeviceSettings.scdEnabled,
                    DeviceSettings.lowPowerEnabled
                )
            }
        return true
    }

    override fun sendScdStateMachineIfRequired() {
        if (radioButtonNormalMode.isChecked) {
            super.sendScdStateMachineIfRequired()
        }
    }


    fun setupTimer() {
        val timeInterval = max(WhrmSettings.sampledModeTimeInterval, MIN_CYCLE_TIME_IN_MILLIS)
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(timeInterval, 1000) {
            override fun onFinish() {
                startMonitoring()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
        if (isMonitoring && radioButtonSampledMode.isChecked) {
            countDownTimer?.start()
        }
    }

    override fun stopMonitoring() {
        super.stopMonitoring()

        setAlgorithmModeRadioButtonsEnabled(true)

        hspViewModel.stopStreaming()
        polarViewModel.disconnect()
    }

    private fun setAlgorithmModeRadioButtonsEnabled(isEnabled: Boolean) {
        for (radioButton in algorithmModeRadioGroup.children) {
            radioButton.isEnabled = isEnabled
        }
    }

    override fun addStreamData(streamData: HspStreamData) {
        notificationResults[MXM_KEY] = "Maxim HR: ${streamData.hr}bpm"
        updateNotification()
        renderHrmModel(streamData)
        dataRecorder?.record(streamData)
        stepCount = streamData.runSteps + streamData.walkSteps

        if (streamData.rr != 0f) {
            ibi = "${streamData.rr} msec"
        }
        energy = "${streamData.kCal} kcal"
        activity = Activity.values()[streamData.activity].displayName
        scd = Scd.values()[streamData.scdState].displayName
        totalActEnergy = "${streamData.totalActEnergy} kcal"
    }

    private fun setupReferenceDeviceView() {
        polarViewModel = ViewModelProviders.of(requireActivity()).get(PolarViewModel::class.java)

        polarViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
                    notificationResults.remove(REF_KEY)
                    updateNotification()
                }
                viewReferenceDevice.bleConnectionInfo =
                    if (polarViewModel.bluetoothDevice != null) {
                        BleConnectionInfo(connectionState, device?.name, device?.address)
                    } else {
                        null
                    }

                checkRefDeviceConnection(polarViewModel.bluetoothDevice, connectionState)
            }

        polarViewModel.heartRateMeasurement
            .observeForever(heartRateMeasurementObserver)

        polarViewModel.isDeviceSupported
            .observe(this) {
                polarViewModel.readBatteryLevel()
            }

        viewReferenceDevice.onSearchButtonClick {
            showBleScannerDialog(R.string.polar_devices)
        }

        viewReferenceDevice.onConnectButtonClick {
            polarViewModel.reconnect()
        }

        viewReferenceDevice.onDisconnectClick {
            polarViewModel.disconnect()
        }

        viewReferenceDevice.onChangeDeviceClick {
            polarViewModel.disconnect()
            viewReferenceDevice.bleConnectionInfo = null
            showBleScannerDialog(R.string.polar_devices)
        }
    }

    private fun showBleScannerDialog(@StringRes titleRes: Int, deviceNamePrefix: ArrayList<String>? = null) {
        bleScannerDialog = BleScannerDialog.newInstance(getString(titleRes), deviceNamePrefix)
        bleScannerDialog?.setTargetFragment(this, 1437)
        fragmentManager?.let { bleScannerDialog?.show(it, "BleScannerDialog") }
    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {
        val whrmSettingsDialog = WhrmSettingsFragmentDialog.newInstance()
        whrmSettingsDialog.setTargetFragment(this, 1560)
        fragmentManager?.let { whrmSettingsDialog.show(it, "whrmSettingsDialog") }
    }

    override fun showInfoDialog() {
        val helpDialog =
            HelpDialog.newInstance(getString(R.string.whrm_info), getString(R.string.info))
        fragmentManager?.let { helpDialog.show(it, "helpDialog") }
    }

    private fun clearChart() {
        chartView.clearChart()
    }

    private fun clearCardViewValues() {
        hrConfidence = null
        ibi = null
        stepCount = null
        energy = null
        activity = null
        scd = null
        totalActEnergy = null
    }

    /*private fun shouldShowMeasuringProgress(): Boolean {
        return (System.currentTimeMillis() - (measurementStartTimestamp
            ?: 0L)) < HR_MEASURING_PERIOD_IN_MILLIS
    }

    private fun getMeasurementProgress(): Int {
        return ((System.currentTimeMillis() - (measurementStartTimestamp
            ?: 0L)) * 100 / HR_MEASURING_PERIOD_IN_MILLIS).toInt()
    }*/

    private fun renderHrmModel(streamData: HspStreamData) {
        if (measurementStartTimestamp == null) {
            measurementStartTimestamp = System.currentTimeMillis()
        }

        //NOTE: These values are ir & red for ME11A
        chartView.addData(streamData.green, streamData.green2)

        if (radioButtonNormalMode.isChecked) {
            hrConfidence = streamData.hrConfidence
            if (hrReadyToDisplay) {
                hrResultView.result = streamData.hr
                if (isHrConfidenceHighEnough(streamData)) {
                    lastValidHrTimestamp = System.currentTimeMillis()
                    hrConfidenceWarningView.visibility = View.INVISIBLE
                } else if (isHrObsolete()) {
                    hrConfidenceWarningView.visibility = View.VISIBLE
                }
            } else {
                hrResultView.result = null
                if (streamData.hrConfidence >= 50) {
                    hrReadyToDisplay = true
                }
            }
        } else {
            hrResultView.result = streamData.hr
            if (streamData.hrConfidence == 100) {
                stopMonitoring()
            } else if (System.currentTimeMillis() - measurementStartTimestamp!! >= TIMEOUT_INTERVAL_IN_MILLIS) {
                hrResultView.result = null
                Toast.makeText(context, "TIME OUT", Toast.LENGTH_SHORT).show()
                stopMonitoring()
            }
        }
    }

    private fun isHrConfidenceHighEnough(hrmModel: HspStreamData): Boolean {
        return if (hrmModel.hr < 40 || hrmModel.hr > 240) {
            false
        } else {
            hrmModel.hrConfidence >= WhrmSettings.minConfidenceLevel
        }
    }

    private fun isHrObsolete(): Boolean {
        return (System.currentTimeMillis() - lastValidHrTimestamp) > TimeUnit.SECONDS.toMillis(
            WhrmSettings.confidenceThresholdInSeconds.toLong()
        )
    }

    override fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        val deviceName = bluetoothDevice.name ?: ""

        when {
            deviceName.startsWith("", false) -> polarViewModel.connect(
                bluetoothDevice
            )
        }

        bleScannerDialog?.dismiss()
    }

    override fun onDetach() {
        super.onDetach()
        countDownTimer?.cancel()
        polarViewModel.heartRateMeasurement.removeObserver(heartRateMeasurementObserver)
    }

    private fun checkRefDeviceConnection(device: BluetoothDevice?, connectionState: Int?): Boolean {
        if ((device != null) and (connectionState == BluetoothAdapter.STATE_DISCONNECTED)) {
            showAlertDialog(requireContext(),
                getString(R.string.ble_connection_lost_title),
                getString(R.string.ble_connection_ref_device_disconnected_message),
                getString(R.string.ble_connection_right_button_text)
            ) {
                polarViewModel.reconnect()
            }
            return false
        }
        return true
    }
}