package com.maximintegrated.zephyr

import java.nio.ByteBuffer
import java.nio.ByteOrder

const val ZEPHYR_BREATH_WAVEFORM_TRANSMIT_ID = 0x15
const val ZEPHYR_BREATH_WAVEFORM_DATA = 0x21
const val STX = 0x02
const val ETX = 0x03
const val ZEPHYR_ACK = 0x06
const val ZEPHYR_NAK = 0x15
private const val CRC_POL = 0x8C

fun createRequestMessagePacket(messageId: Int, payload: ByteArray): ByteArray {
    val size = 1 + 1 + 1 + payload.size + 1 + 1
    val buffer = ByteBuffer.allocate(size)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(STX.toByte())
    buffer.put(messageId.toByte())
    buffer.put(payload.size.toByte())
    if (payload.isNotEmpty()) {
        buffer.put(payload)
    }
    buffer.put(calculateCrc(payload))
    buffer.put(ETX.toByte())
    return buffer.array()
}


private fun calculateCrc(packet: ByteArray): Byte {
    val crc8 = CRC8(CRC_POL)
    return crc8.calculate(packet)
}
