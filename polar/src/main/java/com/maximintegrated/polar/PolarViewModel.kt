package com.maximintegrated.polar

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

class PolarViewModel(application: Application) : AndroidViewModel(application),
    PolarManagerCallbacks {

    private val polarManager = PolarManager(application)

    var bluetoothDevice: BluetoothDevice? = null
        private set

    val connectionState: LiveData<Pair<BluetoothDevice?, Int>> =
        polarManager.state.map { Pair(polarManager.bluetoothDevice, polarManager.connectionState) }

    private val batteryLevelMutable = MutableLiveData<Int>()
    val batteryLevel: LiveData<Int> get() = batteryLevelMutable

    private val heartRateMeasurementMutable =
        MutableLiveData<HeartRateMeasurement>()
    val heartRateMeasurement: LiveData<HeartRateMeasurement>
        get() = heartRateMeasurementMutable

    val isDeviceSupported: LiveData<Boolean> = polarManager.state.map { connectionState -> connectionState.isReady }


    val isConnected: Boolean
        get() = connectionState.value?.second == BluetoothAdapter.STATE_CONNECTED


    init {
        polarManager.polarManagerCallbacks = this
    }

    override fun onCleared() {
        if (polarManager.isConnected) {
            disconnect()
        }
    }

    fun connect(device: BluetoothDevice) {
        if (bluetoothDevice == null) {
            bluetoothDevice = device
            polarManager
                .connect(device)
                .enqueue()
        }
    }

    fun disconnect() {
        bluetoothDevice = null
        polarManager
            .disconnect()
            .enqueue()
    }

    /**
     * Reconnect to previously connected device
     */
    fun reconnect() {
        polarManager
            .disconnect()
            .enqueue()

        bluetoothDevice?.let {
            polarManager
                .connect(it)
                .retry(3, 100)
                .useAutoConnect(false)
                .enqueue()
        }
    }

    fun readBatteryLevel() {
        polarManager.readBatteryLevelCharacteristic()
    }

    fun enableBatteryLevelNotifications() {
        polarManager.enableBatteryLevelCharacteristicNotifications()
    }

    fun disableBatteryLevelNotifications() {
        polarManager.disableBatteryLevelCharacteristicNotifications()
    }

    override fun onHeartRateMeasurementReceived(
        device: BluetoothDevice,
        heartRate: Int,
        contactDetected: Boolean?,
        energyExpanded: Int?,
        rrIntervals: MutableList<Int>?
    ) {
        heartRateMeasurementMutable.value =
            HeartRateMeasurement(heartRate, contactDetected, energyExpanded, rrIntervals)
    }

    override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
        batteryLevelMutable.value = batteryLevel
    }
}