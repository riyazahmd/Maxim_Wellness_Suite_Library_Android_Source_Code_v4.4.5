package com.maximintegrated.maximsensorsapp

import com.chibatching.kotpref.KotprefModel

object DeviceSettings : KotprefModel() {
    var scdEnabled: Boolean by booleanPref(default = true)
    var logUserInfoEnabled: Boolean by booleanPref(default = false)
    var mfioEnabled: Boolean by booleanPref(default = false)
    var scdsmEnabled: Boolean by booleanPref(default = false)
    var lowPowerEnabled: Boolean by booleanPref(default = false)
    var selectedUserId: String by stringPref("")
}