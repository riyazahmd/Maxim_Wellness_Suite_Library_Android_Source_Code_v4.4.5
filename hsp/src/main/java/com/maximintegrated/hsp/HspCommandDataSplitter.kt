package com.maximintegrated.hsp

import no.nordicsemi.android.ble.data.DataSplitter


class HspCommandDataSplitter : DataSplitter {
    companion object {
        const val MAX_COMMAND_LENGTH = 16
    }

    override fun chunk(message: ByteArray, index: Int, maxLength: Int): ByteArray? {
        val offset = index * MAX_COMMAND_LENGTH
        val length = minOf(MAX_COMMAND_LENGTH, message.size - offset)

        if (length <= 0)
            return null

        val data = ByteArray(MAX_COMMAND_LENGTH)
        System.arraycopy(message, offset, data, 0, length)
        return data
    }
}