package com.maximintegrated.maximsensorsapp.hrv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.algorithms.AlgorithmOutput
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.algorithms.hrv.FreqDomainHrvMetrics
import com.maximintegrated.algorithms.hrv.HrvAlgorithmInitConfig
import com.maximintegrated.algorithms.hrv.TimeDomainHrvMetrics
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.set
import com.maximintegrated.maximsensorsapp.view.DataSetInfo
import com.maximintegrated.maximsensorsapp.view.MultiChannelChartView
import kotlinx.android.synthetic.main.include_hrv_fragment_content.*
import kotlinx.android.synthetic.main.view_multi_channel_chart.view.*

class HrvFragment : MeasurementBaseFragment() {
    companion object {
        fun newInstance() = HrvFragment()
    }

    private var algorithmInitConfig: AlgorithmInitConfig? = null
    private val algorithmInput = AlgorithmInput()
    private val algorithmOutput = AlgorithmOutput()

    private lateinit var timeChartView: MultiChannelChartView
    private lateinit var frequencyChartView: MultiChannelChartView
    private lateinit var ibiChartView: MultiChannelChartView

    private var avnn: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE) + " ms"
            avnnView.text = text

        }

    private var sdnn: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE) + " ms"
            sdnnView.text = text

        }

    private var rmssd: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE) + " ms"
            rmssdView.text = text

        }

    private var pnn50: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE)
            pnn50View.text = text
        }


    private var ulf: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE) + " ms²"
            ulfView.text = text

        }

    private var vlf: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE) + " ms²"
            vlfView.text = text

        }

    private var lf: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE) + " ms²"
            lfView.text = text

        }

    private var hf: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE) + " ms²"
            hfView.text = text
        }

    private var lfOverHf: String? = null
        set(value) {
            field = value
            lfOverHfView.text = value ?: ResultCardView.EMPTY_VALUE

        }

    private var totPwr: String? = null
        set(value) {
            field = value
            val text = (value ?: ResultCardView.EMPTY_VALUE) + " ms²"
            totPwrView.text = text
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hrv, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        algorithmInitConfig = AlgorithmInitConfig()
        algorithmInitConfig?.hrvConfig = HrvAlgorithmInitConfig(40f, 60, 15)
        algorithmInitConfig?.enableAlgorithmsFlag = MaximAlgorithms.FLAG_HRV

        timeChartView = view.findViewById(R.id.time_chart_view)
        frequencyChartView = view.findViewById(R.id.frequency_chart_view)
        ibiChartView = view.findViewById(R.id.ibi_chart_view)

        setupChart()
        setupToolbar(getString(R.string.hrv))
        menuItemSettings.isVisible = false
    }

    private fun setupChart() {
        timeChartView.dataSetInfoList = listOf(
            DataSetInfo(R.string.avnn, R.color.channel_ir),
            DataSetInfo(R.string.sdnn, R.color.channel_red),
            DataSetInfo(R.string.rmssd, R.color.channel_green),
            DataSetInfo(R.string.pnn50, R.color.colorPrimaryDark)
        )

        timeChartView.changeCheckStateOfTheChip(2, true) //set rmssd as default chip

        frequencyChartView.dataSetInfoList = listOf(
            DataSetInfo(R.string.ulf, R.color.channel_ir),
            DataSetInfo(R.string.vlf, R.color.channel_red),
            DataSetInfo(R.string.lf, R.color.channel_green),
            DataSetInfo(R.string.hf, R.color.colorPrimaryDark),
            DataSetInfo(R.string.lfOverHf, R.color.colorPrimary),
            DataSetInfo(R.string.totPwr, R.color.color_secondary)
        )

        frequencyChartView.changeCheckStateOfTheChip(3,true) //set hf as default chip

        ibiChartView.dataSetInfoList = listOf(
            DataSetInfo(R.string.ibi, R.color.channel_red)
        )

        timeChartView.titleView.text = getString(R.string.time_domain_metrics)
        frequencyChartView.titleView.text = getString(R.string.frequency_domain_metrics)
        ibiChartView.titleView.text = getString(R.string.ibiRr)

        timeChartView.maximumEntryCount = 100
        frequencyChartView.maximumEntryCount = 100
        ibiChartView.maximumEntryCount = 100
    }

    override fun addStreamData(streamData: HspStreamData) {

        dataRecorder?.record(streamData)

        algorithmInput.set(streamData)

        if (streamData.rr != 0f) {
            ibiChartView.addData(streamData.rr)
        }

        MaximAlgorithms.run(algorithmInput, algorithmOutput)

        percentCompleted.measurementProgress = algorithmOutput.hrv.percentCompleted
        notificationResults[MXM_KEY] = "HRV progress: ${algorithmOutput.hrv.percentCompleted}%"
        updateNotification()

        if (algorithmOutput.hrv.isHrvCalculated) {
            updateTimeDomainHrvMetrics(algorithmOutput.hrv.timeDomainHrvMetrics)
            updateFrequencyDomainHrvMetrics(algorithmOutput.hrv.freqDomainHrvMetrics)
        }
    }

    private fun updateTimeDomainHrvMetrics(timeDomainHrvMetrics: TimeDomainHrvMetrics) {
        avnn = "%.2f".format(timeDomainHrvMetrics.avnn)
        sdnn = "%.2f".format(timeDomainHrvMetrics.sdnn)
        rmssd = "%.2f".format(timeDomainHrvMetrics.rmssd)
        pnn50 = "%.2f".format(timeDomainHrvMetrics.pnn50)

        timeChartView.addData(
            timeDomainHrvMetrics.avnn.toInt(),
            timeDomainHrvMetrics.sdnn.toInt(),
            timeDomainHrvMetrics.rmssd.toInt(),
            timeDomainHrvMetrics.pnn50.toInt()
        )

    }

    private fun updateFrequencyDomainHrvMetrics(freqDomainHrvMetrics: FreqDomainHrvMetrics) {
        ulf = "%.2f".format(freqDomainHrvMetrics.ulf)
        vlf = "%.2f".format(freqDomainHrvMetrics.vlf)
        lf = "%.2f".format(freqDomainHrvMetrics.lf)
        hf = "%.2f".format(freqDomainHrvMetrics.hf)
        lfOverHf = "%.2f".format(freqDomainHrvMetrics.lfOverHf)
        totPwr = "%.2f".format(freqDomainHrvMetrics.totPwr)

        frequencyChartView.addData(
            freqDomainHrvMetrics.ulf.toInt(),
            freqDomainHrvMetrics.vlf.toInt(),
            freqDomainHrvMetrics.lf.toInt(),
            freqDomainHrvMetrics.hf.toInt(),
            freqDomainHrvMetrics.lfOverHf.toInt(),
            freqDomainHrvMetrics.totPwr.toInt()
        )
    }

    override fun startMonitoring(): Boolean {
        if(!super.startMonitoring()) return false

        clearChart()
        clearCardViewValues()

        MaximAlgorithms.init(algorithmInitConfig)

        percentCompleted.measurementProgress = 0
        percentCompleted.isMeasuring = true
        percentCompleted.result = null
        percentCompleted.isTimeout = false

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

    override fun stopMonitoring() {
        super.stopMonitoring()

        percentCompleted.isMeasuring = false
        MaximAlgorithms.end(MaximAlgorithms.FLAG_HRV)

        hspViewModel.stopStreaming()
    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {

    }

    override fun showInfoDialog() {
        val helpDialog =
            HelpDialog.newInstance(getString(R.string.hrv_info), getString(R.string.info))
        fragmentManager?.let { helpDialog.show(it, "helpDialog") }
    }

    private fun clearChart() {
        timeChartView.clearChart()
        frequencyChartView.clearChart()
        ibiChartView.clearChart()
    }

    private fun clearCardViewValues() {
        avnn = null
        sdnn = null
        rmssd = null
        pnn50 = null
        ulf = null
        vlf = null
        lf = null
        hf = null
        lfOverHf = null
        totPwr = null

    }
}
