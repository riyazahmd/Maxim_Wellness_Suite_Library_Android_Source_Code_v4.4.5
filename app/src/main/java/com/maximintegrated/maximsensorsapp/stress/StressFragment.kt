package com.maximintegrated.maximsensorsapp.stress

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.observe
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.algorithms.AlgorithmOutput
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.algorithms.hrv.HrvAlgorithmInitConfig
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.set
import kotlinx.android.synthetic.main.include_stress_fragment_content.*
import kotlinx.android.synthetic.main.view_result_card.view.*

class StressFragment : MeasurementBaseFragment() {
    companion object {
        fun newInstance() = StressFragment()
    }

    private var algorithmInitConfig: AlgorithmInitConfig? = null
    private val algorithmInput = AlgorithmInput()
    private val algorithmOutput = AlgorithmOutput()

    private var hr: Int? = null
        set(value) {
            field = value
            hrView.emptyValue = value?.toString() ?: ResultCardView.EMPTY_VALUE
        }

    private var stress: Int? = null
        set(value) {
            field = value
            when (value) {
                in 0..2 -> setStressView(
                    R.drawable.ic_stress,
                    R.color.stress_red,
                    "${value}: Overwhelmed"
                )
                in 3..5 -> setStressView(
                    R.drawable.ic_stress,
                    R.color.stress_orange,
                    "${value}: Frustrated"
                )
                6, 7 -> setStressView(
                    R.drawable.ic_stress_neutral,
                    R.color.stress_orange,
                    "${value}: Manageable stress"
                )
                8, 9 -> setStressView(
                    R.drawable.ic_stress_neutral,
                    R.color.stress_green,
                    "${value}: Feeling okay"
                )
                10, 11 -> setStressView(
                    R.drawable.ic_stress_smile,
                    R.color.stress_green,
                    "${value}: Doing great"
                )
                in 12..14 -> setStressView(
                    R.drawable.ic_stress_smile,
                    R.color.stress_orange,
                    "${value}: Very relaxed"
                )
                in 15..18 -> setStressView(
                    R.drawable.ic_stress_smile,
                    R.color.stress_red,
                    "${value}: Uncomfortably relaxed"
                )
                else -> setStressView(
                    R.drawable.ic_stress_neutral,
                    R.color.stress_orange,
                    ResultCardView.EMPTY_VALUE
                )
            }
        }

    private fun setStressView(iconRes: Int, colorRes: Int, message: String) {
        stressView.emptyValue = message
        if (context != null) {
            stressView.iconImageView.setImageResource(iconRes)
            val color = ContextCompat.getColor(context!!, colorRes)
            ImageViewCompat.setImageTintList(
                stressView.iconImageView,
                ColorStateList.valueOf(color)
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        algorithmInitConfig = AlgorithmInitConfig()
        algorithmInitConfig?.hrvConfig = HrvAlgorithmInitConfig(40f, 90, 30)
        algorithmInitConfig?.enableAlgorithmsFlag =
            MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_STRESS

        setupToolbar(getString(R.string.stress))
        menuItemSettings.isVisible = false
    }

    override fun addStreamData(streamData: HspStreamData) {
        hr = streamData.hr
        dataRecorder?.record(streamData)

        algorithmInput.set(streamData)

        val status = MaximAlgorithms.run(algorithmInput, algorithmOutput) and MaximAlgorithms.FLAG_STRESS

        percentCompleted.measurementProgress = algorithmOutput.hrv.percentCompleted
        notificationResults[MXM_KEY] = "Stress progress: ${algorithmOutput.hrv.percentCompleted}%"
        updateNotification()
        if (status == 0) {
            stress = algorithmOutput.stress.stressScore
            notificationResults[MXM_KEY] = "Stress score: $stress"
            updateNotification()
            stopMonitoring()
        }
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
        MaximAlgorithms.end(MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_STRESS)

        hspViewModel.stopStreaming()
    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {

    }

    override fun showInfoDialog() {
        val helpDialog =
            HelpDialog.newInstance(getString(R.string.stress_info), getString(R.string.info))
        fragmentManager?.let { helpDialog.show(it, "helpDialog") }
    }

    private fun clearCardViewValues() {
        hr = null
        stress = null
    }
}