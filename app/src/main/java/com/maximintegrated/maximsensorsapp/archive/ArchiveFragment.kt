package com.maximintegrated.maximsensorsapp.archive

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.algorithms.AlgorithmOutput
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.algorithms.sleep.SleepAlgorithmInitConfig
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import com.maximintegrated.maximsensorsapp.exts.addFragment
import com.maximintegrated.maximsensorsapp.exts.ioThread
import com.maximintegrated.maximsensorsapp.profile.UserViewModel
import com.maximintegrated.maximsensorsapp.profile.UserViewModelFactory
import com.maximintegrated.maximsensorsapp.profile.toAlgorithmUser
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Sleep
import com.maximintegrated.maximsensorsapp.sleep.utils.postProcessingWithEncodedData
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
import kotlinx.android.synthetic.main.fragment_archive.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.Comparator


class ArchiveFragment : RecyclerViewClickListener, Fragment(),
    CsvWriter.Companion.CsvWriterListener {

    var algorithmInitConfig = AlgorithmInitConfig()

    private var algorithmResult = false

    companion object {

        enum class CSV_SLEEP_ORDER {
            SLEEP_TIMESTAMP,
            SLEEP_isSleep,
            SLEEP_latency,
            SLEEP_sleepWakeOutput,
            SLEEP_sleepPhasesReady,
            SLEEP_sleepPhasesOutput,
            SLEEP_encodedOutput_sleepPhaseOutput,
            SLEEP_encodedOutput_duration,
            SLEEP_encodedOutput_needsStorage,
            SLEEP_hr,
            SLEEP_ibi,
            SLEEP_spo2,
            SLEEP_accMag,
            SLEEP_sleepRestingHR,
            SLEEP_sleepPhasesOutputProcessed,
            SLEEP_userId
        }

        private val CSV_HEADER_SLEEP = arrayOf(
            "Timestamp",
            "wake_decision_status",
            "latency",
            "wake_decision",
            "phase_output_status",
            "phase_output",
            "hr",
            "ibi",
            "spo2",
            "acc_magnitude",
            "phase_output_processed"
        )

        fun newInstance() = ArchiveFragment()
    }

    private val adapter: FileListAdapter by lazy { FileListAdapter(this) }
    private val algorithmOutput = AlgorithmOutput()
    private var sleepList: ArrayList<Sleep> = arrayListOf()

    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_archive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProviders.of(
            requireActivity(), UserViewModelFactory(
                (requireActivity().application as MaximSensorsApp).userDao
            )
        ).get(UserViewModel::class.java)

        val inputDirectory = getDirectoryReference(RAW_DIRECTORY_NAME)
        val files = inputDirectory!!.listFiles().toList()

        initRecyclerView()

        adapter.fileList = files.sortedWith(Comparator<DocumentFile> { file1, file2 ->
            when {
                file1.lastModified() > file2.lastModified() -> -1
                file1.lastModified() < file2.lastModified() -> 1
                else -> 0
            }
        }).toMutableList()

        adapter.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        fileRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        fileRecyclerView.adapter = adapter
    }

    private fun handleListItemClick(file: DocumentFile) {
        requireActivity().addFragment(OfflineDataFragment.newInstance(file))
    }

    override fun onRowClicked(file: DocumentFile) {
        handleListItemClick(file)
    }

    override fun onDeleteClicked(file: DocumentFile) {
        showDeleteDialog(file)
    }

    override fun onShareClicked(file: DocumentFile) {

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "vnd.android.cursor.dir/email"
            setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, "MaximSensorsApp Csv File")
            putExtra(Intent.EXTRA_TEXT, "File Name: ${file.name}")
            val uri = file.uri
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        startActivity(intent)
    }

    override fun onSleepClicked(file: DocumentFile) {
        progressBar.visibility = View.VISIBLE
        doAsync {
            algorithmResult = runSleepAlgorithm(file)
        }
    }


    override fun onAddInfoClicked(file: DocumentFile) {

        val input = EditText(requireContext())
        input.hint = "SaO2 (%)"
        input.inputType = InputType.TYPE_CLASS_NUMBER

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Blood Gas Measurement").setView(input)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                // Here you get get input text from the Edittext
                var saO2Value = input.text.toString().toIntOrZero()
                ioThread {
                    val stringBuilder = StringBuilder()
                    val lastIndex = file.name!!.lastIndexOf('.')
                    stringBuilder.append(file.name!!.substring(0, lastIndex)) //file name without extension
                    stringBuilder.append("_sao2_$saO2Value")
                    stringBuilder.append(file.name!!.substring(lastIndex))
                    file.renameTo(stringBuilder.toString())
                }
                adapter.notifyDataSetChanged()
            }).setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            .setCancelable(false)
            .show()
    }

    private fun showDeleteDialog(file: DocumentFile) {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Delete File")
        alertDialog.setMessage("Are you sure you want to delete this file ?")
            .setPositiveButton("Delete") { dialog, which ->
                val deleted = file.delete()
                if (deleted) {
                    adapter.fileList.remove(file)
                    adapter.notifyDataSetChanged()
                }
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun runSleepAlgorithm(file: DocumentFile): Boolean {
        sleepList.clear()

        val oneHzFile = getDirectoryReference(ONE_HZ_DIRECTORY_NAME)!!.findFile(
            "${
                file.name!!.substring(
                    0,
                    file.name!!.lastIndexOf('.')
                )
            }${ONE_HZ_SUFFIX}.csv"
        )
        val user = userViewModel.getCurrentUser()

        var sleepFound = false
        val newFileName = "${
            file.name!!.substring(
                0,
                file.name!!.lastIndexOf('.')
            )
        }_${user.username}.${file.name!!.substring(file.name!!.lastIndexOf('.') + 1)}"
        val outputFile = SQA_OUTPUT_DIRECTORY.createFile("application/maxim", file.name!!)

        if (user.sleepRestingHr == 0) {
            var hrSum = 0
            var hrCount = 0
            var restingHr = 0f

            if (oneHzFile!!.exists()) {
                var reader = CsvReader()
                reader.setContainsHeader(true)
                try {
                    var fileReader =
                        context!!.contentResolver.openInputStream(oneHzFile.uri)!!.reader()
                    var parser = reader.parse(fileReader)
                    var row: CsvRow? = parser.nextRow()
                    while (row != null) {
                        if (row.fieldCount < 2) {
                            row = parser.nextRow()
                            continue
                        }
                        val hr = row.getField(1).toFloatOrNull()?.toInt()
                        if (hr != null && hr != 0) {
                            hrSum += hr
                            hrCount++
                        }
                        row = parser.nextRow()
                    }

                    if (hrCount != 0) {
                        restingHr = hrSum * 1f / hrCount
                    }
                    initSleepAlgorithm(restingHr)

                    reader = CsvReader()
                    fileReader = context!!.contentResolver.openInputStream(file.uri)!!.reader()
                    parser = reader.parse(fileReader)
                    row = parser.nextRow()
                    var version = 0
                    if (row?.getField(0)?.startsWith(CsvWriter.LOG_VERSION_HEADER) == true) {
                        version = if (row.fieldCount >= 2) row.getField(1).toIntOrZero() else 0
                        parser.nextRow()
                    }
                    row = parser.nextRow()
                    while (row != null) {
                        val input = csvRowToAlgorithmInput(version, row)
                        sleepFound = runSleepAlgorithm(input, sleepFound)
                        row = parser.nextRow()
                    }

                } catch (e: Exception) {
                    Timber.d("Exception: $e")
                }
            } else if (file.exists()) {

                val inputs = readAlgorithmInputsFromFile(file, context!!.contentResolver)

                restingHr = inputs.filter { it.hr != 0 }.map { it.hr }.average().toFloat()

                initSleepAlgorithm(restingHr)

                for (input in inputs) {
                    sleepFound = runSleepAlgorithm(input, sleepFound)
                }
                inputs.clear()
            }
        } else {

            initSleepAlgorithm(user.sleepRestingHr.toFloat())

            try {
                val reader = CsvReader()
                val fileReader = context!!.contentResolver.openInputStream(file.uri)!!.reader()
                val parser = reader.parse(fileReader)
                var row = parser.nextRow()
                var version = 0
                if (row?.getField(0)?.startsWith(CsvWriter.LOG_VERSION_HEADER) == true) {
                    version = if (row.fieldCount >= 2) row.getField(1).toIntOrZero() else 0
                    parser.nextRow()
                }
                row = parser.nextRow()
                while (row != null) {
                    val input = csvRowToAlgorithmInput(version, row)
                    sleepFound = runSleepAlgorithm(input, sleepFound)
                    row = parser.nextRow()
                }

            } catch (e: Exception) {
                Timber.d("Exception: $e")
            }
        }

        MaximAlgorithms.end(MaximAlgorithms.FLAG_SLEEP)

        if (sleepList.isNotEmpty() && sleepFound) {
//            postProcessingWithOldData(sleepList)
            postProcessingWithEncodedData(sleepList)
            val csvWriter = CsvWriter.open(outputFile!!.uri, context = requireContext())
            csvWriter.listener = this

            for (sleep in sleepList) {
                csvWriter.write(
                    SLEEP_LOG_TIMESTAMP_FORMAT.format(sleep.date),
                    sleep.isSleep,
                    sleep.latency,
                    sleep.sleepWakeOutput,
                    sleep.sleepPhasesReady,
                    sleep.sleepPhasesOutput,
                    sleep.encodedOutput_sleepPhaseOutput,
                    sleep.encodedOutput_duration,
                    sleep.encodedOutput_needsStorage,
                    sleep.hr,
                    sleep.ibi,
                    sleep.spo2,
                    sleep.accMag,
                    sleep.sleepRestingHR,
                    sleep.sleepPhasesOutputProcessed,
                    sleep.userId
                )
            }
            csvWriter.close()
            user.sleepRestingHr = sleepList.last().sleepRestingHR.toInt()
            userViewModel.updateUser(user)
        } else {
            onCompleted(false)
        }

        return sleepFound
    }

    private fun initSleepAlgorithm(restingHr: Float) {
        val user = userViewModel.getCurrentUser().toAlgorithmUser()
        user.sleepRestingHr = restingHr

        algorithmInitConfig.sleepConfig = SleepAlgorithmInitConfig(
            SleepAlgorithmInitConfig.DetectableSleepDuration.MINIMUM_30_MIN,
            restingHr != 0f,
            true,
            true,
            true
        )
        algorithmInitConfig.user = user
        algorithmInitConfig.enableAlgorithmsFlag = MaximAlgorithms.FLAG_SLEEP

        MaximAlgorithms.init(algorithmInitConfig)
    }

    private fun runSleepAlgorithm(input: AlgorithmInput?, sleepFound: Boolean): Boolean {
        var sleepState = sleepFound
        var userId: String
        if (input != null) {
            val status = MaximAlgorithms.run(input, algorithmOutput) and MaximAlgorithms.FLAG_SLEEP
            if (status == 0) {
                with(algorithmOutput.sleep) {
                    if (outputDataArrayLength > 0) {
                        if (output.sleepWakeDetentionLatency >= 0) {
                            sleepState = true
                        } else {
                            output.sleepWakeDetentionLatency = -1
                        }
                        userId = if (sleepList.isEmpty()) DeviceSettings.selectedUserId else ""
                        val sleep = Sleep(
                            id = 0,
                            sourceId = 0,
                            userId = userId,
                            date = Date(dateInfo),
                            isSleep = output.sleepWakeDecisionStatus,
                            latency = output.sleepWakeDetentionLatency,
                            sleepWakeOutput = output.sleepWakeDecision,
                            sleepPhasesReady = output.sleepPhaseOutputStatus,
                            sleepPhasesOutput = output.sleepPhaseOutput,
                            encodedOutput_sleepPhaseOutput = output.encodedOutput_sleepPhaseOutput,
                            encodedOutput_duration = output.encodedOutput_duration,
                            encodedOutput_needsStorage = output.isEncodedOutput_needsStorage,
                            hr = output.hr.toDouble(),
                            ibi = output.ibi.toDouble(),
                            spo2 = 0,
                            accMag = output.accMag.toDouble(),
                            sleepRestingHR = output.sleepRestingHR,
                            sleepPhasesOutputProcessed = 0
                        )
                        sleepList.add(sleep)
                    }
                }

            }
        }
        return sleepState
    }

    private fun showSleepAlgorithmResultDialog(success: Boolean) {
        val message = if (success) {
            getString(R.string.algorithm_finished_successfully)
        } else {
            getString(R.string.algorithm_not_found_sleep_state)
        }
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        alertDialog.setTitle(getString(R.string.sleep_algorithm_result))
        alertDialog.setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                dialog.dismiss()
            }

        alertDialog.show()
    }

    override fun onCompleted(isSuccessful: Boolean) {
        doAsync {
            uiThread {
                progressBar.visibility = View.GONE
                showSleepAlgorithmResultDialog(algorithmResult)
            }
        }
    }
}
