package com.maximintegrated.polar

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback
import no.nordicsemi.android.ble.common.callback.hr.HeartRateMeasurementDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import timber.log.Timber

class PolarManager(context: Context) : ObservableBleManager(context) {
    companion object {
        private val UUID_BATTERY_SERVICE = uuidFromShortCode16("180F")
        private val UUID_BATTERY_LEVEL_CHARACTERISTIC = uuidFromShortCode16("2A19")

        private val UUID_HEART_RATE_SERVICE = uuidFromShortCode16("180D")
        private val UUID_HEART_RATE_MEASUREMENT_CHARACTERISTIC = uuidFromShortCode16("2A37")
    }

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var heartRateMeasurementCharacteristic: BluetoothGattCharacteristic? = null

    var polarManagerCallbacks: PolarManagerCallbacks? = null

    private val batteryLevelDataCallback = object : BatteryLevelDataCallback() {
        override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
            polarManagerCallbacks?.onBatteryLevelChanged(device, batteryLevel)
        }

        override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
            Timber.e("Invalid battery level data received: %s", data)
        }
    }

    private val heartRateMeasurementDataCallback = object : HeartRateMeasurementDataCallback() {
        override fun onHeartRateMeasurementReceived(
            device: BluetoothDevice,
            heartRate: Int,
            contactDetected: Boolean?,
            energyExpanded: Int?,
            rrIntervals: MutableList<Int>?
        ) {
            polarManagerCallbacks?.onHeartRateMeasurementReceived(
                device,
                heartRate,
                contactDetected,
                energyExpanded,
                rrIntervals
            )
        }

        override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
            Timber.e("Invalid heart rate measurement data received: %s", data)
        }
    }

    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
            batteryLevelCharacteristic = null
            heartRateMeasurementCharacteristic = null
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val batteryService = gatt.getService(UUID_BATTERY_SERVICE)
            batteryLevelCharacteristic =
                batteryService?.getCharacteristic(UUID_BATTERY_LEVEL_CHARACTERISTIC)

            val heartRateService = gatt.getService(UUID_HEART_RATE_SERVICE)
            heartRateMeasurementCharacteristic = heartRateService?.getCharacteristic(
                UUID_HEART_RATE_MEASUREMENT_CHARACTERISTIC
            )

            return heartRateMeasurementCharacteristic.hasNotifyProperty
        }

        override fun initialize() {
            super.initialize()

            enableHeartRateMeasurementCharacteristicNotifications()
        }
    }

    fun readBatteryLevelCharacteristic() {
        if (isConnected) {
            readCharacteristic(batteryLevelCharacteristic)
                .with(batteryLevelDataCallback)
                .fail { device, status ->
                    Timber.e(
                        "Failed to read battery level characteristic (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }

    fun enableBatteryLevelCharacteristicNotifications() {
        if (isConnected) {
            setNotificationCallback(batteryLevelCharacteristic)
                .with(batteryLevelDataCallback)
            enableNotifications(batteryLevelCharacteristic)
                .done { device ->
                    Timber.i(
                        "Enabled battery level notifications (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Failed to enable battery level notifications (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }

    fun disableBatteryLevelCharacteristicNotifications() {
        if (isConnected) {
            disableNotifications(batteryLevelCharacteristic)
                .done { device ->
                    Timber.i(
                        "Disabled battery level notifications (Device: %s)",
                        device
                    )
                }
                .enqueue()
        }
    }

    fun enableHeartRateMeasurementCharacteristicNotifications() {
        if (isConnected) {
            setNotificationCallback(heartRateMeasurementCharacteristic)
                .with(heartRateMeasurementDataCallback)
            enableNotifications(heartRateMeasurementCharacteristic)
                .done { device ->
                    Timber.i(
                        "Enabled heart rate measurement notifications (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Failed to enable heart rate measurement notifications (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }

    fun disableHeartRateMeasurementCharacteristicNotifications() {
        if (isConnected) {
            disableIndications(heartRateMeasurementCharacteristic)
                .done { device ->
                    Timber.i(
                        "Disabled heart rate measurement notifications (Device: %s)",
                        device
                    )
                }
                .enqueue()
        }
    }
}