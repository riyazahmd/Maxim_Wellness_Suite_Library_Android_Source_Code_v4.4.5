package com.maximintegrated.zephyr

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data

interface IZephyrSummaryCallback {
    fun onSummaryReceived(device: BluetoothDevice, summary: ZephyrSummary)
}

interface IZephyrTxCallback {
    fun onResponseReceived(device: BluetoothDevice, message: ZephyrResponseMessage)
}

interface IZephyrManagerCallbacks : IZephyrSummaryCallback, IZephyrTxCallback

abstract class ZephyrSummaryCallback : DataReceivedCallback, IZephyrSummaryCallback {

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        data.value?.let {
            onSummaryReceived(device, ZephyrSummary.fromPacket(it))
        }
    }
}

abstract class ZephyrTxCallback : DataReceivedCallback, IZephyrTxCallback {

    private var payload: ByteArray? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        data.value?.let {
            if (isStartByteValid(it) && isMessageByteValid(it)) {
                payload = null
            }
            if (isStartByteValid(it) && isEndByteValid(it)) {
                val response = ZephyrResponseMessage.fromPacket(it)
                if (response != null) {
                    onResponseReceived(device, response)
                }
            } else {
                val packet = appendToPayload(it)
                payload = packet
                if (isStartByteValid(packet) && isEndByteValid(packet)) {
                    val response = ZephyrResponseMessage.fromPacket(packet)
                    if (response != null) {
                        payload = null
                        onResponseReceived(device, response)
                    }
                }
            }

        }
    }

    private fun isStartByteValid(packet: ByteArray) = packet[0] == STX.toByte()

    private fun isMessageByteValid(packet: ByteArray): Boolean {
        if (packet.size < 2) return false
        return when (packet[1].toInt()) {
            ZEPHYR_BREATH_WAVEFORM_TRANSMIT_ID, ZEPHYR_BREATH_WAVEFORM_DATA -> true
            else -> false
        }
    }

    private fun isEndByteValid(packet: ByteArray): Boolean {
        return when (packet.last().toInt()) {
            ZEPHYR_ACK, ZEPHYR_NAK, ETX -> true
            else -> false
        }
    }

    private fun appendToPayload(packet: ByteArray): ByteArray {
        return if (payload == null) {
            packet
        } else {
            payload!!.plus(packet)
        }
    }
}