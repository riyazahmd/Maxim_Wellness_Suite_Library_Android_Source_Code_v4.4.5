package com.maximintegrated.polar

import no.nordicsemi.android.ble.common.profile.battery.BatteryLevelCallback
import no.nordicsemi.android.ble.common.profile.hr.HeartRateMeasurementCallback

interface PolarManagerCallbacks : HeartRateMeasurementCallback,
    BatteryLevelCallback