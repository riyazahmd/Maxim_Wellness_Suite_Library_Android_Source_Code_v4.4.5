package com.maximintegrated.maximsensorsapp.sports_coaching

import android.content.ContentResolver
import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.maximintegrated.algorithms.sports.SportsCoachingAlgorithmOutput
import com.maximintegrated.algorithms.sports.SportsCoachingHistory
import com.maximintegrated.algorithms.sports.SportsCoachingHistoryItem
import com.maximintegrated.algorithms.sports.SportsCoachingSession
import com.maximintegrated.maximsensorsapp.*
import com.maximintegrated.maximsensorsapp.exts.ioThread

fun getSportsCoachingOutputFile(username: String, timestamp: String, type: String): DocumentFile? {
    var userDirectory = getDirectoryReference(SPORTS_COACHING_DIRECTORY_NAME)!!.findFile(username)
    if (userDirectory == null){
        userDirectory = getDirectoryReference(SPORTS_COACHING_DIRECTORY_NAME)!!.createDirectory(username)
    }

    var outputFile = userDirectory!!.findFile("${BASE_FILE_NAME_PREFIX}${timestamp}_${type}${OUT_SUFFIX}.json")
    if (outputFile == null){
       outputFile = userDirectory.createFile("application/json", "${BASE_FILE_NAME_PREFIX}${timestamp}_${type}${OUT_SUFFIX}.json")
    }

    return outputFile
}

fun saveMeasurement(
    username: String,
    output: SportsCoachingAlgorithmOutput,
    timestamp: String,
    type: String,
    contentResolver: ContentResolver
) {
    val file = getSportsCoachingOutputFile(username, timestamp, type)
    val json = Gson().toJson(output)
    ioThread {
        contentResolver.openOutputStream(file!!.uri)!!.writer().use { out ->
            out.write(json)
        }
    }
}

fun getHistoryFromFiles(username: String, contentResolver: ContentResolver): SportsCoachingHistory {
    val outputs = getSportsCoachingOutputsFromFiles(username, contentResolver)
    if (outputs.isEmpty()) {
        return SportsCoachingHistory(0)
    }

    val historyList =
        outputs.filter { it.session != SportsCoachingSession.VO2MAX_FROM_HISTORY }.map {
            SportsCoachingHistoryItem(
                it.timestamp,
                it.estimates,
                it.hrStats,
                it.session
            )
        }

    return SportsCoachingHistory(historyList as ArrayList<SportsCoachingHistoryItem>)
}

fun getSportsCoachingOutputsFromFiles(username: String, contentResolver: ContentResolver): ArrayList<SportsCoachingAlgorithmOutput> {
    val outputs: ArrayList<SportsCoachingAlgorithmOutput> = arrayListOf()
    val inputDirectory = getDirectoryReference(SPORTS_COACHING_DIRECTORY_NAME)!!.findFile(username)
        ?: return outputs
    val files = inputDirectory.listFiles().toList().sortedWith(Comparator<DocumentFile> { file1, file2 ->
        when {
            file1.lastModified() > file2.lastModified() -> -1
            file1.lastModified() < file2.lastModified() -> 1
            else -> 0
        }
    }).toMutableList()
    val gson = Gson()
    for (file in files) {
        val reader = contentResolver.openInputStream(file.uri)!!.reader()
        val json = reader.readText()
        reader.close()

        val output = gson.fromJson<SportsCoachingAlgorithmOutput>(
            json,
            SportsCoachingAlgorithmOutput::class.java
        )
        if (output != null) {
            outputs.add(output)
        }
    }
    return outputs
}

fun getStringValueOfSession(context: Context, session: SportsCoachingSession): String {
    return when (session) {
        SportsCoachingSession.VO2MAX_RELAX -> context.getString(R.string.vo2max)
        SportsCoachingSession.UNDEFINED -> context.getString(R.string.undefined)
        SportsCoachingSession.VO2 -> context.getString(R.string.undefined)
        SportsCoachingSession.RECOVERY_TIME -> context.getString(R.string.recovery_time)
        SportsCoachingSession.READINESS -> context.getString(R.string.readiness)
        SportsCoachingSession.VO2MAX_FROM_HISTORY -> context.getString(R.string.fitness_age)
        SportsCoachingSession.EPOC_RECOVERY -> context.getString(R.string.epoc_recovery)
    }
}

fun getScore(output: SportsCoachingAlgorithmOutput): Number {
    return when (output.session ?: SportsCoachingSession.UNDEFINED) {
        SportsCoachingSession.VO2MAX_RELAX -> output.estimates.vo2max.relax
        SportsCoachingSession.UNDEFINED -> 0
        SportsCoachingSession.VO2 -> output.estimates.vo2max.vo2
        SportsCoachingSession.RECOVERY_TIME -> output.estimates.recovery.recoveryTimeMin
        SportsCoachingSession.READINESS -> output.estimates.readiness.readinessScore
        SportsCoachingSession.VO2MAX_FROM_HISTORY -> output.estimates.vo2max.fitnessAge
        SportsCoachingSession.EPOC_RECOVERY -> output.estimates.recovery.epoc
    }
}