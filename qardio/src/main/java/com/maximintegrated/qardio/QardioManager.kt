package com.maximintegrated.qardio

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback
import no.nordicsemi.android.ble.common.callback.bps.BloodPressureMeasurementDataCallback
import no.nordicsemi.android.ble.common.profile.bp.BloodPressureTypes
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import timber.log.Timber
import java.util.*

class QardioManager(context: Context) : ObservableBleManager(context) {

    companion object {
        private val UUID_BATTERY_SERVICE = uuidFromShortCode16("180F")
        private val UUID_BATTERY_LEVEL_CHARACTERISTIC = uuidFromShortCode16("2A19")

        private val UUID_BLOOD_PRESSURE_SERVICE = uuidFromShortCode16("1810")
        private val UUID_BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC = uuidFromShortCode16("2A35")
        private val UUID_BLOOD_PRESSURE_COMMAND_CHARACTERISTIC =
            UUID.fromString("583CB5B3-875D-40ED-9098-C39EB0C1983D")

        private val COMMAND_MEASUREMENT_START = byteArrayOf(0xF1.toByte(), 0x01)
        private val COMMAND_MEASUREMENT_STOP = byteArrayOf(0xF1.toByte(), 0x02)
    }

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var bloodPressureMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var bloodPressureCommandCharacteristic: BluetoothGattCharacteristic? = null

    var qardioManagerCallbacks: QardioManagerCallbacks? = null

    private val batterLevelDataCallback = object : BatteryLevelDataCallback() {
        override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
            qardioManagerCallbacks?.onBatteryLevelChanged(device, batteryLevel)
        }

        override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
            Timber.e("Qardio: Invalid battery level data received: %s", data)
        }
    }

    private val bloodPressureMeasurementDataCallback =
        object : BloodPressureMeasurementDataCallback() {
            override fun onBloodPressureMeasurementReceived(
                device: BluetoothDevice,
                systolic: Float,
                diastolic: Float,
                meanArterialPressure: Float,
                unit: Int,
                pulseRate: Float?,
                userID: Int?,
                status: BloodPressureTypes.BPMStatus?,
                calendar: Calendar?
            ) {
                qardioManagerCallbacks?.onBloodPressureMeasurementReceived(
                    device, systolic, diastolic,
                    meanArterialPressure, unit, pulseRate, userID, status, calendar
                )
            }

            override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
                Timber.e("Qardio: Invalid blood pressure measurement data received: %s", data)
            }
        }

    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
            batteryLevelCharacteristic = null
            bloodPressureMeasurementCharacteristic = null
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val batteryService = gatt.getService(UUID_BATTERY_SERVICE)
            batteryLevelCharacteristic =
                batteryService?.getCharacteristic(UUID_BATTERY_LEVEL_CHARACTERISTIC)

            val bloodPressureService = gatt.getService(UUID_BLOOD_PRESSURE_SERVICE)
            bloodPressureMeasurementCharacteristic = bloodPressureService?.getCharacteristic(
                UUID_BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC
            )
            bloodPressureCommandCharacteristic = bloodPressureService?.getCharacteristic(
                UUID_BLOOD_PRESSURE_COMMAND_CHARACTERISTIC
            )

            return bloodPressureMeasurementCharacteristic.hasIndicateProperty &&
                    bloodPressureCommandCharacteristic.hasWriteProperty
        }

        override fun initialize() {
            super.initialize()

            enableBloodPressureMeasurementCharacteristicIndications()
        }
    }

    fun readBatteryLevelCharacteristic() {
        if (isConnected) {
            readCharacteristic(batteryLevelCharacteristic)
                .with(batterLevelDataCallback)
                .fail { device, status ->
                    Timber.e(
                        "Qardio: Failed to read battery level characteristic (Device: %s, Status: %d)",
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
                .with(batterLevelDataCallback)
            enableNotifications(batteryLevelCharacteristic)
                .done { device ->
                    Timber.i(
                        "Qardio: Enabled battery level notifications (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Qardio: Failed to enable battery level notifications (Device: %s, Status: %d)",
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
                        "Qardio: Disabled battery level notifications (Device: %s)",
                        device
                    )
                }
                .enqueue()
        }
    }

    fun enableBloodPressureMeasurementCharacteristicIndications() {
        if (isConnected) {
            setIndicationCallback(bloodPressureMeasurementCharacteristic)
                .with(bloodPressureMeasurementDataCallback)
            enableIndications(bloodPressureMeasurementCharacteristic)
                .done { device ->
                    Timber.i(
                        "Qardio: Enabled blood pressure measurement indications (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Qardio: Failed to enable blood pressure measurement indications (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }

    fun disableBloodPressureMeasurementCharacteristicIndications() {
        if (isConnected) {
            disableIndications(bloodPressureMeasurementCharacteristic)
                .done { device ->
                    Timber.i(
                        "Qardio: Disabled blood pressure measurement indications (Device: %s)",
                        device
                    )
                }
                .enqueue()
        }
    }

    fun startBloodPressureMeasurement() {
        if (isConnected) {
            writeCharacteristic(bloodPressureCommandCharacteristic, COMMAND_MEASUREMENT_START)
                .done { device ->
                    Timber.i(
                        "Qardio: Written start measurement command to characteristic (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Qardio: Failed to write start measurement command to characteristic (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }

    fun stopBloodPressureMeasurement() {
        if (isConnected) {
            writeCharacteristic(bloodPressureCommandCharacteristic, COMMAND_MEASUREMENT_STOP)
                .done { device ->
                    Timber.i(
                        "Qardio: Written stop measurement command to characteristic (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Qardio: Failed to write stop measurement command to characteristic (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }
}