package com.maximintegrated.maximsensorsapp.spo2

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.bluetooth.ble.BleScannerDialog
import com.maximintegrated.bluetooth.devicelist.OnBluetoothDeviceClickListener
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.hsp.protocol.SetConfigurationCommand
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import com.maximintegrated.maximsensorsapp.exts.color
import com.maximintegrated.maximsensorsapp.profile.DEFAULT_USER_NAME
import com.maximintegrated.maximsensorsapp.view.DataSetInfo
import com.maximintegrated.maximsensorsapp.view.MultiChannelChartView
import com.maximintegrated.maximsensorsapp.view.NoninView
import com.maximintegrated.nonin.NoninViewModel
import com.maximintegrated.nonin.PlxMeasurement
import kotlinx.android.synthetic.main.include_spo2_fragment_content.*
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt


class Spo2Fragment : MeasurementBaseFragment(), OnBluetoothDeviceClickListener {

    companion object {
        fun newInstance() = Spo2Fragment()
        const val STATUS_COMPLETED = 2
        const val STATUS_TIMEOUT = 3
    }

    private lateinit var chartView: MultiChannelChartView
    private lateinit var noninView: NoninView
    private lateinit var noninViewModel: NoninViewModel
    private var bleScannerDialog: BleScannerDialog? = null

    private var measurementStartTimestamp: Long? = null

    private var csvWriterPlxMeasurementLog: CsvWriter? = null

    private var csvWriterIDFile: CsvWriter? = null
    val USER_INFO_CSV_HEADER = arrayOf(
        "id",
        "name",
        "surname",
        "birthYear",
        "weight",
        "height",
        "hemoglobin",
        "gender",
        "timestamp"
    )

    private var rResult: Float? = null
        set(value) {
            field = value
            if (value != null) {
                rResultView.emptyValue = value.toString()
            } else {
                rResultView.emptyValue = ResultCardView.EMPTY_VALUE
            }
        }

    private val plxMeasurementObserver =
        androidx.lifecycle.Observer<PlxMeasurement> { plxMeasurement ->
            noninView.plxMeasurement = plxMeasurement
            csvWriterPlxMeasurementLog?.write(plxMeasurement.toCsvModel())
            notificationResults[REF_KEY] = "REF SPO2: ${plxMeasurement.spo2Normal}%"
            updateNotification()
            Timber.d("%s", plxMeasurement)
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupReferenceDeviceView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_spo2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        fillInputs()

        chartView = view.findViewById(R.id.chart_view)
        noninView = view.findViewById(R.id.noninView)

        algorithmModeOneShotRadioButton.isChecked = true

        setupChart()
        setupToolbar(getString(R.string.spo2))

        menuItemLogUserInfo.isVisible = true
        if (menuItemLogUserInfo.isChecked) {
            cardViewInputs.visibility = VISIBLE
        } else {
            cardViewInputs.visibility = GONE
        }

        signalQualityChip.setOnClickListener {
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            alertDialog.setTitle(getString(R.string.signalQuality))
            alertDialog.setMessage(getString(R.string.signal_quality_warning))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }
            alertDialog.show()
        }

        motionChip.setOnClickListener {
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            alertDialog.setTitle(getString(R.string.motion))
            alertDialog.setMessage(getString(R.string.motion_warning))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }
            alertDialog.show()
        }

        orientationChip.setOnClickListener {
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            alertDialog.setTitle(getString(R.string.orientation))
            alertDialog.setMessage(getString(R.string.motion_warning))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }
            alertDialog.show()
        }

        menuItemSettings.isVisible = false
    }

    private fun setupChart() {
        chartView.dataSetInfoList = listOf(
            DataSetInfo(R.string.channel_ir, R.color.channel_ir),
            DataSetInfo(R.string.channel_red, R.color.channel_red)
        )

        chartView.maximumEntryCount = 100
        chartView.changeCheckStateOfTheChip(0, false)
    }

    override fun getMeasurementType(): String {
        return "SpO2"
    }

    override fun startMonitoring(): Boolean {
        var ref_filename = SPO2_REF_FILE_NAME
        if (menuItemLogUserInfo.isChecked) {
            if (checkInputs()) {
                setFileNameAppendix("_${IDEditText.text}")
                ref_filename += "_${IDEditText.text}"
            } else {
                return false
            }
        } else {
            setFileNameAppendix(null)
        }
        if(!super.startMonitoring()) return false

        clearChart()
        clearCardViewValues()

        measurementStartTimestamp = null
        hrResultView.measurementProgress = 0
        hrResultView.result = null

        spo2ResultView.isMeasuring = true
        spo2ResultView.result = null
        spo2ResultView.isTimeout = false
        spo2ResultView.showProgressTogetherWithResult = algorithmModeOneShotRadioButton.isChecked
        spo2ResultView.continuousMode = algorithmModeContinuousRadioButton.isChecked

        setAlgorithmModeRadioButtonsEnabled(false)

        csvWriterPlxMeasurementLog =
            CsvWriter.open(
                makeCsvFilePath(
                    getDirectoryReference(SPO2_REF_DIRECTORY_NAME)!!,
                    ref_filename,
                    logTimestamp
                ), PlxMeasurement.CSV_HEADER,
                context = requireContext()
            )

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
        if (algorithmModeContinuousRadioButton.isChecked) {
            super.sendScdStateMachineIfRequired()
        }
    }

    override fun sendDefaultSettings() {
        super.sendDefaultSettings()
        hspViewModel.sendCommand(
            SetConfigurationCommand("wearablesuite", "spo2ledpdconfig", "1020")
        )
    }

    override fun sendAlgoMode() {
        if (algorithmModeContinuousRadioButton.isChecked) {
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "algomode", "0"))
        } else if (algorithmModeOneShotRadioButton.isChecked) {
            hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "algomode", "1"))
        }
    }

    override fun stopMonitoring() {
        super.stopMonitoring()

        spo2ResultView.isMeasuring = false

        setAlgorithmModeRadioButtonsEnabled(true)

        hspViewModel.stopStreaming()

        csvWriterPlxMeasurementLog?.close()
        csvWriterPlxMeasurementLog = null

        if (menuItemLogUserInfo.isChecked) {
            csvWriterIDFile =
                CsvWriter.open(
                    getDirectoryReference(USER_INFO_DIRECTORY_NAME)!!.createFile(
                        "text/csv", "${IDEditText.text.toString()}_${
                            FILE_TIMESTAMP_FORMAT.format(logTimestamp)
                        }.csv"
                    )?.uri!!, USER_INFO_CSV_HEADER,
                    context = requireContext()
                )
            csvWriterIDFile?.write(userInfoToCsvModel())
            csvWriterIDFile?.close()
            csvWriterIDFile = null
        }

        signalQualityChip.background.setTint(requireContext().color(R.color.channel_ir))
        motionChip.background.setTint(requireContext().color(R.color.channel_ir))
        orientationChip.background.setTint(requireContext().color(R.color.channel_ir))
    }

    private fun setupReferenceDeviceView() {
        noninViewModel = ViewModelProviders.of(requireActivity()).get(NoninViewModel::class.java)

        noninViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
                    notificationResults.remove(REF_KEY)
                    updateNotification()
                }
                noninView.bleConnectionInfo =
                    if (noninViewModel.bluetoothDevice != null) {
                        BleConnectionInfo(connectionState, device?.name, device?.address)
                    } else {
                        null
                    }

                checkRefDeviceConnection(noninViewModel.bluetoothDevice, connectionState)
            }

        noninViewModel.plxMeasurement.observeForever(plxMeasurementObserver)

        noninViewModel.batteryLevel.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                noninView.bleConnectionInfo = noninView.bleConnectionInfo?.copy(batteryLevel = it)
            }
        })

        noninView.onSearchButtonClick {
            showBleScannerDialog(R.string.spo2_ref_device)
        }

        noninView.onConnectButtonClick {
            noninViewModel.reconnect()
        }

        noninView.onDisconnectClick {
            noninViewModel.disconnect()
        }

        noninView.onChangeDeviceClick {
            noninViewModel.disconnect()
            noninView.bleConnectionInfo = null
            showBleScannerDialog(R.string.spo2_ref_device)
        }

        noninView.reset()
    }

    private fun showBleScannerDialog(@StringRes titleRes: Int, deviceNamePrefix: ArrayList<String>? = null) {
        bleScannerDialog = BleScannerDialog.newInstance(getString(titleRes), deviceNamePrefix)
        bleScannerDialog?.setTargetFragment(this, 1437)
        fragmentManager?.let { bleScannerDialog?.show(it, "BleScannerDialog") }
    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {
        /*val settingsDialog = Spo2SettingsFragmentDialog.newInstance()
        fragmentManager?.let { settingsDialog.show(it, "") }*/
    }

    override fun showInfoDialog() {
        val helpDialog =
            HelpDialog.newInstance(getString(R.string.spo2_info), getString(R.string.info))
        fragmentManager?.let { helpDialog.show(it, "helpDialog") }
    }

    override fun addStreamData(streamData: HspStreamData) {
        notificationResults[MXM_KEY] = "Maxim SpO2: ${streamData.spo2}%"
        updateNotification()
        dataRecorder?.record(streamData)

        renderSpo2Model(streamData)
        renderHrmModel(streamData)

        // MSB of the wspo2State corresponds to orientation flag.
        val orientationFlag = (streamData.wspo2State and 0x08) ushr 3
        streamData.wspo2State = streamData.wspo2State and 0x07

        when (streamData.wspo2LowSnr) {
            1 -> signalQualityChip.background.setTint(requireContext().color(R.color.channel_red))
            else -> signalQualityChip.background.setTint(requireContext().color(R.color.channel_green))
        }

        when (streamData.wspo2Motion) {
            1 -> motionChip.background.setTint(requireContext().color(R.color.channel_red))
            else -> motionChip.background.setTint(requireContext().color(R.color.channel_green))
        }

        when (orientationFlag) {
            1 -> orientationChip.background.setTint(requireContext().color(R.color.channel_red))
            else -> orientationChip.background.setTint(requireContext().color(R.color.channel_green))
        }

//        when (streamData.wspo2LowPi) {
//            1 -> lowPi.background.setTint(Color.RED)
//            else -> lowPi.background.setTint(Color.GREEN)
//        }
//
//        when (streamData.wspo2UnreliableR) {
//            1 -> unreliableR.background.setTint(Color.RED)
//            else -> unreliableR.background.setTint(Color.GREEN)
//        }

        rResult = streamData.r
    }

    private fun renderSpo2Model(model: HspStreamData) {
        chartView.addData(model.ir, model.red)

        val spo2Calculated = (model.wspo2PercentageComplete and 0x80) > 0
        val percentage = model.wspo2PercentageComplete and 0x7F

        spo2ResultView.measurementProgress = percentage
        //spo2ResultView.confidence = model.wspo2Confidence

        if (algorithmModeOneShotRadioButton.isChecked) {
            if (model.wspo2State == STATUS_COMPLETED) {
                spo2ResultView.result = model.spo2.roundToInt()
                stopMonitoring()
            } else {
                if (spo2Calculated) {
                    spo2ResultView.result = model.spo2.roundToInt()
                }
            }
        } else {
            if (spo2Calculated) {
                spo2ResultView.result = model.spo2.roundToInt()
            }
        }

        if (model.wspo2State == STATUS_TIMEOUT) {
            spo2ResultView.isTimeout = true
            stopMonitoring()
        }
    }

    private fun renderHrmModel(streamData: HspStreamData) {
        if (measurementStartTimestamp == null) {
            measurementStartTimestamp = System.currentTimeMillis()
        }

        hrResultView.measurementProgress = 0 //getMeasurementProgress()
        hrResultView.result = streamData.hr
    }

    /*private fun getMeasurementProgress(): Int {
        return ((System.currentTimeMillis() - (measurementStartTimestamp
            ?: 0L)) * 100 / WhrmFragment.HR_MEASURING_PERIOD_IN_MILLIS).toInt()
    }*/

    private fun clearChart() {
        chartView.clearChart()
    }

    private fun clearCardViewValues() {
        rResult = null
    }

    private fun setAlgorithmModeRadioButtonsEnabled(isEnabled: Boolean) {
        for (radioButton in algorithmModeRadioGroup.children) {
            radioButton.isEnabled = isEnabled
        }
    }

    override fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        val deviceName = bluetoothDevice.name ?: ""

        when {
            deviceName.startsWith("", false) -> noninViewModel.connect(
                bluetoothDevice
            )
        }

        bleScannerDialog?.dismiss()
    }

    override fun onDetach() {
        super.onDetach()
        noninViewModel.plxMeasurement.removeObserver(plxMeasurementObserver)
        noninViewModel.disconnect()
    }

    fun fillInputs() {
        val user = userViewModel.currentUser.value
        if (user?.username != DEFAULT_USER_NAME) {
            nameEditText.setText(user?.username)
        }
        birthYearEditText.setText(user?.birthYear.toString())
        genderChipGroup.check(if (user?.isMale == true) R.id.maleChip else R.id.femaleChip)
        weightEditText.setText(user?.weight.toString())
        heightEditText.setText(user?.height.toString())
    }

    fun checkInputs(): Boolean {
        val id = IDEditText.text.toString()
        val name = nameEditText.text.toString()
        val surname = surnameEditText.text.toString()
        val birthYear = birthYearEditText.text.toString().toIntOrNull()
        val weight = weightEditText.text.toString().toIntOrNull()
        val height = heightEditText.text.toString().toIntOrNull()
        val hemoglobinValue = hemoglobinEditText.text.toString().toIntOrNull()
        if (id == "") {
            showWarningMessage(getString(R.string.id_required))
            return false
        }
        if (name == "") {
            showWarningMessage(getString(R.string.name_required))
            return false
        }
        if (surname == "") {
            showWarningMessage(getString(R.string.surname_required))
            return false
        }
        if (birthYear == null) {
            showWarningMessage(getString(R.string.birth_year_required))
            return false
        } else if (birthYear !in 1900..Calendar.getInstance().get(Calendar.YEAR)) {
            showWarningMessage(
                getString(
                    R.string.birth_year_out_of_range,
                    Calendar.getInstance().get(Calendar.YEAR)
                )
            )
            return false
        }
        if (weight == null) {
            showWarningMessage(getString(R.string.weight_required))
            return false
        }
        if (height == null) {
            showWarningMessage(getString(R.string.height_required))
            return false
        }
        if (hemoglobinValue == null) {
            showWarningMessage(getString(R.string.hemoglobin_required))
            return false
        }
        return true
    }

    fun userInfoToCsvModel(): String {
        return arrayOf(
            IDEditText.text.toString(),
            nameEditText.text.toString(),
            surnameEditText.text.toString(),
            birthYearEditText.text.toString().toIntOrNull(),
            weightEditText.text.toString(),
            heightEditText.text.toString(),
            hemoglobinEditText.text.toString(),
            if (maleChip.isChecked) "male" else "female",
            FILE_TIMESTAMP_FORMAT.format(logTimestamp)
        ).joinToString(separator = ",")
    }

    override fun logUserInfoToggled() {
        super.logUserInfoToggled()
        if (menuItemLogUserInfo.isChecked) {
            cardViewInputs.visibility = VISIBLE
        } else {
            cardViewInputs.visibility = GONE
        }
    }

    private fun showWarningMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun checkRefDeviceConnection(device: BluetoothDevice?, connectionState: Int?): Boolean {
        if ((device != null) and (connectionState == BluetoothAdapter.STATE_DISCONNECTED)) {
            showAlertDialog(requireContext(),
                getString(R.string.ble_connection_lost_title),
                getString(R.string.ble_connection_ref_device_disconnected_message),
                getString(R.string.ble_connection_right_button_text)
            ) {
                noninViewModel.reconnect()
            }
            return false
        }
        return true
    }
}
