package com.maximintegrated.maximsensorsapp

import com.chibatching.kotpref.KotprefModel

object WhrmSettings : KotprefModel() {
    var sampledModeTimeInterval: Long by longPref(default = 300000)
    var minConfidenceLevel: Int by intPref(default = 0)
    var confidenceThresholdInSeconds: Int by intPref(default = 30)
}