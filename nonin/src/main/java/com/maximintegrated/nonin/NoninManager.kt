package com.maximintegrated.nonin

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import timber.log.Timber

class NoninManager(context: Context) : ObservableBleManager(context) {
    companion object {
        private val UUID_BATTERY_SERVICE = uuidFromShortCode16("180F")
        private val UUID_BATTERY_LEVEL_CHARACTERISTIC = uuidFromShortCode16("2A19")

        private val UUID_PLX_SERVICE = uuidFromShortCode16("1822")
        private val UUID_PLX_CTS_MEASUREMENT_CHARACTERISTIC = uuidFromShortCode16("2A5F")
        private val UUID_PLX_FEATURES_CHARACTERISTIC = uuidFromShortCode16("2A60")
    }

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var plxContinuousMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var plxFeaturesCharacteristic: BluetoothGattCharacteristic? = null

    var noninManagerCallbacks: INoninManagerCallbacks? = null

    private val batteryLevelDataCallback = object : BatteryLevelDataCallback() {
        override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
            noninManagerCallbacks?.onBatteryLevelChanged(device, batteryLevel)
        }

        override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
            Timber.e("Invalid battery level data received: %s", data)
        }
    }

    private val plxMeasurementCallback = object : PlxMeasurementCallback() {
        override fun onPlxMeasurementReceived(
            device: BluetoothDevice,
            plxMeasurement: PlxMeasurement
        ) {
            noninManagerCallbacks?.onPlxMeasurementReceived(device, plxMeasurement)
        }
    }

    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
            batteryLevelCharacteristic = null
            plxContinuousMeasurementCharacteristic = null
            plxFeaturesCharacteristic = null
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val batteryService = gatt.getService(UUID_BATTERY_SERVICE)
            batteryLevelCharacteristic =
                batteryService?.getCharacteristic(UUID_BATTERY_LEVEL_CHARACTERISTIC)

            val plxService = gatt.getService(UUID_PLX_SERVICE)
            plxContinuousMeasurementCharacteristic = plxService?.getCharacteristic(
                UUID_PLX_CTS_MEASUREMENT_CHARACTERISTIC
            )
            plxFeaturesCharacteristic = plxService?.getCharacteristic(
                UUID_PLX_FEATURES_CHARACTERISTIC
            )
            return plxContinuousMeasurementCharacteristic.hasNotifyProperty
        }

        override fun initialize() {
            super.initialize()
            enableBatteryLevelCharacteristicNotifications()
            enablePlxContinuousMeasurementCharacteristicNotifications()
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

    fun enablePlxContinuousMeasurementCharacteristicNotifications() {
        if (isConnected) {
            setNotificationCallback(plxContinuousMeasurementCharacteristic)
                .with(plxMeasurementCallback)
            enableNotifications(plxContinuousMeasurementCharacteristic)
                .done { device ->
                    Timber.i(
                        "Enabled plx continuous measurement notifications (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Failed to enable plx continuous measurement notifications (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }
}
