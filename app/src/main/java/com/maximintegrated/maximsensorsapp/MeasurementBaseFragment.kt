package com.maximintegrated.maximsensorsapp

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import com.maximintegrated.hsp.*
import com.maximintegrated.hsp.protocol.SetConfigurationCommand
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import com.maximintegrated.maximsensorsapp.exts.ioThread
import com.maximintegrated.maximsensorsapp.profile.UserViewModel
import com.maximintegrated.maximsensorsapp.profile.UserViewModelFactory
import com.maximintegrated.maximsensorsapp.service.ForegroundService
import kotlinx.android.synthetic.main.include_app_bar.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

abstract class MeasurementBaseFragment : Fragment(), IOnBackPressed,
    DataRecorder.DataRecorderListener {

    companion object {
        const val MXM_KEY = "MXM"
        const val REF_KEY = "REF"
    }

    var dataRecorder: DataRecorder? = null
    var useDataRecorder = true
    private var dataGenerator = DataGenerator()

    lateinit var hspViewModel: HspViewModel
    lateinit var userViewModel: UserViewModel

    lateinit var menuItemStartMonitoring: MenuItem
    lateinit var menuItemStopMonitoring: MenuItem
    lateinit var menuItemLogToFile: MenuItem
    lateinit var menuItemLogToFlash: MenuItem
    lateinit var menuItemLogUserInfo: MenuItem
    lateinit var menuItemSettings: MenuItem
    lateinit var menuItemEnabledScd: MenuItem
    lateinit var menuItemEnabledMfio: MenuItem
    lateinit var menuItemEnabledScdsm: MenuItem
    lateinit var menuItemEnabledLowPower: MenuItem
    lateinit var menuItemArbitraryCommand: MenuItem
    lateinit var readFromFile: MenuItem

    var notificationResults: HashMap<String, String> = hashMapOf() // MXM, REF --> KEYS

    var isMonitoring: Boolean = false
        set(value) {
            field = value
            menuItemStopMonitoring.isVisible = value
            menuItemStartMonitoring.isVisible = !value
            isMonitoringChanged()
        }
    var expectingSampleCount: Int = 0

    private var startTime: String? = null
    private var startElapsedTime = 0L

    private var handler = Handler()

    private var fileNameAppendix: String? = null

    fun setupToolbar(title: String) {

        toolbar.apply {
            inflateMenu(R.menu.toolbar_menu)
            menu.apply {
                menuItemStartMonitoring = findItem(R.id.monitoring_start)
                menuItemStopMonitoring = findItem(R.id.monitoring_stop)
                menuItemLogToFile = findItem(R.id.log_to_file)
                menuItemLogToFlash = findItem(R.id.log_to_flash)
                menuItemLogUserInfo = findItem(R.id.log_user_info)
                menuItemSettings = findItem(R.id.hrm_settings)
                menuItemEnabledScd = findItem(R.id.enable_scd)
                menuItemEnabledMfio = findItem(R.id.enable_mfio)
                menuItemEnabledScdsm = findItem(R.id.enable_scdsm)
                menuItemEnabledLowPower = findItem(R.id.enable_low_power)
                menuItemArbitraryCommand = findItem(R.id.send_arbitrary_command)
                readFromFile = findItem(R.id.readFromFileButton)

                menuItemEnabledScd.isChecked = DeviceSettings.scdEnabled
                menuItemEnabledScd.isEnabled = true
                menuItemEnabledMfio.isChecked = DeviceSettings.mfioEnabled
                if (!DeviceSettings.mfioEnabled || !DeviceSettings.scdEnabled) {
                    DeviceSettings.scdsmEnabled = false
                }
                menuItemEnabledScdsm.isChecked = DeviceSettings.scdsmEnabled
                menuItemEnabledScdsm.isEnabled =
                    DeviceSettings.mfioEnabled && DeviceSettings.scdEnabled
                menuItemEnabledLowPower.isChecked = DeviceSettings.lowPowerEnabled

                menuItemLogUserInfo.isVisible = false
                menuItemLogUserInfo.isChecked = DeviceSettings.logUserInfoEnabled

            }
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.monitoring_start -> startMonitoring()
                    R.id.monitoring_stop -> showStopMonitoringDialog()
                    R.id.log_to_file -> dataLoggingToggled()
                    R.id.log_to_flash -> flashLoggingToggled()
                    R.id.log_user_info -> logUserInfoToggled()
                    R.id.enable_scd -> enableScdToggled()
                    R.id.enable_mfio -> mfioToggled()
                    R.id.enable_scdsm -> scdsmToggled()
                    R.id.enable_low_power -> lowPowerToggled()
                    R.id.hrm_settings -> showSettingsDialog()
                    R.id.info_menu_item -> showInfoDialog()
                    R.id.send_arbitrary_command -> showArbitraryCommandDialog()
                    R.id.add_annotation -> showAnnotationDialog()
                    R.id.readFromFileButton -> runFromFile()
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
            setTitle(title)
            pageTitle = title
        }
        doDeviceSpecificJob()
    }

    open fun startMonitoring(): Boolean {
        if (!checkDeviceConnection(hspViewModel.connectionState.value?.second)) return false
        val date = Date()
        logTimestamp = date.time
        startTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(date)
        startElapsedTime = SystemClock.elapsedRealtime()
        if (DeviceSettings.lowPowerEnabled) {
            hspViewModel.streamData.observeForever(lowPowerDataStreamObserver)
        } else {
            hspViewModel.streamData.observeForever(dataStreamObserver)
        }
        dataGenerator.reset()
        var filename = getMeasurementType()
        if (menuItemLogUserInfo.isChecked && getMeasurementType() == "SpO2") {
            filename += fileNameAppendix
        }
        hspViewModel.streamType = HspViewModel.StreamType.PPG
        if (useDataRecorder) {
            dataRecorder = DataRecorder(filename, logTimestamp, requireContext())
            dataRecorder?.dataRecorderListener = this
        }
        isMonitoring = true
        expectingSampleCount = 0
        dataCount = 0
        errorCount = 0

        updateChronometer()
        handler.postDelayed(tickRunnable, 1000)

        startService()
        menuItemEnabledScd.isEnabled = false
        menuItemEnabledMfio.isEnabled = false
        menuItemEnabledScdsm.isEnabled = false
        menuItemLogToFlash.isEnabled = false
        menuItemLogUserInfo.isEnabled = false
        menuItemEnabledLowPower.isEnabled = false
        return true
    }

    open fun stopMonitoring() {
        if (DeviceSettings.lowPowerEnabled) {
            hspViewModel.streamData.removeObserver(lowPowerDataStreamObserver)
        } else {
            hspViewModel.streamData.removeObserver(dataStreamObserver)
        }
        isMonitoring = false
        expectingSampleCount = 0
        startTime = null
        handler.removeCallbacks(tickRunnable)
        dataRecorder?.close()
        dataRecorder = null
        stopService()
        errorWriter?.close()
        errorWriter = null
        menuItemEnabledScd.isEnabled = true
        menuItemEnabledMfio.isEnabled = true
        menuItemEnabledScdsm.isEnabled =
            menuItemEnabledMfio.isChecked && menuItemEnabledScd.isChecked
        menuItemLogToFlash.isEnabled = true
        menuItemLogUserInfo.isEnabled = true
        menuItemEnabledLowPower.isEnabled = true
    }

    open fun isMonitoringChanged() {

    }

    open fun sendDefaultSettings() {
        hspViewModel.sendCommand(
            SetConfigurationCommand(
                "wearablesuite",
                "scdenable",
                if (menuItemEnabledScd.isChecked) "1" else "0"
            )
        )
        hspViewModel.sendCommand(SetConfigurationCommand("blepower", "0"))
        hspViewModel.sendCommand(
            SetConfigurationCommand(
                "event_mode",
                if (DeviceSettings.mfioEnabled) "1" else "0"
            )
        )
    }

    open fun sendScdStateMachineIfRequired() {
        if (DeviceSettings.scdsmEnabled) {
            hspViewModel.sendCommand(SetConfigurationCommand("scdsm", "1"))
        }
    }

    open fun sendAlgoMode() {
        hspViewModel.sendCommand(SetConfigurationCommand("wearablesuite", "algomode", "0"))
    }

    open fun sendLogToFlashCommand(forcedValue: Boolean? = null) {
        val flash = forcedValue ?: menuItemLogToFlash.isChecked
        hspViewModel.sendCommand(
            SetConfigurationCommand(
                "flash",
                "log",
                if (flash) "1" else "0"
            )
        )
    }

    open fun showStopMonitoringDialog() {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Stop Monitoring")
        alertDialog.setMessage("Are you sure you want to stop monitoring ?")
            .setPositiveButton("OK") { dialog, which ->
                stopMonitoring()
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    abstract fun dataLoggingToggled()

    open fun flashLoggingToggled() {
        menuItemLogToFlash.isChecked = !menuItemLogToFlash.isChecked
        menuItemLogToFile.isChecked = !menuItemLogToFlash.isChecked
    }

    open fun logUserInfoToggled() {
        menuItemLogUserInfo.isChecked = !menuItemLogUserInfo.isChecked
        DeviceSettings.logUserInfoEnabled = menuItemLogUserInfo.isChecked
    }

    open fun enableScdToggled() {
        DeviceSettings.scdEnabled = !menuItemEnabledScd.isChecked
        menuItemEnabledScd.isChecked = DeviceSettings.scdEnabled
        if (!DeviceSettings.scdEnabled) {
            DeviceSettings.scdsmEnabled = false
            menuItemEnabledScdsm.isChecked = false
            menuItemEnabledScdsm.isEnabled = false
        } else {
            menuItemEnabledScdsm.isEnabled = DeviceSettings.mfioEnabled
        }
    }

    open fun mfioToggled() {
        DeviceSettings.mfioEnabled = !menuItemEnabledMfio.isChecked
        menuItemEnabledMfio.isChecked = DeviceSettings.mfioEnabled
        if (!DeviceSettings.mfioEnabled) {
            DeviceSettings.scdsmEnabled = false
            menuItemEnabledScdsm.isChecked = false
            menuItemEnabledScdsm.isEnabled = false
        } else {
            menuItemEnabledScdsm.isEnabled = DeviceSettings.scdEnabled
        }
    }

    open fun scdsmToggled() {
        DeviceSettings.scdsmEnabled = !menuItemEnabledScdsm.isChecked
        menuItemEnabledScdsm.isChecked = DeviceSettings.scdsmEnabled
    }

    open fun lowPowerToggled() {
        DeviceSettings.lowPowerEnabled = !menuItemEnabledLowPower.isChecked
        menuItemEnabledLowPower.isChecked = DeviceSettings.lowPowerEnabled
    }

    abstract fun showSettingsDialog()

    abstract fun showInfoDialog()

    open fun showArbitraryCommandDialog() {
        val arbitraryCommandDialog = ArbitraryCommandFragmentDialog.newInstance()
        arbitraryCommandDialog.setTargetFragment(this, 1338)
        fragmentManager?.let { arbitraryCommandDialog.show(it, "arbitraryCommandDialog") }
    }

    abstract fun addStreamData(streamData: HspStreamData)

    override fun onBackPressed(): Boolean {
        return isMonitoring
    }

    override fun onStopMonitoring() {
        stopMonitoring()
    }

    private var shouldShowDataLossError = false
        set(value) {
            field = value
            dataRecorder?.packetLossOccurred = value
            if (value) {
                if (view == null) {
                    return
                }
                showSnackbar(
                    view!!,
                    getString(R.string.packet_lost_message),
                    Snackbar.LENGTH_INDEFINITE
                )
            }
        }

    private var previousData: HspStreamData? = null
    private var dataCount = 0L
    private var errorCount = 0L

    private val dataStreamObserver = Observer<HspStreamData> { data ->
        if (!isMonitoring) return@Observer
        dataCount++
        if (dataCount == 25L) {
            sendScdStateMachineIfRequired()
        }
        if (expectingSampleCount != data.sampleCount) {
            errorCount++
            if ((data.sampleCount - expectingSampleCount > 1) || (errorCount * 100f / dataCount > 2f)) {
                shouldShowDataLossError = true
            }
            if (previousData != null) {
                val sampleCount = expectingSampleCount
                val sampleTime =
                    ((data.sampleTime.toLong() + previousData!!.sampleTime.toLong()) / 2).toInt()
                val green = (data.green + previousData!!.green) / 2
                val green2 = (data.green2 + previousData!!.green2) / 2
                val ir = (data.ir + previousData!!.ir) / 2
                val red = (data.red + previousData!!.red) / 2
                val accX = (data.accelerationX + previousData!!.accelerationX) / 2
                val accY = (data.accelerationY + previousData!!.accelerationY) / 2
                val accZ = (data.accelerationZ + previousData!!.accelerationZ) / 2
                val opMode = previousData!!.operationMode
                val hr = (data.hr + previousData!!.hr) / 2
                val hrConfidence = (data.hrConfidence + previousData!!.hrConfidence) / 2
                val rr = previousData!!.rr
                val rrConfidence = previousData!!.rrConfidence
                val activity = previousData!!.activity
                val r = (data.r + previousData!!.r) / 2
                val spo2Confidence = (data.wspo2Confidence + previousData!!.wspo2Confidence) / 2
                val spo2 = (data.spo2 + previousData!!.spo2) / 2
                val spo2PercentageComplete =
                    (data.wspo2PercentageComplete + previousData!!.wspo2PercentageComplete) / 2
                val spo2LowSnr = previousData!!.wspo2LowSnr
                val spo2Motion = previousData!!.wspo2Motion
                val spo2LowPi = previousData!!.wspo2LowPi
                val spo2UnreliableR = previousData!!.wspo2UnreliableR
                val spo2State = previousData!!.wspo2State
                val scdState = previousData!!.scdState
                val walk = (data.walkSteps + previousData!!.walkSteps) / 2
                val run = (data.runSteps + previousData!!.runSteps) / 2
                val kcal = (data.kCal + previousData!!.kCal) / 2
                val totalActEnergy = (data.totalActEnergy + previousData!!.totalActEnergy) / 2
                val timestamp = (data.currentTimeMillis + previousData!!.currentTimeMillis) / 2

                val backupData = HspStreamData(
                    sampleCount,
                    sampleTime,
                    green,
                    green2,
                    ir,
                    red,
                    accX,
                    accY,
                    accZ,
                    opMode,
                    hr,
                    hrConfidence,
                    rr,
                    rrConfidence,
                    activity,
                    r,
                    spo2Confidence,
                    spo2,
                    spo2PercentageComplete,
                    spo2LowSnr,
                    spo2Motion,
                    spo2LowPi,
                    spo2UnreliableR,
                    spo2State,
                    scdState,
                    walk,
                    run,
                    kcal,
                    totalActEnergy,
                    0,
                    timestamp
                )
                dataRecorder?.record(backupData)
            }
            Timber.d("Error: expectingSampleCount = $expectingSampleCount  receivedSampleCount = ${data.sampleCount}")
            saveError(expectingSampleCount.toString(), data.sampleCount.toString())
            expectingSampleCount = data.sampleCount
        }
        addStreamData(data)
        previousData = data
        expectingSampleCount++
        if (expectingSampleCount == 256) {
            expectingSampleCount = 0
        }
    }

    private val lowPowerDataStreamObserver = Observer<HspStreamData> { data ->
        if (!isMonitoring) return@Observer
        dataCount++
        if (dataCount == 1L) {
            sendScdStateMachineIfRequired()
        }
        val list = dataGenerator.generateData(data)
        if (list.isNotEmpty()) {
            list.forEach {
                addStreamData(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hspViewModel = ViewModelProviders.of(requireActivity()).get(HspViewModel::class.java)
        userViewModel = ViewModelProviders.of(
            requireActivity(), UserViewModelFactory(
                (requireActivity().application as MaximSensorsApp).userDao
            )
        ).get(UserViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        hspViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                toolbar.connectionInfo = if (hspViewModel.bluetoothDevice != null) {
                    BleConnectionInfo(connectionState, device?.name, device?.address)
                } else {
                    null
                }
                checkDeviceConnection(hspViewModel.connectionState.value?.second)
            }
        //hspViewModel.streamData.observeForever(dataStreamObserver)
    }

    override fun onDetach() {
        super.onDetach()
        annotationWriter?.close()
        //hspViewModel.streamData.removeObserver(dataStreamObserver)
        stopService()
    }

    override fun onFilesAreReadyForAlignment(
        alignedFilePath: Uri,
        maxim1HzFilePath: Uri,
        refFilePath: Uri
    ) {

        ioThread {
            align(alignedFilePath, maxim1HzFilePath, refFilePath, requireContext())
        }
    }

    private var serviceActive = false

    private fun startService() {
        val intent = Intent(requireActivity(), ForegroundService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
        serviceActive = true
    }

    private fun stopService() {
        val intent = Intent(requireActivity(), ForegroundService::class.java)
        activity?.stopService(intent)
        serviceActive = false
    }

    private fun getNotificationText(): String {
        var text = ""
        if (notificationResults[MXM_KEY] != null) {
            text += notificationResults[MXM_KEY]
        }
        if (notificationResults[REF_KEY] != null) {
            text += "  ${notificationResults[REF_KEY]}"
        }
        return text
    }

    fun updateNotification() {
        if (serviceActive) {
            val intent = Intent(requireActivity(), ForegroundService::class.java)
            intent.putExtra(ForegroundService.NOTIFICATION_MESSAGE_KEY, getNotificationText())
            ContextCompat.startForegroundService(requireContext(), intent)
        }
    }

    var logTimestamp = System.currentTimeMillis()
    private var annotationWriter: CsvWriter? = null
    private var errorWriter: CsvWriter? = null

    open fun getMeasurementType(): String {
        return this.javaClass.simpleName.replace("Fragment", "")
    }

    private fun saveAnnotation(annotation: String) {
        if (annotationWriter == null) {
            annotationWriter = CsvWriter.open(
                makeCsvFilePath(
                    getDirectoryReference(ANNOTATION_DIRECTORY_NAME)!!,
                    getMeasurementType() + ANNOTATION_SUFFIX,
                    logTimestamp
                ),
                arrayOf("timestamp", "annotation"),
                context = requireContext()
            )
        }
        annotationWriter?.write(
            System.currentTimeMillis(),
            annotation
        )
    }

    private fun saveError(expected: String, received: String) {
        if (errorWriter == null) {
            errorWriter = CsvWriter.open(
                makeCsvFilePath(
                    getDirectoryReference(ERROR_DIRECTORY_NAME)!!,
                    getMeasurementType() + ERROR_SUFFIX,
                    logTimestamp
                ),
                arrayOf("timestamp", "expected", "received"),
                context = requireContext()
            )
        }
        errorWriter?.write(System.currentTimeMillis(), expected, received)
    }

    private fun showAnnotationDialog() {
        val editText = EditText(context)
        editText.hint = getString(R.string.enter_message)
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        alertDialog.setTitle(getString(R.string.add_annotation))
        alertDialog.setView(editText)
            .setPositiveButton(getString(R.string.save)) { dialog, which ->
                saveAnnotation(editText.text.toString())
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    open fun runFromFile() {

    }

    fun showSnackbar(view: View, message: String, duration: Int) {
        val snackbar = Snackbar.make(view, message, duration)
        snackbar.setAction(getString(R.string.ok)) {
            snackbar.dismiss()
        }
        snackbar.show()
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isMonitoring) {
                updateChronometer()
                handler.postDelayed(this, 1000)
            } else {
                handler.removeCallbacks(this)
            }
        }
    }

    private fun updateChronometer() {
        val elapsedMillis = SystemClock.elapsedRealtime() - startElapsedTime
        toolbar.subtitle =
            "Start Time: ${startTime ?: ResultCardView.EMPTY_VALUE} - ${
                getFormattedTime(
                    elapsedMillis
                )
            }"
    }

    private fun doDeviceSpecificJob() {
        when (hspViewModel.deviceModel) {
            ME15 -> {
                menuItemLogToFlash.isVisible = false
                menuItemEnabledMfio.isVisible = false
                menuItemEnabledLowPower.isVisible = false
                Timber.d("doDeviceSpecificJob ME15")
            }
            ME11A -> {
                Timber.d("doDeviceSpecificJob ME11A")
            }
            ME11B -> {
                Timber.d("doDeviceSpecificJob ME11B")
            }
            ME11C -> {
                Timber.d("doDeviceSpecificJob ME11C")
            }
            ME11D -> {
                Timber.d("doDeviceSpecificJob ME11D")
            }
            else -> {
                Timber.d("doDeviceSpecificJob UNDEFINED")
            }
        }
    }

    fun setFileNameAppendix(value: String?) {
        fileNameAppendix = value
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
}
