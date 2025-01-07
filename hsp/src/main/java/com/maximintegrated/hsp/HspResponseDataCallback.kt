package com.maximintegrated.hsp

import android.bluetooth.BluetoothDevice
import com.maximintegrated.hsp.HspResponseDataMerger.Companion.COMMAND_RESPONSE_END_BYTE
import com.maximintegrated.hsp.HspResponseDataMerger.Companion.COMMAND_RESPONSE_PADDING_BYTE
import com.maximintegrated.hsp.protocol.HspResponse
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data
import timber.log.Timber

interface HspResponseCallback {
    fun onCommandResponseReceived(device: BluetoothDevice, commandResponse: HspResponse<*>)
    fun onStreamDataReceived(device: BluetoothDevice, packet: ByteArray)
}

abstract class HspResponseDataCallback : DataReceivedCallback, HspResponseCallback {

    companion object{
        private const val MAX_RETURN_LEN = 2048
    }

    private val responsePacket = ByteArray(MAX_RETURN_LEN)
    private var responseIndex = 0

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val packet = data.value ?: return
        Timber.d("RECEIVED PACKET: ${packet.toResponseString()}")
        Timber.d("RECEIVED PACKET in BYTE ${packet.toHexString()}")
        if (packet[0] == HspResponseDataMerger.STREAM_START_BYTE) {
            onStreamDataReceived(device, packet)
        } else {
            for(b in packet){
                responsePacket[responseIndex++] = b
                if(b == COMMAND_RESPONSE_END_BYTE){
                    val response = responsePacket.sliceArray(0 until responseIndex).toResponseString()
                    if(response != ""){
                        onCommandResponseReceived(device, HspResponse.fromText(response))
                    }
                    responseIndex = 0
                }
            }
        }
    }

    private fun Data.toText() = value?.let {
        String(it).trim(
            COMMAND_RESPONSE_PADDING_BYTE.toChar(),
            COMMAND_RESPONSE_END_BYTE.toChar(),
            '\r',
            ' '
        )
    } ?: ""

    private fun ByteArray.toResponseString() = String(this).trim(
        COMMAND_RESPONSE_PADDING_BYTE.toChar(),
        COMMAND_RESPONSE_END_BYTE.toChar(),
        '\r',
        ' '
    )

    private fun Data.toCommandResponse() = HspResponse.fromText(toText())
}
