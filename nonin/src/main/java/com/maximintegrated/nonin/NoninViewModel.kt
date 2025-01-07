package com.maximintegrated.nonin

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

class NoninViewModel(application: Application) : AndroidViewModel(application),
    INoninManagerCallbacks {

    private val noninManager = NoninManager(application)

    var bluetoothDevice: BluetoothDevice? = null
        private set

    val connectionState: LiveData<Pair<BluetoothDevice?, Int>> =
        noninManager.state.map { Pair(noninManager.bluetoothDevice, noninManager.connectionState) }

    private val batteryLevelMutable = MutableLiveData<Int>()
    val batteryLevel: LiveData<Int> get() = batteryLevelMutable

    private val plxMeasurementMutable = MutableLiveData<PlxMeasurement>()
    val plxMeasurement: LiveData<PlxMeasurement> get() = plxMeasurementMutable

    val isDeviceSupported: LiveData<Boolean> = noninManager.state.map { connectionState -> connectionState.isReady }

    init {
        noninManager.noninManagerCallbacks = this
    }

    override fun onCleared() {
        if (noninManager.isConnected) {
            disconnect()
        }
    }

    fun connect(device: BluetoothDevice) {
        if (bluetoothDevice == null) {
            bluetoothDevice = device
            noninManager
                .connect(device)
                .enqueue()
        }
    }

    fun disconnect() {
        bluetoothDevice = null
        noninManager
            .disconnect()
            .enqueue()
    }

    /**
     * Reconnect to previously connected device
     */
    fun reconnect() {
        noninManager
            .disconnect()
            .enqueue()

        bluetoothDevice?.let {
            noninManager
                .connect(it)
                .retry(3, 100)
                .useAutoConnect(false)
                .enqueue()
        }
    }

    override fun onPlxMeasurementReceived(device: BluetoothDevice, plxMeasurement: PlxMeasurement) {
        plxMeasurementMutable.value = plxMeasurement
    }

    override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
        batteryLevelMutable.value = batteryLevel
    }
}