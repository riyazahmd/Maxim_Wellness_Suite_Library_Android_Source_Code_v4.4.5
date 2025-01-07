package com.maximintegrated.maximsensorsapp.respiratory

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.algorithms.AlgorithmOutput
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.algorithms.respiratory.RespiratoryRateAlgorithmInitConfig
import com.maximintegrated.bluetooth.ble.BleScannerDialog
import com.maximintegrated.bluetooth.devicelist.OnBluetoothDeviceClickListener
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import com.maximintegrated.maximsensorsapp.exts.set
import com.maximintegrated.maximsensorsapp.view.DataSetInfo
import com.maximintegrated.maximsensorsapp.view.FloatValueFormatter
import com.maximintegrated.maximsensorsapp.view.MultiChannelChartView
import com.maximintegrated.zephyr.ZephyrBreathWaveform
import com.maximintegrated.zephyr.ZephyrSummary
import com.maximintegrated.zephyr.ZephyrViewModel
import kotlinx.android.synthetic.main.include_respiratory_fragment_content.*
import kotlinx.android.synthetic.main.view_multi_channel_chart.view.titleView
import kotlinx.android.synthetic.main.view_result_card.view.*
import timber.log.Timber

class RespiratoryFragment : MeasurementBaseFragment(), OnBluetoothDeviceClickListener {

    companion object {
        fun newInstance() = RespiratoryFragment()
    }

    private lateinit var chartView: MultiChannelChartView

    private var algorithmInitConfig: AlgorithmInitConfig? = null
    private val algorithmInput = AlgorithmInput()
    private val algorithmOutput = AlgorithmOutput()

    private var csvWriterSummaryLog: CsvWriter? = null
    private var csvWriterBreathWaveformLog: CsvWriter? = null

    private var respiration: Float? = null
        set(value) {
            field = value
            if (value != null) {
                respirationResultView.emptyValue = decimalFormat.format(value)
            } else {
                respirationResultView.emptyValue = ResultCardView.EMPTY_VALUE
            }
        }

    private var scd: Int? = null
        set(value) {
            field = value
            if (value != null) {
                respirationResultView.scdStateTextView.text = Scd.values()[value].displayName
            } else {
                respirationResultView.scdStateTextView.text = Scd.NO_DECISION.displayName
            }
        }

    /*private var respirationConfidence: Int? = null
        set(value) {
            field = value
            if (value != null) {
                respirationResultView.confidenceProgressBar.progress = value
            } else {
                respirationResultView.confidenceProgressBar.progress = 0
            }
        }*/

    private lateinit var zephyrViewModel: ZephyrViewModel
    private var bleScannerDialog: BleScannerDialog? = null

    private val summaryObserver = Observer<ZephyrSummary> { summary ->
        zephyrView.zephyrSummary = summary
        zephyrView.bleConnectionInfo =
            zephyrView.bleConnectionInfo?.copy(batteryLevel = summary.batteryLevel)
        csvWriterSummaryLog?.write(summary.toCsvModel())
        notificationResults[REF_KEY] = "REF RR: ${summary.rr} breath/min"
        updateNotification()
        Timber.d(summary.toString())
    }

    private val breathWaveformObserver = Observer<ZephyrBreathWaveform> { waveform ->
        csvWriterBreathWaveformLog?.write(waveform.toCsvModel())
        if(zephyrView != null) {
            for (sample in waveform.data) {
                zephyrView.addData(sample)
            }
        } else {
            Timber.d("Zephyr view null")
        }

        Timber.d(waveform.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_respiratory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chartView = view.findViewById(R.id.chart_view)

        algorithmInitConfig = AlgorithmInitConfig()
        algorithmInitConfig?.respConfig = RespiratoryRateAlgorithmInitConfig(
            RespiratoryRateAlgorithmInitConfig.SourceOptions.WRIST,
            RespiratoryRateAlgorithmInitConfig.LedCodes.GREEN,
            RespiratoryRateAlgorithmInitConfig.SamplingRateOption.Hz_25,
            RespiratoryRateAlgorithmInitConfig.DEFAULT_MOTION_MAGNITUDE_LIMIT
        )
        algorithmInitConfig?.enableAlgorithmsFlag = MaximAlgorithms.FLAG_RESP

        setupChart()
        setupToolbar(getString(R.string.respiratory))
        menuItemSettings.isVisible = false

        setupReferenceDeviceView()
    }

    private fun setupReferenceDeviceView() {
        zephyrViewModel = ViewModelProviders.of(requireActivity()).get(ZephyrViewModel::class.java)

        zephyrViewModel.connectionState.observe(this) { (device, connectionState) ->
            if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
                notificationResults.remove(REF_KEY)
                updateNotification()
            }
            zephyrView.bleConnectionInfo =
                if (zephyrViewModel.bluetoothDevice != null) {
                    BleConnectionInfo(connectionState, device?.name, device?.address)
                } else {
                    null
                }

            checkRefDeviceConnection(zephyrViewModel.bluetoothDevice, connectionState)
        }

        zephyrViewModel.zephyrSummary.observeForever(summaryObserver)
        zephyrViewModel.zephyrBreathWaveform.observeForever(breathWaveformObserver)

        zephyrViewModel.isDeviceSupported.observe(this) {
            if (it) {
                zephyrView.reset()
                zephyrViewModel.enableSummaryCharacteristicNotifications()
                zephyrViewModel.enableTxCharacteristicNotifications()
                zephyrViewModel.startBreathWaveform()
            }
        }

        zephyrView.onSearchButtonClick {
            showBleScannerDialog(R.string.reference_device_title)
        }

        zephyrView.onConnectButtonClick {
            zephyrViewModel.reconnect()
        }

        zephyrView.onDisconnectClick {
            zephyrViewModel.disconnect()
        }

        zephyrView.onChangeDeviceClick {
            zephyrViewModel.disconnect()
            zephyrView.bleConnectionInfo = null
            showBleScannerDialog(R.string.rr_ref_device)
        }

        zephyrView.setupChart()
        zephyrView.reset()
    }

    private fun showBleScannerDialog(@StringRes titleRes: Int, deviceNamePrefix: ArrayList<String>? = null) {
        bleScannerDialog = BleScannerDialog.newInstance(getString(titleRes), deviceNamePrefix)
        bleScannerDialog?.setTargetFragment(this, 1437)
        fragmentManager?.let { bleScannerDialog?.show(it, "BleScannerDialog") }
    }

    private fun setupChart() {
        chartView.dataSetInfoList = listOf(
            DataSetInfo(R.string.respiration_rate, R.color.channel_red)
        )

        chartView.titleView.text = getString(R.string.respiration_rate)
        chartView.maximumEntryCount = 4500
        chartView.setFormatterForYAxis(FloatValueFormatter(decimalFormat))
        chartView.setVisibilityForChipGroup(false)
    }

    override fun addStreamData(streamData: HspStreamData) {

        dataRecorder?.record(streamData)

        algorithmInput.set(streamData)

        val status =
            MaximAlgorithms.run(algorithmInput, algorithmOutput) and MaximAlgorithms.FLAG_RESP
        if (status == 0) {
            if (algorithmOutput.respiratory.respirationRate < 0.01f) {
                algorithmOutput.respiratory.respirationRate = 0f
            }
            chartView.addData(algorithmOutput.respiratory.respirationRate)
            respiration = algorithmOutput.respiratory.respirationRate
            notificationResults[MXM_KEY] =
                "Resp. rate: ${decimalFormat.format(respiration)} breath/min"
            updateNotification()
        }

        scd = streamData.scdState
    }

    override fun getMeasurementType(): String {
        return "Respiration_Rate"
    }

    override fun startMonitoring(): Boolean {
        if(!super.startMonitoring()) return false

        clearChart()
        clearCardViewValues()

        MaximAlgorithms.init(algorithmInitConfig)

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

        csvWriterSummaryLog =
            CsvWriter.open(
                makeCsvFilePath(
                    getDirectoryReference(RR_REF_DIRECTORY_NAME)!!, ZEPHYR_SUMMARY_FILE_NAME,
                    logTimestamp
                ), ZephyrSummary.CSV_HEADER,
                context=requireContext()
            )
        csvWriterBreathWaveformLog =
            CsvWriter.open(
                makeCsvFilePath(
                    getDirectoryReference(RR_REF_DIRECTORY_NAME)!!, ZEPHYR_BREATH_WAVEFORM_FILE_NAME,
                    logTimestamp
                ), ZephyrBreathWaveform.CSV_HEADER,
                context=requireContext()
            )
        return true
    }

    override fun stopMonitoring() {
        super.stopMonitoring()

        MaximAlgorithms.end(MaximAlgorithms.FLAG_RESP)

        hspViewModel.stopStreaming()

        csvWriterSummaryLog?.close()
        csvWriterSummaryLog = null
        csvWriterBreathWaveformLog?.close()
        csvWriterBreathWaveformLog = null
    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {

    }

    override fun showInfoDialog() {
        val helpDialog =
            HelpDialog.newInstance(getString(R.string.resp_info), getString(R.string.info))
        fragmentManager?.let { helpDialog.show(it, "helpDialog") }
    }

    private fun clearChart() {
        chartView.clearChart()
    }

    private fun clearCardViewValues() {
        respiration = null
        scd = null
    }

    override fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        val deviceName = bluetoothDevice.name ?: ""

        when {
            deviceName.startsWith("", false) -> zephyrViewModel.connect(
                bluetoothDevice
            )
        }

        bleScannerDialog?.dismiss()
    }

    override fun onDetach() {
        super.onDetach()
        zephyrViewModel.zephyrSummary.removeObserver(summaryObserver)
        zephyrViewModel.zephyrBreathWaveform.removeObserver(breathWaveformObserver)
        zephyrViewModel.disconnect()
    }

    private fun checkRefDeviceConnection(device: BluetoothDevice?, connectionState: Int?): Boolean {
        if ((device != null) and (connectionState == BluetoothAdapter.STATE_DISCONNECTED)) {
            showAlertDialog(requireContext(),
                getString(R.string.ble_connection_lost_title),
                getString(R.string.ble_connection_ref_device_disconnected_message),
                getString(R.string.ble_connection_right_button_text)
            ) {
                zephyrViewModel.reconnect()
            }
            return false
        }
        return true
    }
}