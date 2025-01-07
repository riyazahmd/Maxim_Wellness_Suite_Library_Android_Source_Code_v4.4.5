package com.maximintegrated.nonin

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.common.profile.battery.BatteryLevelCallback
import no.nordicsemi.android.ble.data.Data

interface IPlxMeasurementCallback {
    fun onPlxMeasurementReceived(device: BluetoothDevice, plxMeasurement: PlxMeasurement)
}

interface INoninManagerCallbacks : BatteryLevelCallback,
    IPlxMeasurementCallback

abstract class PlxMeasurementCallback : DataReceivedCallback, IPlxMeasurementCallback {
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        data.value?.let {
            onPlxMeasurementReceived(device, PlxMeasurement.fromPacket(it))
        }
    }
}
