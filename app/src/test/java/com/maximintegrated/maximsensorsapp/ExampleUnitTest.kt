package com.maximintegrated.maximsensorsapp

import android.content.ContentResolver
import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.maximintegrated.hsp.HspStreamData
import com.maximintegrated.maximsensorsapp.exts.CsvWriter
import com.maximintegrated.maximsensorsapp.exts.convertToHspData
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun convert1HzDataTo25HzData(context: Context) {
        val file = File("meizu_stress.csv")
        val documentFile = DocumentFile.fromFile(file)
        assert(documentFile.exists())
        val inputs = readAlgorithmInputsFromFile(documentFile, context.contentResolver)
        val csvWriter = CsvWriter.open(
            documentFile.uri,
            HspStreamData.CSV_HEADER_HSP,
            DataRecorder.LOG_VERSION,
            context
        )
        runBlocking {
            var samplecount = 1
            for (input in inputs) {
                val realData = input.convertToHspData()
                realData.currentTimeMillis = 0
                for (i in 1..24) {
                    val copy = realData.copy()
                    copy.rrConfidence = 0
                    copy.rr = 0f
                    copy.sampleCount = samplecount++
                    csvWriter.write(copy.toCsvModel())
                }
                realData.sampleCount = samplecount++
                csvWriter.write(realData.toCsvModel())
            }
            csvWriter.close()
        }
        assert(File("meizu_stress_25Hz.csv").exists())
    }
}
