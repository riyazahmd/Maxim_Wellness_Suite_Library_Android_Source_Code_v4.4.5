package com.maximintegrated.maximsensorsapp.bpt

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import com.maximintegrated.maximsensorsapp.*
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
import timber.log.Timber
import java.io.OutputStream

var HISTORY_FILE: DocumentFile? = null
    get() = getDirectoryReference(BPT_DIRECTORY_NAME)!!.findFile(BptSettings.currentUser)
        ?.findFile("${BPT_HISTORY_FILE_NAME}.csv")


var CALIBRATION_FILE: DocumentFile? = null
    get() = getDirectoryReference(BPT_DIRECTORY_NAME)!!.findFile(BptSettings.currentUser)
        ?.findFile("${BPT_CALIBRATION_DATA_FILE_NAME}.txt")


const val NUMBER_OF_REFERENCES = 3
const val NUMBER_OF_FEATURES = 3
const val PPG_TEMPLATE_LENGTH = 50
const val MAX_NUMBER_OF_CALIBRATION = 5
const val SUGGESTED_NUMBER_OF_CALIBRATION = 3
const val NUMBER_OF_SAMPLES_FOR_CHART = 150
const val appendableWrite = "wa"
/**
 * Append the given history data to HISTORY_FILE. If there is no such file, it creates a new one for
 * the current user and writes the data there.
 * @param historyData history data to be written to HISTORY_FILE
 * @param contentResolver content resolver that is needed to access HISTORY_FILE.
 */
fun saveHistoryData(historyData: BptHistoryData, contentResolver: ContentResolver) {
    var outputStream: OutputStream
    if (HISTORY_FILE == null) {
        var userDirectory =
            getDirectoryReference(BPT_DIRECTORY_NAME)!!.findFile(BptSettings.currentUser)

        if (userDirectory == null) {
            userDirectory =
                getDirectoryReference(BPT_DIRECTORY_NAME)!!.createDirectory(BptSettings.currentUser)
        }

        HISTORY_FILE = userDirectory!!.findFile("${BPT_HISTORY_FILE_NAME}.csv")
        if (HISTORY_FILE == null) {
            HISTORY_FILE = userDirectory.createFile("text/csv", "${BPT_HISTORY_FILE_NAME}.csv")
        }
        outputStream = contentResolver.openOutputStream(HISTORY_FILE!!.uri, appendableWrite)!!
        outputStream.writer().append(BptHistoryData.CSV_HEADER_ARRAY.joinToString(",") + "\n")
            .close()
    }
    outputStream = contentResolver.openOutputStream(HISTORY_FILE!!.uri, appendableWrite)!!
    outputStream.writer().append(historyData.toText()).close()
}

/**
 * Reads history data from HISTORY_FILE and returns a list of BptHistoryData where each item includes
 * row data that is parsed from HISTORY_FILE. If HISTORY_FILE does not exists then returns an empty
 * list.
 * @param contentResolver content resolver for reading the contents of HISTORY_FILE.
 * @return list of BptHistoryData, each item of the list contains row data that is parsed from
 *         HISTORY_FILE.
 */
fun readHistoryData(contentResolver: ContentResolver): List<BptHistoryData> {
    val list: ArrayList<BptHistoryData> = arrayListOf()
    if (HISTORY_FILE?.exists() != true) {
        return list
    }
    val reader = CsvReader()
    reader.setContainsHeader(true)
    try {
        val parser = reader.parse(contentResolver.openInputStream(HISTORY_FILE!!.uri)!!.reader())
        var row: CsvRow? = parser.nextRow()
        while (row != null) {
            val timestamp = row.getField(0).toLongOrZero()
            val sbp = row.getField(1).toIntOrZero()
            val dbp = row.getField(2).toIntOrZero()
            val hr = row.getField(3).toIntOrZero()
            val spo2 = row.getField(4).toIntOrZero()
            val pulseFlag = row.getField(5).toIntOrZero()
            val isCalibration = row.getField(6) == "Calibration"
            val data = BptHistoryData(timestamp, isCalibration, sbp, dbp, hr, spo2, pulseFlag)
            list.add(data)
            row = parser.nextRow()
        }
    } catch (e: Exception) {
        Timber.d("Exception: $e")
    }
    return list
}

/**
 * Saves the calibration data to CALIBRATION_FILE, if there is no such file then it creates a new file
 * and write the new data there.
 * @param calibrationInHexString calibration data in hex string format
 * @param timestamp timestamp that is added to the row in order to signify when the data is read
 * @param sbp systolic blood pressure value
 * @param dbp diastolic blood pressure value
 * @param contentResolver content resolver to access and write to the content of CALIBRATION_FILE
 */
fun saveCalibrationData(
    calibrationInHexString: String,
    timestamp: Long,
    sbp: Int,
    dbp: Int,
    contentResolver: ContentResolver
) {
    if (CALIBRATION_FILE == null) {
        var userDirectory =
            getDirectoryReference(BPT_DIRECTORY_NAME)!!.findFile(BptSettings.currentUser)

        if (userDirectory == null) {
            userDirectory =
                getDirectoryReference(BPT_DIRECTORY_NAME)!!.createDirectory(BptSettings.currentUser)
        }

        CALIBRATION_FILE = userDirectory!!.findFile("${BPT_CALIBRATION_DATA_FILE_NAME}.txt")
        if (CALIBRATION_FILE == null) {
            CALIBRATION_FILE =
                userDirectory.createFile("text/plain", "${BPT_CALIBRATION_DATA_FILE_NAME}.txt")
        }
    }
    val outputStream = contentResolver.openOutputStream(CALIBRATION_FILE!!.uri, appendableWrite)!!
    outputStream.writer().append("$calibrationInHexString $timestamp $sbp $dbp\n").close()
}

/**
 * Reads the calibration data from CALIBRATION_FILE, which is the calibration file of the user
 * @param contentResolver content resolver to read the content of the CALIBRATION_FILE
 * @return returns the parsed list of BptCalibrationData if CALIBRATION_FILE exists
 *         returns empty list if there is no CALIBRATION_FILE
 */
fun readCalibrationData(contentResolver: ContentResolver): List<BptCalibrationData> {
    val list: ArrayList<BptCalibrationData> = arrayListOf()

    if (CALIBRATION_FILE?.exists() != true) {
        return list
    }

    val lines = contentResolver.openInputStream(CALIBRATION_FILE!!.uri)!!.reader().readLines()
    if (lines.isEmpty()) {
        return arrayListOf()
    }

    return lines.map { BptCalibrationData.parseCalibrationDataFromString(it) }
}
