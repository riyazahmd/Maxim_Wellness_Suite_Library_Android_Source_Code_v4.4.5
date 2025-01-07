package com.maximintegrated.maximsensorsapp

import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.hsp.HspViewModel
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

val FILE_TIMESTAMP_FORMAT = SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", Locale.US)
val SLEEP_LOG_TIMESTAMP_FORMAT = SimpleDateFormat("yyyy/MM/dd/HH/mm/ss", Locale.US)
val USER_TIMESTAMP_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
val decimalFormat = DecimalFormat("0.0")

lateinit var MWA_OUTPUT_DIRECTORY: DocumentFile
lateinit var SQA_OUTPUT_DIRECTORY: DocumentFile

val BPT_DIRECTORY_NAME = "BP Trending"
val ERROR_DIRECTORY_NAME = "ERROR"
val ANNOTATION_DIRECTORY_NAME = "ANNOTATIONS"
val RAW_DIRECTORY_NAME = "RAW"
val ONE_HZ_DIRECTORY_NAME = "1HZ"
val ALIGNED_DIRECTORY_NAME = "ALIGNED"
val ECG_DIRECTORY_NAME = "ECG"
val TEMP_DIRECTORY_NAME = "TEMP"
val RR_REF_DIRECTORY_NAME = "RR_REF"
val HR_REF_DIRECTORY_NAME = "HR_REF"
val SPO2_REF_DIRECTORY_NAME = "SPO2_REF"
val USER_INFO_DIRECTORY_NAME = "USER_INFO"
val ALGO_OUTPUT_DIRECTORY_NAME = "ALGO_OUTPUT"
val SPORTS_COACHING_DIRECTORY_NAME = "SPORTS_COACHING"

val BASE_FILE_NAME_PREFIX = "MaximSensorsApp_"
val BASE_BPT_FILE_NAME_PREFIX = "BPTrending_"

val ANNOTATION_SUFFIX = "_annotation"
val ERROR_SUFFIX = "_error"
val FLASH_SUFFIX = "_flash"
val OUT_SUFFIX = "_out"
val BPT_CALIBRATION_SUFFIX = "_calibration"
val BPT_ESTIMATION_SUFFIX = "_estimation"
val ALIGNED_SUFFIX = "_aligned"
val ONE_HZ_SUFFIX = "_1Hz"
val HR_REF_SUFFIX = "_hr_ref"

val SPO2_REF_FILE_NAME = "spo2_ref"
val ZEPHYR_SUMMARY_FILE_NAME = "rr_ref_summary"
val QARDIO_RESULT_FILE_NAME = "bpt_ref"
val ZEPHYR_BREATH_WAVEFORM_FILE_NAME = "rr_ref_breath_waveform"
val BPT_HISTORY_FILE_NAME = "BPTrending_history"
val BPT_CALIBRATION_DATA_FILE_NAME = "calibration_data"


/**
 * Checks if a folder with the given name exists starting with root directory (MWA_OUTPUT_DIRECTORY)
 * If a directory with given name is not found, then it creates a new directory and returns it
 * @param folderName Requested directory reference's display name
 * @return reference to requested directory
 */
fun getDirectoryReference(folderName: String): DocumentFile? {
    var foundFolder = MWA_OUTPUT_DIRECTORY.findFile(folderName)

    if (foundFolder == null) {
        foundFolder = MWA_OUTPUT_DIRECTORY.createDirectory(folderName)
    }
    return foundFolder
}

/**
 * Creates a CSV file in the given directory with name based on given parameters.
 * Created file name format will be MaximSensorsApp_<formatted timestamp>_<fileName>.csv
 * @param directory parent directory of the created file
 * @param fileName name that will be added at the end of the file name
 * @param timestamp timestamp that will be included in the file name
 * @return reference to created CSV file
 */
fun makeCsvFilePath(directory: DocumentFile, fileName: String, timestamp: Long): Uri {
    return directory.createFile(
        "text/csv",
        "${BASE_FILE_NAME_PREFIX}${FILE_TIMESTAMP_FORMAT.format(timestamp)}_${fileName}.csv"
    )?.uri!!
}

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun String.convertHexStringToByteArray(): ByteArray {
    val array = ByteArray(this.length / 2)
    for (i in array.indices) {
        val index = i * 2
        val j = this.substring(index, index + 2).toIntOrNull(16) ?: 0
        array[i] = j.toByte()
    }
    return array
}

/**
 * Reads and parses the given input CSV file
 * @param file input csv file reference that contains algorithm inputs
 * @param contentResolver content resolver that is used to read content of the given DocumentFile
 *                         in the first parameter
 * @return ArrayList of AlgorithmInput, each entry contains AlgorithmInput object that contains inputs
 */
fun readAlgorithmInputsFromFile(
    file: DocumentFile?,
    contentResolver: ContentResolver
): ArrayList<AlgorithmInput> {
    val inputs: ArrayList<AlgorithmInput> = arrayListOf()
    if (file == null) {
        return inputs
    }
    val reader = CsvReader()
    val fileReader = contentResolver.openInputStream(file.uri)!!.reader()
    try {
        val parser = reader.parse(fileReader)
        var row: CsvRow? = parser.nextRow()
        var version = 0

        if (row?.getField(0)?.startsWith(CsvWriter.LOG_VERSION_HEADER) == true) {
            version = if (row.fieldCount >= 2) row.getField(1).toIntOrZero() else 0
            parser.nextRow()
        }
        row = parser.nextRow()
        while (row != null) {
            val input = csvRowToAlgorithmInput(version, row)
            if (input != null) {
                inputs.add(input)
            }
            row = parser.nextRow()
        }
    } catch (e: Exception) {

    }
    return inputs
}

fun csvRowToAlgorithmInput(version: Int, row: CsvRow): AlgorithmInput? {
    val input = AlgorithmInput()
    if (row.fieldCount < 31) return null
    input.sampleCount = row.getField(0).toIntOrZero()
    input.green = row.getField(2).toFloatOrZero().toInt()
    input.green2 = row.getField(3).toFloatOrZero().toInt()
    input.ir = row.getField(4).toFloatOrZero().toInt()
    input.red = row.getField(5).toFloatOrZero().toInt()
    input.accelerationX = (row.getField(6).toFloatOrZero() * 1000f).toInt()
    input.accelerationY = (row.getField(7).toFloatOrZero() * 1000f).toInt()
    input.accelerationZ = (row.getField(8).toFloatOrZero() * 1000f).toInt()
    input.operationMode = row.getField(9).toFloatOrZero().toInt()
    input.hr = row.getField(10).toFloatOrZero().toInt()
    input.hrConfidence = row.getField(11).toFloatOrZero().toInt()
    input.rr = (row.getField(12).toFloatOrZero() * 10f).toInt()
    input.rrConfidence = row.getField(13).toFloatOrZero().toInt()
    input.activity = row.getField(14).toFloatOrZero().toInt()
    input.r = (row.getField(15).toFloatOrZero() * 1000f).toInt()
    input.wspo2Confidence = row.getField(16).toFloatOrZero().toInt()
    input.spo2 = (row.getField(17).toFloatOrZero() * 10f).toInt()
    input.wspo2PercentageComplete = row.getField(18).toFloatOrZero().toInt()
    input.wspo2LowSnr = row.getField(19).toFloatOrZero().toInt()
    input.wspo2Motion = row.getField(20).toFloatOrZero().toInt()
    input.wspo2LowPi = row.getField(21).toFloatOrZero().toInt()
    input.wspo2UnreliableR = row.getField(22).toFloatOrZero().toInt()
    input.wspo2State = row.getField(23).toFloatOrZero().toInt()
    input.scdState = row.getField(24).toFloatOrZero().toInt()
    input.walkSteps = row.getField(25).toFloatOrZero().toInt()
    input.runSteps = row.getField(26).toFloatOrZero().toInt()
    input.kCal = (row.getField(27).toFloatOrZero() * 10f).toInt()
    input.totalActEnergy = (row.getField(28).toFloatOrZero() * 10f).toInt()
    if (version >= DataRecorder.LOG_VERSION) {
        input.timestamp = row.getField(31).toDoubleOrZero().toLong()
        input.ibiOffset = row.getField(29).toIntOrZero()
    } else {
        input.timestamp = row.getField(30).toDoubleOrZero().toLong()
    }
    return input
}

fun getFormattedTime(elapsedTime: Long): String {
    val hour = TimeUnit.MILLISECONDS.toHours(elapsedTime)
    val min = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60
    val sec = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
    return String.format("%02d:%02d:%02d", hour, min, sec)
}

fun showAlertDialog(
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String,
    action: (() -> Unit)? = null
) {
    val alertDialog = AlertDialog.Builder(context)
    alertDialog.setTitle(title)
    alertDialog.setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            action?.let { it() }
            dialog.dismiss()
        }.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
    alertDialog.setCancelable(true)
    alertDialog.show()
}

fun showCustomAlertDialog(
    context: Context,
    title: String,
    message: String,
    rightButtonText: String,
    leftButtonText: String,
    actionRightButton: (() -> Unit)? = null,
    actionLeftButton: (() -> Unit)? = null
) {
    val alertDialog = AlertDialog.Builder(context)
    alertDialog.setTitle(title)
        .setMessage(message)
        .setPositiveButton(rightButtonText) { dialog, _ ->
            actionRightButton?.let { it() }
            dialog.dismiss()
        }.setNegativeButton(leftButtonText) { dialog, _ ->
            actionLeftButton?.let { it() }
            dialog.dismiss()
        }.setCancelable(true)
        .show()
}

fun showAlertDialogWithThreeButton(
    context: Context,
    title: String,
    message: String,
    rightButtonText: String,
    middleButtonText: String,
    leftButtonText: String,
    actionRightButton: (() -> Unit)? = null,
    actionMiddleButton: (() -> Unit)? = null,
    actionLeftButton: (() -> Unit)? = null
) {
    val alertDialog = AlertDialog.Builder(context)
    alertDialog.setTitle(title)
        .setMessage(message)
        .setPositiveButton(rightButtonText) { dialog, _ ->
            actionRightButton?.let { it() }
            dialog.dismiss()
        }.setNegativeButton(middleButtonText) { dialog, _ ->
            actionMiddleButton?.let { it() }
            dialog.dismiss()
        }.setNeutralButton(leftButtonText) { dialog, _ ->
            actionLeftButton?.let { it() }
            dialog.dismiss()
        }.setCancelable(true)
        .show()
}

fun String.toIntOrZero(): Int {
    return this.toIntOrNull() ?: 0
}

fun String.toFloatOrZero(): Float {
    return this.toFloatOrNull() ?: 0f
}

fun String.toLongOrZero(): Long {
    return this.toLongOrNull() ?: 0L
}

fun String.toDoubleOrZero(): Double {
    return this.toDoubleOrNull() ?: 0.0
}

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}
