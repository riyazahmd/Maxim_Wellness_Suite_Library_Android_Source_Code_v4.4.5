package com.maximintegrated.maximsensorsapp.sports_coaching

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.observe
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.algorithms.AlgorithmOutput
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.algorithms.hrv.HrvAlgorithmInitConfig
import com.maximintegrated.algorithms.sports.SportsCoachingEpocConfig
import com.maximintegrated.algorithms.sports.SportsCoachingSession
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.set
import com.maximintegrated.maximsensorsapp.profile.toAlgorithmUser
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.android.synthetic.main.fragment_sports_coaching_epoc_recovery.*
import kotlinx.android.synthetic.main.statistics_layout.view.*
import kotlinx.android.synthetic.main.view_result_card.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class SportsCoachingEpocRecoveryFragment : MeasurementBaseFragment() {

    companion object {
        fun newInstance() = SportsCoachingEpocRecoveryFragment()
    }

    private lateinit var algorithmInitConfig: AlgorithmInitConfig
    private val algorithmInput = AlgorithmInput()
    private val algorithmOutput = AlgorithmOutput()
    private val REQUEST_OPEN_DOCUMENT = 1

    private var hr: Int? = null
        set(value) {
            field = value
            hrView.emptyValue = value?.toString() ?: ResultCardView.EMPTY_VALUE
        }

    private var epocRecovery: Int? = null
        set(value) {
            field = value
            epocRecoveryView.emptyValue = value?.toString() ?: ResultCardView.EMPTY_VALUE
        }

    private var vo2MaxFound = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sports_coaching_epoc_recovery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = userViewModel.getCurrentUser().toAlgorithmUser()
        algorithmInitConfig = AlgorithmInitConfig()
        algorithmInitConfig.hrvConfig = HrvAlgorithmInitConfig(40f, 90, 30)
        with(algorithmInitConfig.sportCoachingConfig) {
            this.samplingRate = 25
            this.session = SportsCoachingSession.EPOC_RECOVERY
            this.history = getHistoryFromFiles(user.username, requireContext().contentResolver)
            vo2MaxFound =
                this.history.records.any { it.session == SportsCoachingSession.VO2MAX_RELAX }
        }
        algorithmInitConfig.user = user
        algorithmInitConfig.enableAlgorithmsFlag =
            MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_SPORTS
        setupToolbar(getString(R.string.epoc_recovery))
        menuItemArbitraryCommand.isVisible = false
        menuItemLogToFlash.isVisible = false
        menuItemSettings.isVisible = false
        if (BuildConfig.DEBUG) {
            readFromFile.isVisible = true
        }

        val drawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(
                ContextCompat.getColor(context!!, R.color.progress_red),
                ContextCompat.getColor(context!!, R.color.progress_orange),
                ContextCompat.getColor(context!!, R.color.progress_green)
            )
        )
        drawable.let {
            it.cornerRadius = 10f
            it.mutate()
            it.setGradientCenter(0.8f, 0.2f)
        }
        epocRecoveryView.confidenceProgressBar.progressDrawable = drawable
        epocRecoveryView.confidenceProgressBar.progress = 80
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
        if (algorithmOutput.sports.isNewOutputReady && algorithmOutput.sports.isNewOutputReady) {
            epocRecovery = algorithmOutput.sports.estimates.recovery.epoc.toInt()
            statisticLayout.minHrTextView.text = algorithmOutput.sports.hrStats.minHr.toString()
            statisticLayout.maxHrTextView.text = algorithmOutput.sports.hrStats.maxHr.toString()
            statisticLayout.meanHrTextView.text = algorithmOutput.sports.hrStats.meanHr.toString()
            algorithmOutput.sports.session = SportsCoachingSession.EPOC_RECOVERY
            saveMeasurement(
                algorithmInitConfig.user.username,
                algorithmOutput.sports,
                FILE_TIMESTAMP_FORMAT.format(logTimestamp),
                getMeasurementType(),
                requireContext().contentResolver
            )
            stopMonitoring()
        }
        epocRecoveryView.confidenceProgressBar.progress = streamData.hrConfidence
    }

    override fun getMeasurementType(): String {
        return getString(R.string.epoc_recovery)
    }

    private fun checkForEpocInput() {
        val exerciseDuration =
            when (durationChipGroup.checkedChipId) {
                R.id.durationChip1 -> 5
                R.id.durationChip2 -> 20
                R.id.durationChip3 -> 60
                R.id.durationChip4 -> 90
                else -> 0
            }
        val intensity =
            when (intensityChipGroup.checkedChipId) {
                R.id.intensityChip1 -> 1
                R.id.intensityChip2 -> 2
                R.id.intensityChip3 -> 3
                else -> 0
            }
        val delay =
            when (delayChipGroup.checkedChipId) {
                R.id.delayChip1 -> 0
                R.id.delayChip2 -> 1
                R.id.delayChip3 -> 2
                R.id.delayChip4 -> 3
                else -> 0
            }
        algorithmInitConfig.sportCoachingConfig.epocConfig =
            SportsCoachingEpocConfig(exerciseDuration, intensity, delay)
    }

    override fun startMonitoring(): Boolean {
        if (!vo2MaxFound) {
            Toast.makeText(
                requireContext(),
                R.string.vo2max_requirement,
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        checkForEpocInput()
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

    override fun runFromFile() {
        if (!vo2MaxFound) {
            Toast.makeText(
                requireContext(),
                R.string.vo2max_requirement,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        checkForEpocInput()

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MWA_OUTPUT_DIRECTORY.uri)
            }
        }
        startActivityForResult(intent, REQUEST_OPEN_DOCUMENT)
    }

    override fun dataLoggingToggled() {

    }

    override fun showSettingsDialog() {

    }

    override fun showInfoDialog() {
        val helpDialog =
            HelpDialog.newInstance(getString(R.string.epoc_recovery_info), getString(R.string.info))
        fragmentManager?.let { helpDialog.show(it, "helpDialog") }
    }

    private fun clearCardViewValues() {
        hr = null
        epocRecovery = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OPEN_DOCUMENT && resultCode == RESULT_OK){
            val dirFile = DocumentFile.fromSingleUri(requireContext(), data?.data!!)
            MaximAlgorithms.init(algorithmInitConfig)
            doAsync {
                val inputs = readAlgorithmInputsFromFile(dirFile, context!!.contentResolver)
                for (input in inputs) {
                    MaximAlgorithms.run(input, algorithmOutput)
                    if (algorithmOutput.sports.isNewOutputReady && algorithmOutput.sports.percentCompleted == 100) {
                        with(algorithmOutput.sports) {
                            epocRecovery =
                                algorithmOutput.sports.estimates.recovery.epoc.toInt()
                            session = SportsCoachingSession.EPOC_RECOVERY
                            uiThread {
                                statisticLayout.minHrTextView.text = hrStats.minHr.toString()
                                statisticLayout.maxHrTextView.text = hrStats.maxHr.toString()
                                statisticLayout.meanHrTextView.text = hrStats.meanHr.toString()

                                saveMeasurement(
                                    algorithmInitConfig.user.username,
                                    this, FILE_TIMESTAMP_FORMAT.format(
                                        Date()
                                    ), getMeasurementType(),
                                    requireContext().contentResolver
                                )
                            }
                        }
                        break
                    }
                }
                MaximAlgorithms.end(MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_SPORTS)
            }
        }
    }
}