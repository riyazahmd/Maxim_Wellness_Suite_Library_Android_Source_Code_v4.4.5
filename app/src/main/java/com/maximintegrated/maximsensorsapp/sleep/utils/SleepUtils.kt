package com.maximintegrated.maximsensorsapp.sleep.utils

import com.maximintegrated.algorithms.sleep.SleepAlgorithmOutput
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Sleep

fun postProcessingWithOldData(sleepList: List<Sleep>) {

    //Step 1: Introduce Latency
    for ((i, sleep) in sleepList.withIndex()) {
        if (sleep.latency > 0) {
            for (j in 0 until sleep.latency * 60) {
                if (i - j > 0) {
                    sleepList[i - j].sleepWakeOutput = sleep.sleepWakeOutput
                    sleepList[i - j].sleepPhasesOutput =
                        SleepAlgorithmOutput.SleepPhaseOutput.LIGHT.value
                }
            }
        }
    }

    //Step 2: Add wake to sleep phase
    for (sleep in sleepList) {
        //Consider wake and restless as wake phase in sleep phase
        if (sleep.sleepWakeOutput == SleepAlgorithmOutput.SleepWakeDecision.WAKE.value ||
            sleep.sleepWakeOutput == SleepAlgorithmOutput.SleepWakeDecision.RESTLESS.value
        ) {
            sleep.sleepPhasesOutputProcessed = SleepAlgorithmOutput.SleepPhaseOutput.WAKE.value
        } else {
            sleep.sleepPhasesOutputProcessed = sleep.sleepPhasesOutput
        }
    }

    //Step 3: fill undefined values with nearest value
    var i = 0
    while (i < sleepList.size) {
        if (sleepList[i].sleepPhasesOutputProcessed == SleepAlgorithmOutput.SleepPhaseOutput.UNDEFINED.value) {
            //find the undefined interval
            var j = 0
            while (j < sleepList.size - i) {
                if (sleepList[i + j].sleepPhasesOutput != SleepAlgorithmOutput.SleepPhaseOutput.UNDEFINED.value) {
                    break
                }
                j++
            }
            for (k in 0 until j) {
                if ((k < j / 2 && i > 1) //if k belongs to first half and there is a previous value
                    || (i + j == sleepList.size)
                ) { //if counter reached to the end of array use only previous value
                    //fill the first half with previous value
                    sleepList[i + k].sleepPhasesOutputProcessed =
                        sleepList[i - 1].sleepPhasesOutputProcessed
                } else {
                    //fill the second half with next value
                    sleepList[i + k].sleepPhasesOutputProcessed =
                        sleepList[i + j].sleepPhasesOutputProcessed
                }
            }

            i += j
        }
        i++
    }
}


fun postProcessingWithEncodedData(sleepList: List<Sleep>) {
    var filledIndex = 0
    for ((i, sleep) in sleepList.withIndex()) {
        if (sleep.encodedOutput_needsStorage) {
            val numberOfRowsNeedsToBeFilled = sleep.encodedOutput_duration
            val currentSleepPhaseOutput = sleep.encodedOutput_sleepPhaseOutput
            for (currentIndex in 1..numberOfRowsNeedsToBeFilled) {
                sleepList[filledIndex++].sleepPhasesOutputProcessed = currentSleepPhaseOutput
            }
        }
        if (i == sleepList.size) {
            while (filledIndex != i) {
                sleepList[filledIndex++].sleepPhasesOutputProcessed =
                    sleep.encodedOutput_sleepPhaseOutput
            }
        }
    }
}