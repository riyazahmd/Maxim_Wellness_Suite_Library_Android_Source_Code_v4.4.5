package com.maximintegrated.maximsensorsapp.sleep

import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Sleep
import com.maximintegrated.maximsensorsapp.sleep.database.entity.SourceAndAllSleeps
import java.util.*
import kotlin.collections.ArrayList

data class SleepDataModel(
    val sourceId: Long,
    val userId: String,
    val date: Date,
    val bedTime: Date,
    val wakeTime: Date,
    val wakeCount: Int,
    val remCount: Int,
    val lightCount: Int,
    val deepCount: Int,
    val totalCount: Int,
    val numberOfWakeInSleep: Int
) {

    val pieDataSet: PieDataSet
        get() {
            val list = ArrayList<PieEntry>()
            list.add(PieEntry(wakeCount.toFloat()))
            list.add(PieEntry(remCount.toFloat()))
            list.add(PieEntry(lightCount.toFloat()))
            list.add(PieEntry(deepCount.toFloat()))
            return PieDataSet(list, "")
        }

    companion object {
        fun parseSleepList(data: SourceAndAllSleeps): SleepDataModel? {
            val sleepList = data.sleepList ?: return null
            return parseSleepList(sleepList, data.source?.id ?: 0L)
        }

        private fun parseSleepList(sleepList: List<Sleep>, sourceId: Long = 0L): SleepDataModel? {
            if(sleepList.isEmpty()) return null
            var inSleepWakeCount = 0
            var remCount = 0
            var lightCount = 0
            var deepCount = 0
            var numberOfWakeInSleep = 0
            var inSleepState = false

            val tempList = sleepList.filter { it.sleepPhasesOutputProcessed >= 2 }

            val bedDate = if (tempList.isNotEmpty()) {
                tempList.first().date
            } else {
                sleepList.first().date
            }

            val wakeDate = if (tempList.isNotEmpty()) {
                tempList.last().date
            } else {
                sleepList.last().date
            }

            for (sleep in sleepList) {
                when (sleep.sleepPhasesOutputProcessed) {
                    -1, 0 -> {
                        if (sleep.date.time in bedDate.time..wakeDate.time) {
                            inSleepWakeCount++
                            if (inSleepState) {
                                numberOfWakeInSleep++
                            }
                        }
                        inSleepState = false
                    }
                    2 -> {
                        inSleepState = true
                        remCount++
                    }
                    3 -> {
                        inSleepState = true
                        lightCount++
                    }
                    4 -> {
                        inSleepState = true
                        deepCount++
                    }
                }
            }

            val totalCount = inSleepWakeCount + remCount + lightCount + deepCount

            return SleepDataModel(
                sourceId,
                sleepList.first().userId,
                sleepList.last().date,
                bedDate,
                wakeDate,
                inSleepWakeCount,
                remCount,
                lightCount,
                deepCount,
                totalCount,
                numberOfWakeInSleep
            )
        }
    }
}