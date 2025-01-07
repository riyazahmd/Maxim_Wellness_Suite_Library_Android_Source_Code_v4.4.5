package com.maximintegrated.maximsensorsapp.ecg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.maximintegrated.hsp.*
import com.maximintegrated.hsp.protocol.HspCommand
import com.maximintegrated.hsp.protocol.SetConfigurationCommand
import com.maximintegrated.hsp.protocol.SetRegisterCommand
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import kotlinx.android.synthetic.main.include_ecg_fragment_content.*
import java.util.*

class EcgFragment : MeasurementBaseFragment() {

    companion object {
        fun newInstance() = EcgFragment()

        private const val RTOR_AVERAGE_WINDOW_SIZE = 5
        private const val EMPTY_VALUE = "--"
    }

    private var currentRtoR: Float? = null
        set(value) {
            field = value
            if (value != null) {
                currentRtorValueView.text = "%.2f".format(value)
            } else {
                currentRtorValueView.text = EMPTY_VALUE
            }
        }

    private var averageRtoR: Float? = null
        set(value) {
            field = value
            if (value != null) {
                averageRtorValueView.text = "%.2f".format(value)
            } else {
                averageRtorValueView.text = EMPTY_VALUE
            }
        }

    private val rtorMovingAverage = MovingAverage(RTOR_AVERAGE_WINDOW_SIZE)

    private val ecgStreamObserver = Observer<Array<HspEcgStreamData>> { data ->
        if (!isMonitoring) return@Observer
        renderEcgData(data)
    }

    private var csvWriter: CsvWriter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        hspViewModel.ecgStreamData.observeForever(ecgStreamObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ecg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        useDataRecorder = false
        setupToolbar(getString(R.string.ecg))
        menuItemArbitraryCommand.isVisible = false
        menuItemLogToFlash.isVisible = false
        menuItemEnabledScd.isVisible = false
        menuItemSettings.isVisible = false

        ecgChartView.invertClicked = {
            hspViewModel.sendCommand(HspCommand.fromText("set_cfg ecg invert"))
        }
    }

    override fun addStreamData(streamData: HspStreamData) {

    }

    private fun renderEcgData(data: Array<HspEcgStreamData>) {
        for (d in data) {
            d.filteredEcg = ecgChartView.applyFilterAndAddData(d.ecgMv)
            if (d.currentRToRBpm != 0) {
                val rtor = d.currentRToRBpm.toFloat()
                currentRtoR = rtor
                rtorMovingAverage.add(rtor)
                averageRtoR = rtorMovingAverage.average
            }
            d.averagedRToRBpm = rtorMovingAverage.average
            csvWriter?.write(d.toCsvModel())
        }
        notificationResults[MXM_KEY] = "ECG: ${data.last().ecgMv}"
        updateNotification()
    }

    override fun getMeasurementType(): String {
        return getString(R.string.ecg)
    }

    override fun startMonitoring(): Boolean {
        if(!super.startMonitoring()) return false
        hspViewModel.streamType = HspViewModel.StreamType.ECG
        csvWriter = CsvWriter.open(
            makeCsvFilePath(
                getDirectoryReference(ECG_DIRECTORY_NAME)!!,
                getMeasurementType(),
                logTimestamp
            ), HspEcgStreamData.CSV_HEADER_ARRAY,
            context=requireContext()
        )

        averageRtoR = null
        currentRtoR = null
        rtorMovingAverage.reset()
        ecgChartView.clearData()

        hspViewModel.isDeviceSupported
            .observe(this) {
                hspViewModel.sendCommand(SetConfigurationCommand("blepower", "0"))
                sendLogToFlashCommand(false)
                sendDefaultRegisterValues()
                HspEcgStreamData.ECG_GAIN = EcgRegisterMap.getDefaultEcgGain()
                hspViewModel.startEcgStreaming()
            }
        return true
    }

    private fun sendDefaultRegisterValues() {
        EcgRegisterMap.Defaults.forEach { (address, value) ->
            hspViewModel.sendCommand(SetRegisterCommand("ecg", address, value))
        }
    }

    override fun stopMonitoring() {
        super.stopMonitoring()
        csvWriter?.close()
        csvWriter = null

        hspViewModel.stopStreaming()

    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {

    }

    override fun showInfoDialog() {

    }

    override fun onDetach() {
        super.onDetach()
        hspViewModel.ecgStreamData.removeObserver(ecgStreamObserver)
        csvWriter?.close()
        csvWriter = null
    }

    override fun onStopMonitoring() {
        hspViewModel.ecgStreamData.removeObserver(ecgStreamObserver)
        stopMonitoring()
    }
}