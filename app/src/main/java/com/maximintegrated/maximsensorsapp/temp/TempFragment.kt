package com.maximintegrated.maximsensorsapp.temp

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.hsp.HspTempStreamData
import com.maximintegrated.hsp.HspViewModel
import com.maximintegrated.hsp.protocol.SetConfigurationCommand
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import kotlinx.android.synthetic.main.include_temp_fragment_content.*
import java.text.DecimalFormat
import java.util.*

class TempFragment : MeasurementBaseFragment() {

    companion object {
        fun newInstance() = TempFragment()

        private const val SAMPLE_INTERVAL_START_MS = 500
        private const val SAMPLE_INTERVAL_STEP_MS = 500
        private const val SAMPLE_INTERVAL_END_MS = 10000
    }

    private var temperature: Float?
        get() = tempValueView.temperatureInCelsius
        set(value) {
            tempValueView.temperatureInCelsius = value
        }

    private val sampleIntervalInMillis: Int
        get() {
            val seconds =
                sampleIntervalDecimalFormat.parse(sampleIntervalSpinner.selectedItem.toString())
                    ?: 0
            return (seconds.toFloat() * DateUtils.SECOND_IN_MILLIS).toInt()
        }

    private val sampleIntervalDecimalFormat = DecimalFormat("0.0")

    var timestamp = ""


    private val tempStreamObserver = Observer<HspTempStreamData> { data ->
        if (!isMonitoring) return@Observer
        renderTempData(data)
    }

    private var csvWriter: CsvWriter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        hspViewModel.tempStreamData.observeForever(tempStreamObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_temp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        useDataRecorder = false
        setupToolbar(getString(R.string.temp))
        setupSampleIntervalSpinner()
        menuItemArbitraryCommand.isVisible = false
        menuItemLogToFlash.isVisible = false
        menuItemEnabledScd.isVisible = false
        menuItemSettings.isVisible = false
    }

    override fun addStreamData(streamData: HspStreamData) {

    }

    private fun renderTempData(data: HspTempStreamData) {
        csvWriter?.write(data.toCsvModel())
        temperature = data.temperature
        val temperatureInFahrenheit = celsiusToFahrenheit(data.temperature)
        notificationResults[MXM_KEY] = "Temperature: $temperature °C / $temperatureInFahrenheit °F"
        updateNotification()
        tempChartView.addTempData(data.temperature)
    }

    override fun getMeasurementType(): String {
        return getString(R.string.temp)
    }

    override fun startMonitoring(): Boolean {
        if(!super.startMonitoring()) return false
        hspViewModel.streamType = HspViewModel.StreamType.TEMP
        timestamp = FILE_TIMESTAMP_FORMAT.format(Date())
        csvWriter = CsvWriter.open(
            makeCsvFilePath(
                getDirectoryReference(TEMP_DIRECTORY_NAME)!!,
                getMeasurementType(),
                logTimestamp
            ), HspTempStreamData.CSV_HEADER_ARRAY,
            context = requireContext()
        )
        tempChartView.clearData()

        hspViewModel.isDeviceSupported
            .observe(this) {
                hspViewModel.sendCommand(SetConfigurationCommand("blepower", "0"))
                sendLogToFlashCommand(false)
                hspViewModel.startTempStreaming(sampleIntervalInMillis)
            }
        return true
    }

    override fun stopMonitoring() {
        super.stopMonitoring()
        csvWriter?.close()
        csvWriter = null

        hspViewModel.stopStreaming()

    }

    override fun isMonitoringChanged() {
        super.isMonitoringChanged()
        sampleIntervalSpinner.isEnabled = !isMonitoring
    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {

    }

    override fun showInfoDialog() {

    }

    private fun setupSampleIntervalSpinner() {
        val spinnerArray =
            (SAMPLE_INTERVAL_START_MS..SAMPLE_INTERVAL_END_MS step SAMPLE_INTERVAL_STEP_MS).map {
                sampleIntervalDecimalFormat.format(it.toFloat() / DateUtils.SECOND_IN_MILLIS)
            }.toList()

        sampleIntervalSpinner.adapter = ArrayAdapter<String>(
            requireContext(), R.layout.numeric_spinner_item, spinnerArray
        ).apply {
            setDropDownViewResource(R.layout.numeric_spinner_dropdown_item)
        }
    }

    override fun onDetach() {
        super.onDetach()
        hspViewModel.tempStreamData.removeObserver(tempStreamObserver)
        csvWriter?.close()
        csvWriter = null
    }

    override fun onStopMonitoring() {
        hspViewModel.tempStreamData.removeObserver(tempStreamObserver)
        stopMonitoring()
    }
}