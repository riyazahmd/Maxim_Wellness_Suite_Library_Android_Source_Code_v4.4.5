package com.maximintegrated.maximsensorsapp.sports_coaching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.algorithms.AlgorithmOutput
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.algorithms.hrv.HrvAlgorithmInitConfig
import com.maximintegrated.algorithms.sports.SportsCoachingSession
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.set
import com.maximintegrated.maximsensorsapp.profile.toAlgorithmUser
import kotlinx.android.synthetic.main.fragment_sports_coaching_readiness.*
import kotlinx.android.synthetic.main.statistics_layout.view.*

class SportsCoachingReadinessFragment : MeasurementBaseFragment() {

    companion object {
        fun newInstance() = SportsCoachingReadinessFragment()
    }

    private lateinit var algorithmInitConfig: AlgorithmInitConfig
    private val algorithmInput = AlgorithmInput()
    private val algorithmOutput = AlgorithmOutput()

    private var hr: Int? = null
        set(value) {
            field = value
            hrView.emptyValue = value?.toString() ?: ResultCardView.EMPTY_VALUE
        }

    private var readiness: Int? = null
        set(value) {
            field = value
            readinessView.emptyValue = value?.toString() ?: ResultCardView.EMPTY_VALUE
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sports_coaching_readiness, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = userViewModel.getCurrentUser().toAlgorithmUser()
        algorithmInitConfig = AlgorithmInitConfig()
        algorithmInitConfig.hrvConfig = HrvAlgorithmInitConfig(40f, 90, 30)
        with(algorithmInitConfig.sportCoachingConfig) {
            this.samplingRate = 25
            this.session = SportsCoachingSession.READINESS
            this.history = getHistoryFromFiles(user.username, requireContext().contentResolver)
        }
        algorithmInitConfig.user = user
        algorithmInitConfig.enableAlgorithmsFlag =
            MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_SPORTS
        setupToolbar(getString(R.string.readiness))
        menuItemArbitraryCommand.isVisible = false
        menuItemLogToFlash.isVisible = false
        menuItemSettings.isVisible = false
    }

    override fun addStreamData(streamData: HspStreamData) {
        hr = streamData.hr
        dataRecorder?.record(streamData)

        algorithmInput.set(streamData)

        MaximAlgorithms.run(algorithmInput, algorithmOutput)
        val percentage = algorithmOutput.sports.percentCompleted
        percentCompleted.measurementProgress = percentage
        notificationResults[MXM_KEY] = "Sports Coaching progress: $percentage%"
        updateNotification()
        if (algorithmOutput.sports.isNewOutputReady && percentage == 100) {
            readiness = algorithmOutput.sports.estimates.readiness.readinessScore.toInt()
            statisticLayout.minHrTextView.text = algorithmOutput.sports.hrStats.minHr.toString()
            statisticLayout.maxHrTextView.text = algorithmOutput.sports.hrStats.maxHr.toString()
            statisticLayout.meanHrTextView.text = algorithmOutput.sports.hrStats.meanHr.toString()
            algorithmOutput.sports.session = SportsCoachingSession.READINESS
            saveMeasurement(
                algorithmInitConfig.user.username,
                algorithmOutput.sports,
                FILE_TIMESTAMP_FORMAT.format(logTimestamp),
                getMeasurementType(),
                requireContext().contentResolver
            )
            stopMonitoring()
        }
    }

    override fun getMeasurementType(): String {
        return getString(R.string.readiness)
    }

    override fun startMonitoring(): Boolean {
        if(!super.startMonitoring()) return false
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
        MaximAlgorithms.end(MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_SPORTS)
        hspViewModel.stopStreaming()
    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {

    }

    override fun showInfoDialog() {
        val helpDialog =
            HelpDialog.newInstance(getString(R.string.readiness_info), getString(R.string.info))
        fragmentManager?.let { helpDialog.show(it, "helpDialog") }
    }

    private fun clearCardViewValues() {
        hr = null
        readiness = null
    }
}