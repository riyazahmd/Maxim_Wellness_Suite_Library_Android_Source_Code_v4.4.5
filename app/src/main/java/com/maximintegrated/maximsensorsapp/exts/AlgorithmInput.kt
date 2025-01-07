package com.maximintegrated.maximsensorsapp.exts

import com.maximintegrated.algorithms.AlgorithmInput
import com.maximintegrated.hsp.HspStreamData

fun AlgorithmInput.set(data: HspStreamData) {
    sampleCount = data.sampleCount
    sampleTime = data.sampleTime
    green = data.green
    green2 = data.green2
    ir = data.ir
    red = data.red
    accelerationX = data.accelerationXInt
    accelerationY = data.accelerationYInt
    accelerationZ = data.accelerationZInt
    operationMode = data.operationMode
    hr = data.hr
    hrConfidence = data.hrConfidence
    rr = data.rrInt
    rrConfidence = data.rrConfidence
    activity = data.activity
    r = data.rInt
    wspo2Confidence = data.wspo2Confidence
    spo2 = data.spo2Int
    wspo2PercentageComplete = data.wspo2PercentageComplete
    wspo2LowSnr = data.wspo2LowSnr
    wspo2Motion = data.wspo2Motion
    wspo2LowPi = data.wspo2LowPi
    wspo2UnreliableR = data.wspo2UnreliableR
    wspo2State = data.wspo2State
    scdState = data.scdState
    walkSteps = data.walkSteps
    runSteps = data.runSteps
    kCal = data.kCalInt
    totalActEnergy = data.totalActEnergyInt
    ibiOffset = data.ibiOffset
    timestamp = data.currentTimeMillis
}

fun AlgorithmInput.convertToHspData(): HspStreamData {
    return HspStreamData(
        sampleCount = sampleCount,
        sampleTime = sampleTime,
        green = green,
        green2 = green2,
        ir = ir,
        red = red,
        accelerationX = accelerationX / 1000f,
        accelerationY = accelerationY / 1000f,
        accelerationZ = accelerationZ / 1000f,
        operationMode = operationMode,
        hr = hr,
        hrConfidence = hrConfidence,
        rr = (rr / 10f),
        rrConfidence = rrConfidence,
        activity = activity,
        r = r / 1000f,
        wspo2Confidence = wspo2Confidence,
        spo2 = spo2 / 10f,
        wspo2PercentageComplete = wspo2PercentageComplete,
        wspo2LowSnr = wspo2LowSnr,
        wspo2Motion = wspo2Motion,
        wspo2LowPi = wspo2LowPi,
        wspo2UnreliableR = wspo2UnreliableR,
        wspo2State = wspo2State,
        scdState = scdState,
        walkSteps = walkSteps,
        runSteps = runSteps,
        kCal = kCal / 10f,
        totalActEnergy = totalActEnergy / 10f,
        ibiOffset = ibiOffset,
        currentTimeMillis = timestamp
    )
}