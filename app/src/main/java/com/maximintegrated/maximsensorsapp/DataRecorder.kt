package com.maximintegrated.maximsensorsapp

import android.content.Context
import android.net.Uri
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.polar.HeartRateMeasurement
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import timber.log.Timber

class DataRecorder(var fileName: String, val timestamp: Long, context: Context) {

    companion object {
        const val LOG_VERSION = 1

        private val CSV_HEADER_HSP_1Hz = arrayOf("timestamp", "hr")
        private val CSV_HEADER_HR_REF_DEVICE =
            arrayOf("timestamp", "heart_rate", "contact_detected")
    }

    private val csvWriter: CsvWriter
    private val csvWriter1Hz: CsvWriter
    private var count = 1

    private val csvWriterHRRefDevice: CsvWriter

    private var oneHzFileIsFinished = false
    private var hrRefFileIsFinished = false

    var dataRecorderListener: DataRecorderListener? = null

    var packetLossOccurred = false

    init {
        csvWriter = CsvWriter.open(
            makeCsvFilePath(getDirectoryReference(RAW_DIRECTORY_NAME)!!, fileName, timestamp),
            HspStreamData.CSV_HEADER_HSP,
            LOG_VERSION,
            context
        )

        csvWriter1Hz = CsvWriter.open(
            makeCsvFilePath(
                getDirectoryReference(ONE_HZ_DIRECTORY_NAME)!!, fileName + ONE_HZ_SUFFIX, timestamp
            ),
            CSV_HEADER_HSP_1Hz,
            context = context
        )

        csvWriterHRRefDevice = CsvWriter.open(
            makeCsvFilePath(
                getDirectoryReference(HR_REF_DIRECTORY_NAME)!!, fileName + HR_REF_SUFFIX, timestamp
            ),
            CSV_HEADER_HR_REF_DEVICE,
            context = context
        )
    }

    private var oneHzListener = object : CsvWriter.Companion.CsvWriterListener {
        override fun onCompleted(isSuccessful: Boolean) {
            if (!isSuccessful) return
            oneHzFileIsFinished = true
            if (hrRefFileIsFinished) {
                dataRecorderListener?.onFilesAreReadyForAlignment(
                    makeCsvFilePath(
                        getDirectoryReference(ALIGNED_DIRECTORY_NAME)!!, fileName + ALIGNED_SUFFIX,
                        timestamp
                    ),
                    csvWriter1Hz.filePath,
                    csvWriterHRRefDevice.filePath
                )
                oneHzFileIsFinished = false
                hrRefFileIsFinished = false
            }
        }
    }

    private var hrRefListener = object : CsvWriter.Companion.CsvWriterListener {
        override fun onCompleted(isSuccessful: Boolean) {
            if (!isSuccessful) return
            hrRefFileIsFinished = true
            if (oneHzFileIsFinished) {
                dataRecorderListener?.onFilesAreReadyForAlignment(
                    makeCsvFilePath(
                        getDirectoryReference(ALIGNED_DIRECTORY_NAME)!!, fileName + ALIGNED_SUFFIX,
                        timestamp
                    ),
                    csvWriter1Hz.filePath,
                    csvWriterHRRefDevice.filePath
                )
                oneHzFileIsFinished = false
                hrRefFileIsFinished = false
            }
        }
    }

    fun record(data: HspStreamData) {
        csvWriter.write(data.toCsvModel())

        if (count % 25 == 0) {
            csvWriter1Hz.write(
                data.currentTimeMillis,
                data.hr
            )
            count = 0
        }

        count++
    }

    fun record(data: HeartRateMeasurement) {
        csvWriterHRRefDevice.write(
            data.currentTimeMillis,
            data.heartRate,
            if (data.contactDetected == true) 1 else 0
        )
    }

    fun close() {
        csvWriter1Hz.listener = oneHzListener
        csvWriterHRRefDevice.listener = hrRefListener
        try {
            csvWriter.close(packetLossOccurred)
            csvWriter1Hz.close(packetLossOccurred)
            csvWriterHRRefDevice.close(packetLossOccurred)
        } catch (e: Exception) {
            Timber.tag(DataRecorder::class.java.simpleName).e(e.message.toString())
        }
    }

    interface DataRecorderListener {
        fun onFilesAreReadyForAlignment(
            alignedFilePath: Uri,
            maxim1HzFilePath: Uri,
            refFilePath: Uri
        )
    }
}