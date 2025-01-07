package com.maximintegrated.hsp

import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import timber.log.Timber

class HspResponseDataMerger : DataMerger {
    companion object {
        const val STREAM_START_BYTE = 0xAA.toByte()

        const val COMMAND_RESPONSE_PADDING_BYTE: Byte = 0x00
        const val COMMAND_RESPONSE_END_BYTE = '\n'.toByte()
    }

    override fun merge(output: DataStream, lastPacket: ByteArray?, index: Int): Boolean {
        lastPacket?.let { packet ->
            output.write(packet)

            return packet.isStreamDataPacket() || packet.isEndOfCommandResponse()
        }

        return true
    }

    private fun ByteArray.isStreamDataPacket() = this[0] == STREAM_START_BYTE

    private fun ByteArray.isEndOfCommandResponse(): Boolean {
        val paddingStartIndex = indexOfFirst { it == COMMAND_RESPONSE_PADDING_BYTE }

        return when {
            paddingStartIndex == 0 -> true
            paddingStartIndex > 0 -> this[paddingStartIndex - 1] == COMMAND_RESPONSE_END_BYTE
            else -> last() == COMMAND_RESPONSE_END_BYTE
        }
    }
}