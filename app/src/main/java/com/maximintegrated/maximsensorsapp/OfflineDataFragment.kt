package com.maximintegrated.maximsensorsapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.algorithms.AlgorithmOutput
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.algorithms.hrv.HrvAlgorithmInitConfig
import com.maximintegrated.algorithms.respiratory.RespiratoryRateAlgorithmInitConfig
import com.maximintegrated.algorithms.respiratory.RespiratoryRateAlgorithmOutput
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import kotlinx.android.synthetic.main.fragment_offline_data.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

enum class HrAlignment {
    ALIGNMENT_FAIL,
    ALIGNMENT_NO_REF_DEVICE,
    ALIGNMENT_SUCCESSFUL
}

class OfflineDataFragment : Fragment(), AdapterView.OnItemSelectedListener,
    CsvWriter.Companion.CsvWriterListener {

    private var algorithmInitConfig: AlgorithmInitConfig? = null
    private val algorithmOutput = AlgorithmOutput()

    var calculated: Float = 0f

    private var logFile: DocumentFile? = null

    private var refHrFile: DocumentFile? = null

    private var oneHzFile: DocumentFile? = null

    private var alignFile: DocumentFile? = null

    private var offlineDataList: ArrayList<AlgorithmInput> = arrayListOf()

    private var respResults: List<Pair<Long, RespiratoryRateAlgorithmOutput>> = arrayListOf()

    private var hrvResults: List<Pair<Long, HrvOfflineChartData>> = arrayListOf()

    private var stressResults: List<Pair<Long, Int>> = arrayListOf()

    companion object {
        const val RESP_INDEX = 6
        const val RMSSD_INDEX = 7
        const val SDNN_INDEX = 8
        const val AVNN_INDEX = 9
        const val PNN50_INDEX = 10
        const val ULF_INDEX = 11
        const val VLF_INDEX = 12
        const val LF_INDEX = 13
        const val HF_INDEX = 14
        const val LF_HF_INDEX = 15
        const val TOTPWR_INDEX = 16
        const val STRESS_INDEX = 17

        fun newInstance(logFile: DocumentFile): OfflineDataFragment {
            val fragment = OfflineDataFragment()
            fragment.logFile = logFile

            fragment.refHrFile = getDirectoryReference(HR_REF_DIRECTORY_NAME)?.findFile("${logFile.name!!.substring(0, logFile.name!!.lastIndexOf('.'))}${HR_REF_SUFFIX}.csv")
            fragment.oneHzFile = getDirectoryReference(ONE_HZ_DIRECTORY_NAME)?.findFile("${logFile.name!!.substring(0, logFile.name!!.lastIndexOf('.'))}${ONE_HZ_SUFFIX}.csv")
            fragment.alignFile = getDirectoryReference(ALIGNED_DIRECTORY_NAME)?.findFile("${logFile.name!!.substring(0, logFile.name!!.lastIndexOf('.'))}${ALIGNED_SUFFIX}.csv")

            return fragment
        }
    }

    private var hrAlignment = HrAlignment.ALIGNMENT_NO_REF_DEVICE

    private var csvWriter: CsvWriter? = null
    //private val adapter: OfflineDataAdapter by lazy { OfflineDataAdapter() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_offline_data, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.clear()

        algorithmInitConfig = AlgorithmInitConfig()
        algorithmInitConfig?.respConfig = RespiratoryRateAlgorithmInitConfig(
            RespiratoryRateAlgorithmInitConfig.SourceOptions.WRIST,
            RespiratoryRateAlgorithmInitConfig.LedCodes.GREEN,
            RespiratoryRateAlgorithmInitConfig.SamplingRateOption.Hz_25,
            RespiratoryRateAlgorithmInitConfig.DEFAULT_MOTION_MAGNITUDE_LIMIT
        )

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.chart_titles,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            chartSpinner.adapter = adapter
        }

        chartSpinner.onItemSelectedListener = this

        csvExportButton.setOnClickListener {
            val index = chartSpinner.selectedItemPosition
            var file: DocumentFile?
            when (index) {
                RESP_INDEX -> {
                    file = getOutputFile("resp_out")
                    if (file != null) {
                        when {
                            file.exists() -> {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.file_already_exists),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            respResults.isEmpty() -> {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.no_data_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            else -> {
                                csvWriter = CsvWriter.open(file.uri, context=requireContext())
                                csvWriter?.listener = this@OfflineDataFragment
                            }
                        }
                    }
                }
                in RMSSD_INDEX..TOTPWR_INDEX -> {
                    file = getOutputFile("hrv_out")
                    if (file != null) {
                        when {
                            file.exists() -> {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.file_already_exists),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            hrvResults.isEmpty() -> {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.no_data_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            else -> {
                                csvWriter = CsvWriter.open(file.uri, context=requireContext())
                                csvWriter?.listener = this@OfflineDataFragment
                            }
                        }
                    }
                }
                STRESS_INDEX -> {
                    file = getOutputFile("stress_out")
                    if (file != null) {
                        when {
                            file.exists() -> {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.file_already_exists),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            stressResults.isEmpty() -> {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.no_data_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            else -> {
                                csvWriter = CsvWriter.open(file.uri, context=requireContext())
                                csvWriter?.listener = this@OfflineDataFragment
                            }
                        }
                    }

                }
            }
            progressBar.visibility = View.VISIBLE
            doAsync {
                when (index) {
                    RESP_INDEX -> {
                        csvWriter?.write(
                            "timestamp",
                            "Respiration Rate. Motion Flag, Ibi Low Quality Flag, Ppg Low Quality Flag"
                        )
                        respResults.forEach {
                            val rr = "%.2f".format(it.second.respirationRate)
                            val motion = it.second.isMotionFlag.toInt()
                            val ibiLowQuality = it.second.isIbiLowQualityFlag.toInt()
                            val ppgLowQuality = it.second.isPpgLowQualityFlag.toInt()
                            csvWriter?.write(it.first, rr, motion, ibiLowQuality, ppgLowQuality)
                        }
                        csvWriter?.close()
                    }
                    in RMSSD_INDEX..TOTPWR_INDEX -> {
                        csvWriter?.write(
                            "timestamp", "AVNN", "SDNN", "RMSSD", "PNN50",
                            "ULF", "VLF", "LF", "HF", "LF/HF", "TOT_PWR"
                        )
                        hrvResults.forEach {
                            csvWriter?.write(
                                it.first, it.second.avnn, it.second.sdnn,
                                it.second.rmssd, it.second.pnn50, it.second.ulf, it.second.vlf,
                                it.second.lf, it.second.hf, it.second.lfOverHf, it.second.totPwr
                            )
                        }
                        csvWriter?.close()
                    }
                    STRESS_INDEX -> {
                        csvWriter?.write("timestamp", "Stress Score")
                        stressResults.forEach {
                            csvWriter?.write(it.first, it.second)
                        }
                        csvWriter?.close()
                    }
                }
                uiThread {
                    progressBar.visibility = View.GONE
                }
            }
        }

        progressBar.visibility = View.VISIBLE
        doAsync {

            offlineDataList = readAlgorithmInputsFromFile(logFile, context!!.contentResolver)

            val alignedDataList = readTimeStampAndHrFromAlignedFile(alignFile, context!!.contentResolver)

            hrvResults = runHrvAlgo()

            respResults = runRrAlgo()

            stressResults = runStressAlgo()

            //var hrTimestampStart = 0L

            if (alignedDataList.isNotEmpty()) {
                hrAlignment = HrAlignment.ALIGNMENT_SUCCESSFUL
                //hrTimestampStart = alignedDataList[0].first
                offlineChart.put(
                    1, OfflineChartData(
                        alignedDataList.mapIndexed { idx, it ->
                            Entry(
                                idx.toFloat(),
                                it.second.toFloat()
                            )
                        },
                        "HR (bpm)",
                        "Maxim"
                    )
                )
                offlineChart.put(
                    1, OfflineChartData(
                        alignedDataList.mapIndexed { idx, it ->
                            Entry(
                                idx.toFloat(),
                                it.third.toFloat()
                            )
                        },
                        "HR (bpm)",
                        "Ref"
                    )
                )
                alignedDataList.clear()
            } else {
                val oneHzDataList = readTimeStampAndHrFrom1HzFile(oneHzFile, context!!.contentResolver)

                if (oneHzDataList.isEmpty()) {
                    offlineChart.put(
                        1, OfflineChartData(
                            offlineDataList.filterIndexed { index, it -> index % 25 == 0 }
                                .mapIndexed { idx, it -> Entry(idx.toFloat(), it.hr.toFloat()) },
                            "HR (bpm)",
                            "Maxim"
                        )
                    )
                } else {
                    val refDataList = readTimeStampAndHrFromReferenceFile(refHrFile, context!!.contentResolver)
                    offlineChart.put(
                        1, OfflineChartData(
                            oneHzDataList.mapIndexed { idx, it ->
                                Entry(
                                    idx.toFloat(),
                                    it.second.toFloat()
                                )
                            },
                            "HR (bpm)",
                            "Maxim"
                        )
                    )
                    if (refDataList.isNotEmpty()) {
                        //hrTimestampStart = min(hrTimestampStart, refDataList[0].first)
                        hrAlignment = HrAlignment.ALIGNMENT_FAIL
                        offlineChart.put(
                            1, OfflineChartData(
                                refDataList.mapIndexed { idx, it ->
                                    Entry(
                                        idx.toFloat(),
                                        it.second.toFloat()
                                    )
                                },
                                "HR (bpm)",
                                "Ref"
                            )
                        )
                        refDataList.clear()
                    } else {
                        hrAlignment = HrAlignment.ALIGNMENT_NO_REF_DEVICE
                    }
                    oneHzDataList.clear()
                }
            }

            offlineChart.put(
                2, OfflineChartData(
                    offlineDataList.filterIndexed { index, it -> index % 25 == 0 }
                        .mapIndexed { idx, it -> Entry(idx.toFloat(), it.spo2 / 10f) },
                    "SpO2 (%)",
                    "Maxim"
                )
            )

            offlineChart.put(
                3, OfflineChartData(
                    offlineDataList.filterIndexed { index, it -> it.rr != 0 }
                        .mapIndexed { idx, it -> Entry(idx.toFloat(), it.rr / 10f) },
                    "IBI (ms)",
                    "Maxim"
                )
            )

            offlineChart.put(
                4, OfflineChartData(
                    offlineDataList.filterIndexed { index, it -> index % 25 == 0 }
                        .mapIndexed { idx, it ->
                            Entry(
                                idx.toFloat(),
                                (it.walkSteps + it.runSteps).toFloat()
                            )
                        },
                    "STEPS",
                    "Maxim"
                )
            )

            offlineChart.put(
                5, OfflineChartData(
                    offlineDataList.filterIndexed { index, it -> index % 25 == 0 }
                        .mapIndexed { idx, it ->
                            Entry(
                                idx.toFloat(), sqrt(
                                    (it.accelerationX / 1000f).pow(2) + (it.accelerationY / 1000f).pow(
                                        2
                                    ) +
                                            (it.accelerationZ / 1000f).pow(2)
                                )
                            )
                        },
                    "MOTION (g)",
                    "Maxim"
                )
            )

            offlineDataList.clear()

            offlineChart.put(
                RESP_INDEX, OfflineChartData(
                    respResults.mapIndexed { idx, it ->
                        Entry(
                            idx.toFloat(),
                            it.second.respirationRate
                        )
                    },
                    "RESPIRATION RATE \n(breath/min)",
                    "Maxim"
                )
            )

            offlineChart.put(
                RMSSD_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.rmssd) },
                    "RMSSD (ms)",
                    "Maxim"
                )
            )

            offlineChart.put(
                SDNN_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.sdnn) },
                    "SDNN (ms)",
                    "Maxim"
                )
            )

            offlineChart.put(
                AVNN_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.avnn) },
                    "AVNN (ms)",
                    "Maxim"
                )
            )

            offlineChart.put(
                PNN50_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.pnn50) },
                    "PNN50 (ms)",
                    "Maxim"
                )
            )

            offlineChart.put(
                ULF_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.ulf) },
                    "ULF (ms²)",
                    "Maxim"
                )
            )

            offlineChart.put(
                VLF_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.vlf) },
                    "VLF (ms²)",
                    "Maxim"
                )
            )

            offlineChart.put(
                LF_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.lf) },
                    "LF (ms²)",
                    "Maxim"
                )
            )

            offlineChart.put(
                HF_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.hf) },
                    "HF (ms²)",
                    "Maxim"
                )
            )

            offlineChart.put(
                LF_HF_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.lfOverHf) },
                    "LF/HF",
                    "Maxim"
                )
            )

            offlineChart.put(
                TOTPWR_INDEX, OfflineChartData(
                    hrvResults.mapIndexed { idx, it -> Entry(idx.toFloat(), it.second.totPwr) },
                    "TOTAL POWER (ms²)",
                    "Maxim"
                )
            )

            offlineChart.put(
                STRESS_INDEX, OfflineChartData(
                    stressResults.mapIndexed { idx, it ->
                        Entry(
                            idx.toFloat(),
                            it.second.toFloat()
                        )
                    },
                    "STRESS SCORE",
                    "Maxim"
                )
            )

            //resultList.clear()

            uiThread {
                progressBar.visibility = View.GONE
                chartSpinner.setSelection(1)
            }
        }
    }

    private fun runHrvAlgo(): ArrayList<Pair<Long, HrvOfflineChartData>> {
        algorithmInitConfig?.hrvConfig = HrvAlgorithmInitConfig(40f, 60, 15)
        algorithmInitConfig?.enableAlgorithmsFlag = MaximAlgorithms.FLAG_HRV
        MaximAlgorithms.init(algorithmInitConfig)

        val resultList: ArrayList<Pair<Long, HrvOfflineChartData>> = arrayListOf()

        for (algorithmInput in offlineDataList) {
            MaximAlgorithms.run(algorithmInput, algorithmOutput)

            if (algorithmOutput.hrv.isHrvCalculated) {
                resultList.add(
                    Pair(
                        algorithmInput.timestamp,
                        HrvOfflineChartData(
                            avnn = algorithmOutput.hrv.timeDomainHrvMetrics.avnn,
                            sdnn = algorithmOutput.hrv.timeDomainHrvMetrics.sdnn,
                            rmssd = algorithmOutput.hrv.timeDomainHrvMetrics.rmssd,
                            pnn50 = algorithmOutput.hrv.timeDomainHrvMetrics.pnn50,
                            ulf = algorithmOutput.hrv.freqDomainHrvMetrics.ulf,
                            vlf = algorithmOutput.hrv.freqDomainHrvMetrics.vlf,
                            lf = algorithmOutput.hrv.freqDomainHrvMetrics.lf,
                            hf = algorithmOutput.hrv.freqDomainHrvMetrics.hf,
                            lfOverHf = algorithmOutput.hrv.freqDomainHrvMetrics.lfOverHf,
                            totPwr = algorithmOutput.hrv.freqDomainHrvMetrics.totPwr
                        )
                    )
                )
            }

        }

        MaximAlgorithms.end(MaximAlgorithms.FLAG_HRV)

        return resultList
    }

    private fun runStressAlgo(): List<Pair<Long, Int>> {
        algorithmInitConfig?.hrvConfig = HrvAlgorithmInitConfig(40f, 90, 30)
        algorithmInitConfig?.enableAlgorithmsFlag =
            MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_STRESS
        MaximAlgorithms.init(algorithmInitConfig)

        val resultList: ArrayList<Pair<Long, Int>> = arrayListOf()

        for (algorithmInput in offlineDataList) {
            val status =
                MaximAlgorithms.run(algorithmInput, algorithmOutput) and MaximAlgorithms.FLAG_STRESS
            if (status == 0) {
                Timber.d("Stress score = ${algorithmOutput.stress.stressScore}")
                resultList.add(Pair(algorithmInput.timestamp, algorithmOutput.stress.stressScore))
            }
        }

        MaximAlgorithms.end(MaximAlgorithms.FLAG_HRV or MaximAlgorithms.FLAG_STRESS)

        return resultList
    }


    private fun runRrAlgo(): List<Pair<Long, RespiratoryRateAlgorithmOutput>> {
        algorithmInitConfig?.enableAlgorithmsFlag = MaximAlgorithms.FLAG_RESP
        MaximAlgorithms.init(algorithmInitConfig)

        val resultList: ArrayList<Pair<Long, RespiratoryRateAlgorithmOutput>> = arrayListOf()

        for ((index, algorithmInput) in offlineDataList.withIndex()) {
            MaximAlgorithms.run(
                algorithmInput,
                algorithmOutput
            )
            if (index % 25 == 0) {
                val respObj = with(algorithmOutput.respiratory) {
                    RespiratoryRateAlgorithmOutput(
                        respirationRate,
                        confidenceLevel,
                        isMotionFlag,
                        isIbiLowQualityFlag,
                        isPpgLowQualityFlag
                    )
                }
                resultList.add(Pair(algorithmInput.timestamp, respObj))
            }
        }

        MaximAlgorithms.end(MaximAlgorithms.FLAG_RESP)

        return resultList
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position != 0) {
            offlineChart.display(position)
            if (position != 1) { //HR
                warningMessageView.visibility = View.GONE
            } else {
                when (hrAlignment) {
                    HrAlignment.ALIGNMENT_SUCCESSFUL -> {
                        warningMessageView.text = getString(R.string.alignment_successful)
                        warningMessageView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_check,
                            0,
                            0,
                            0
                        )
                        warningMessageView.visibility = View.VISIBLE
                    }
                    HrAlignment.ALIGNMENT_FAIL -> {
                        warningMessageView.text = getString(R.string.alignment_fail)
                        warningMessageView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_warning,
                            0,
                            0,
                            0
                        )
                        warningMessageView.visibility = View.VISIBLE
                    }
                    else -> warningMessageView.visibility = View.GONE
                }
            }

            if (position in RESP_INDEX..STRESS_INDEX) {
                csvExportButton.visibility = View.VISIBLE
            } else {
                csvExportButton.visibility = View.GONE
            }
        } else {
            csvExportButton.visibility = View.GONE
        }
    }

    private fun getOutputFile(suffix: String): DocumentFile? {
        return getDirectoryReference(ALGO_OUTPUT_DIRECTORY_NAME)?.findFile("${logFile?.name!!.substring(0, logFile?.name!!.lastIndexOf('.'))}_$suffix.csv")
    }

    override fun onCompleted(isSuccessful: Boolean) {
        doAsync {
            uiThread {
                Toast.makeText(
                    requireContext(),
                    "Saved to ${csvWriter?.filePath}",
                    Toast.LENGTH_LONG
                ).show()
                csvWriter = null
            }
        }
    }
}
