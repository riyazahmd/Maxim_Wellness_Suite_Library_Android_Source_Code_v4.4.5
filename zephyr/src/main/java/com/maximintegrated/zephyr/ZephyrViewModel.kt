package com.maximintegrated.zephyr

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

class ZephyrViewModel(application: Application) : AndroidViewModel(application),
    IZephyrManagerCallbacks {

    private val zephyrManager = ZephyrManager(application)

    var bluetoothDevice: BluetoothDevice? = null
        private set

    val connectionState: LiveData<Pair<BluetoothDevice?, Int>> = zephyrManager.state.map {
        Pair(
            zephyrManager.bluetoothDevice,
            zephyrManager.connectionState
        )
    }

    private val zephyrSummaryMutable = MutableLiveData<ZephyrSummary>()
    val zephyrSummary: LiveData<ZephyrSummary> = zephyrSummaryMutable

    private val zephyrBreathWaveformMutable = MutableLiveData<ZephyrBreathWaveform>()
    val zephyrBreathWaveform: LiveData<ZephyrBreathWaveform> = zephyrBreathWaveformMutable

    val isDeviceSupported: LiveData<Boolean> = zephyrManager.state.map { connectionState -> connectionState.isReady }

    val isConnected: Boolean
        get() = connectionState.value?.second == BluetoothAdapter.STATE_CONNECTED


    init {
        zephyrManager.zephyrManagerCallbacks = this
    }

    override fun onCleared() {
        super.onCleared()
        if (zephyrManager.isConnected) {
            disconnect()
        }
    }

    fun connect(device: BluetoothDevice) {
        if (bluetoothDevice == null) {
            bluetoothDevice = device
            zephyrManager
                .connect(device)
                .enqueue()
        }
    }

    fun disconnect() {
        bluetoothDevice = null
        zephyrManager.stopBreathWaveform()
        zephyrManager
            .disconnect()
            .enqueue()
    }

    fun reconnect() {
        zephyrManager
            .disconnect()
            .enqueue()

        bluetoothDevice?.let {
            zephyrManager
                .connect(it)
                .retry(3, 100)
                .useAutoConnect(false)
                .enqueue()
        }
    }

    fun enableSummaryCharacteristicNotifications() {
        zephyrManager.enableSummaryCharacteristicNotifications()
    }

    fun enableTxCharacteristicNotifications() {
        zephyrManager.enableTxCharacteristicNotifications()
    }

    fun updateSummaryPeriod(periodInSec: Int) {
        zephyrManager.updateSummaryPeriod(periodInSec)
    }

    fun startBreathWaveform() {
        zephyrManager.startBreathWaveform()
    }

    fun stopBreathWaveform() {
        zephyrManager.stopBreathWaveform()
    }

    override fun onSummaryReceived(device: BluetoothDevice, summary: ZephyrSummary) {
        zephyrSummaryMutable.value = summary
    }

    override fun onResponseReceived(device: BluetoothDevice, message: ZephyrResponseMessage) {
        if (message.messageId == ZEPHYR_BREATH_WAVEFORM_TRANSMIT_ID) {
            if (message.ackOrEtx == ZEPHYR_NAK) {
                zephyrManager.startBreathWaveform()
            }
        }
        if (message.messageId == ZEPHYR_BREATH_WAVEFORM_DATA) {
            zephyrBreathWaveformMutable.value = ZephyrBreathWaveform.fromPacket(message.payload)
        }
    }
}