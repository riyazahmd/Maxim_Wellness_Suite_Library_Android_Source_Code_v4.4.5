package com.maximintegrated.maximsensorsapp.sports_coaching

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.observe
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.algorithms.AlgorithmOutput
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.algorithms.hrv.HrvAlgorithmInitConfig
import com.maximintegrated.algorithms.sports.SportsCoachingHistoryItem
import com.maximintegrated.algorithms.sports.SportsCoachingSession
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.set
import com.maximintegrated.maximsensorsapp.profile.toAlgorithmUser
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.android.synthetic.main.fragment_sports_coaching_vo2max.*
import kotlinx.android.synthetic.main.statistics_layout.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class SportsCoachingVO2MaxFragment : MeasurementBaseFragment() {

    companion object {
        fun newInstance() = SportsCoachingVO2MaxFragment()
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

    private var vo2Max: Int? = null
        set(value) {
            field = value
            vo2maxView.emptyValue = value?.toString() ?: ResultCardView.EMPTY_VALUE
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sports_coaching_vo2max, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = userViewModel.getCurrentUser().toAlgorithmUser()
        algorithmInitConfig = AlgorithmInitConfig()
        algorithmInitConfig.hrvConfig = HrvAlgorithmInitConfig(40f, 90, 30)
        with(algorithmInitConfig.sportCoachingConfig) {
            this.samplingRate = 25
            this.session = SportsCoachingSession.VO2MAX_RELAX
            this.history = getHistoryFromFiles(user.username, requireContext().contentResolver)
        }
        algorithmInitConfig.user = user
        algorithmInitConfig.enableAlgorithmsFlag =
            MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_SPORTS
        setupToolbar(getString(R.string.resting_vo2max))
        menuItemArbitraryCommand.isVisible = false
        menuItemLogToFlash.isVisible = false
        menuItemSettings.isVisible = false
        if (BuildConfig.DEBUG) {
            readFromFile.isVisible = true
        }
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
            calculateVo2MaxFromHistory()
            stopMonitoring()
        }
    }

    private fun calculateVo2MaxFromHistory() {
        val username = algorithmInitConfig.user.username
        MaximAlgorithms.end(MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_SPORTS)
        val item: SportsCoachingHistoryItem
        with(algorithmOutput.sports) {
            item = SportsCoachingHistoryItem(timestamp, estimates, hrStats, session)
            statisticLayout.minHrTextView.text = hrStats.minHr.toString()
            statisticLayout.maxHrTextView.text = hrStats.maxHr.toString()
            statisticLayout.meanHrTextView.text = hrStats.meanHr.toString()
        }
        with(algorithmInitConfig.sportCoachingConfig) {
            session = SportsCoachingSession.VO2MAX_FROM_HISTORY
            history = getHistoryFromFiles(username, requireContext().contentResolver)
            history.records.add(0, item)
            history.numberOfRecords++
        }
        algorithmOutput.sports.session = SportsCoachingSession.VO2MAX_RELAX
        val timestamp = algorithmOutput.sports.timestamp
        saveMeasurement(
            username,
            algorithmOutput.sports,
            FILE_TIMESTAMP_FORMAT.format(Date(timestamp * 1000L)),
            getMeasurementType(),
            requireContext().contentResolver
        )

        MaximAlgorithms.init(algorithmInitConfig)
        MaximAlgorithms.run(AlgorithmInput(), algorithmOutput)
        vo2Max = algorithmOutput.sports.estimates.vo2max.relax.toInt()
        algorithmOutput.sports.session = SportsCoachingSession.VO2MAX_FROM_HISTORY
        algorithmOutput.sports.timestamp = timestamp
        saveMeasurement(
            username,
            algorithmOutput.sports,
            FILE_TIMESTAMP_FORMAT.format(Date(timestamp * 1000L)),
            getMeasurementType() + "History",
            requireContext().contentResolver
        )
    }

    override fun getMeasurementType(): String {
        return getString(R.string.vo2max)
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

    override fun runFromFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"
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
            HelpDialog.newInstance(getString(R.string.vo2max_info), getString(R.string.info))
        fragmentManager?.let { helpDialog.show(it, "helpDialog") }
    }

    private fun clearCardViewValues() {
        hr = null
        vo2Max = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_OPEN_DOCUMENT && resultCode == RESULT_OK){
            val dirFile = DocumentFile.fromSingleUri(requireContext(), data?.data!!)
            MaximAlgorithms.init(algorithmInitConfig)
            doAsync {
                val inputs = readAlgorithmInputsFromFile(dirFile, context!!.contentResolver)
                for (input in inputs) {
                    MaximAlgorithms.run(input, algorithmOutput)
                    if (algorithmOutput.sports.percentCompleted == 100 && algorithmOutput.sports.isNewOutputReady) {
                        uiThread {
                            calculateVo2MaxFromHistory()
                        }
                        break
                    }
                }
                MaximAlgorithms.end(MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_SPORTS)
            }
        }
    }
}