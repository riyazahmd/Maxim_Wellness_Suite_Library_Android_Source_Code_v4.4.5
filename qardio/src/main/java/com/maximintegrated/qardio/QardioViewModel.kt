package com.maximintegrated.qardio

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import no.nordicsemi.android.ble.common.profile.bp.BloodPressureTypes
import timber.log.Timber
import java.util.*

class QardioViewModel(application: Application) : AndroidViewModel(application),
    QardioManagerCallbacks {

    private val qardioManager = QardioManager(application)

    var bluetoothDevice: BluetoothDevice? = null

    val connectionState: LiveData<Pair<BluetoothDevice?, Int>> =
        qardioManager.state.map { Pair(qardioManager.bluetoothDevice, qardioManager.connectionState) }

    private val batteryLevelMutable = MutableLiveData<Int>()
    val batteryLevel: LiveData<Int> get() = batteryLevelMutable

    private val bloodPressureMeasurementMutable =
        MutableLiveData<BloodPressureMeasurement>()
    val bloodPressureMeasurement: LiveData<BloodPressureMeasurement>
        get() = bloodPressureMeasurementMutable

    val isDeviceSupported: LiveData<Boolean> = qardioManager.state.map { connectionState -> connectionState.isReady }

    private val _isMeasuring = MutableLiveData<Boolean>(false)
    val isMeasuring: LiveData<Boolean>
        get() = _isMeasuring

    val isConnected: Boolean
        get() = connectionState.value?.second == BluetoothAdapter.STATE_CONNECTED


    init {
        qardioManager.qardioManagerCallbacks = this
    }

    override fun onCleared() {
        if (qardioManager.isConnected) {
            disconnect()
        }
    }

    fun connect(device: BluetoothDevice) {
        if (bluetoothDevice == null) {
            bluetoothDevice = device
            qardioManager
                .connect(device)
                .enqueue()
        }
    }

    fun disconnect() {
        bluetoothDevice = null
        qardioManager
            .disconnect()
            .enqueue()
    }

    /**
     * Reconnect to previously connected device
     */
    fun reconnect() {
        qardioManager
            .disconnect()
            .enqueue()

        bluetoothDevice?.let {
            qardioManager
                .connect(it)
                .retry(3, 100)
                .useAutoConnect(false)
                .enqueue()
        }
    }

    fun startMeasurement() {
        _isMeasuring.value = true
        qardioManager.startBloodPressureMeasurement()
    }

    fun stopMeasurement() {
        _isMeasuring.value = false
        qardioManager.stopBloodPressureMeasurement()
    }

    fun readBatteryLevel() {
        qardioManager.readBatteryLevelCharacteristic()
    }

    fun enableBatteryLevelNotifications() {
        qardioManager.enableBatteryLevelCharacteristicNotifications()
    }

    fun disableBatteryLevelNotifications() {
        qardioManager.disableBatteryLevelCharacteristicNotifications()
    }

    fun resetBloodPressureMeasurement(){
        bloodPressureMeasurementMutable.value = BloodPressureMeasurement(0f,0f,0f,0,0f,0,null,null,0)
    }

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
        val bpmStatus = status?.let {
            BloodPressureMeasurement.Status(
                it.bodyMovementDetected,
                it.cuffTooLose,
                it.irregularPulseDetected,
                it.pulseRateInRange,
                it.pulseRateExceedsUpperLimit,
                it.pulseRateIsLessThenLowerLimit,
                it.improperMeasurementPosition
            )
        }

        val newMeasurement = BloodPressureMeasurement(
            systolic,
            diastolic,
            meanArterialPressure,
            unit,
            pulseRate,
            userID,
            bpmStatus,
            calendar
        )
        // do not fire duplicate values
        if (bloodPressureMeasurementMutable.value != newMeasurement) {
            bloodPressureMeasurementMutable.value = newMeasurement
        }
    }

    override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
        batteryLevelMutable.value = batteryLevel
    }
}