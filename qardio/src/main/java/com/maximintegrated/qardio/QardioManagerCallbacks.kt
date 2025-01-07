package com.maximintegrated.qardio

import no.nordicsemi.android.ble.common.profile.battery.BatteryLevelCallback
import no.nordicsemi.android.ble.common.profile.bp.BloodPressureMeasurementCallback

interface QardioManagerCallbacks : BloodPressureMeasurementCallback,
    BatteryLevelCallback