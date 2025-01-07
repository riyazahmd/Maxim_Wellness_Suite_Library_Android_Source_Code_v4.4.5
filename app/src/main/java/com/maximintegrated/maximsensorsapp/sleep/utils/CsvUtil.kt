package com.maximintegrated.maximsensorsapp.sleep.utils

import android.app.Application
import com.maximintegrated.maximsensorsapp.DeviceSettings
import com.maximintegrated.maximsensorsapp.archive.ArchiveFragment
import com.maximintegrated.maximsensorsapp.archive.ArchiveFragment.Companion.CSV_SLEEP_ORDER.*
import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Sleep
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Source
import com.maximintegrated.maximsensorsapp.sleep.database.repository.SleepRepository
import com.maximintegrated.maximsensorsapp.sleep.database.repository.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.lang.Exception
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList

class CsvUtil {

    companion object {

        suspend fun importFromCsv(application: Application, file: DocumentFile) {

            val md5 = calculateMD5(file, application.contentResolver)
            Timber.d("CsvUtil: File = ${file.name}")

            val source = Source(0, file.name!!, md5!!)

            val sourceRepo = SourceRepository(application)
            val sleepRepo = SleepRepository(application)
            sourceRepo.deleteByFileName(file.name!!)

            val sourceId = sourceRepo.insert(source)
            var lines: List<String>

            withContext(Dispatchers.IO) {
                lines = application.contentResolver.openInputStream(file.uri)!!.reader().readLines()
                val sleepList = ArrayList<Sleep>()

                for (line in lines) {
                    val arr = line.split(",")
                    val dateArr = arr[SLEEP_TIMESTAMP.ordinal].replace("'", "").split("/")
                    val dateCal: Calendar = Calendar.getInstance()

                    dateCal.set(
                        dateArr[0].toInt(),
                        dateArr[1].toInt() - 1,
                        dateArr[2].toInt(),
                        dateArr[3].toInt(),
                        dateArr[4].toInt(),
                        dateArr[5].toInt()
                    )

                    val date = Date(dateCal.timeInMillis)

                    try {
                        val sleepPhasesOutputProcessed = if (arr.size > 10) {
                            arr[SLEEP_sleepPhasesOutputProcessed.ordinal].trim().toIntOrNull()
                                ?: arr[SLEEP_sleepPhasesOutput.ordinal].trim().toInt()
                        } else {
                            arr[SLEEP_sleepPhasesOutput.ordinal].trim().toInt()
                        }

                        sleepList.add(
                            Sleep(
                                id = 0,
                                sourceId = sourceId,
                                userId = arr[SLEEP_userId.ordinal],
                                date = date,
                                isSleep = arr[SLEEP_isSleep.ordinal].trim().toInt(),
                                latency = arr[SLEEP_latency.ordinal].trim().toInt(),
                                sleepWakeOutput = arr[SLEEP_sleepWakeOutput.ordinal].trim().toInt(),
                                sleepPhasesReady = arr[SLEEP_sleepPhasesReady.ordinal].trim()
                                    .toInt(),
                                sleepPhasesOutput = arr[SLEEP_sleepPhasesOutput.ordinal].trim()
                                    .toInt(),
                                encodedOutput_sleepPhaseOutput = arr[SLEEP_encodedOutput_sleepPhaseOutput.ordinal].trim()
                                    .toInt(),
                                encodedOutput_duration = arr[SLEEP_encodedOutput_duration.ordinal].trim()
                                    .toInt(),
                                encodedOutput_needsStorage = arr[SLEEP_encodedOutput_needsStorage.ordinal].trim()
                                    .toBoolean(),
                                hr = arr[SLEEP_hr.ordinal].trim().toDouble(),
                                ibi = arr[SLEEP_ibi.ordinal].trim().toDouble(),
                                spo2 = arr[SLEEP_spo2.ordinal].trim().toInt(),
                                accMag = arr[SLEEP_accMag.ordinal].trim().toDouble(),
                                sleepRestingHR = arr[SLEEP_sleepRestingHR.ordinal].trim().toFloat(),
                                sleepPhasesOutputProcessed = sleepPhasesOutputProcessed
                            )
                        )
                    } catch (e: Exception) {
                        Timber.d("Exception: $e")
                    }
                }
                sleepRepo.insertAll(sleepList)
            }
        }

        private fun calculateMD5(updateFile: DocumentFile, contentResolver: ContentResolver): String? {
            val digest: MessageDigest
            try {
                digest = MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                Timber.e(e, "Exception while getting digest")
                return null
            }

            val inputStream: InputStream
            try {
                inputStream = contentResolver.openInputStream(updateFile.uri)!!
            } catch (e: FileNotFoundException) {
                Timber.e(e, "Exception while getting FileInputStream")
                return null
            }

            val buffer = ByteArray(8192)
            var read = 0
            try {

                while ({ read = inputStream.read(buffer); read }() > 0) {
                    digest.update(buffer, 0, read)
                }

                val md5sum = digest.digest()
                val bigInt = BigInteger(1, md5sum)
                var output = bigInt.toString(16)

                output = String.format("%32s", output).replace(' ', '0')
                return output
            } catch (e: IOException) {
                throw RuntimeException("Unable to process file for MD5", e)
            } finally {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    Timber.e(e, "Exception on closing MD5 input stream")
                }
            }
        }

        fun listCalculateMD5(list: List<DocumentFile>, contentResolver: ContentResolver): List<String> {
            val resultList = ArrayList<String>()
            for (i in list) {
                resultList.add(calculateMD5(i, contentResolver).toString())
            }

            return resultList
        }
    }
}