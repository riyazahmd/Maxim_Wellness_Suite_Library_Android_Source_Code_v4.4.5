package com.maximintegrated.zephyr

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ZephyrResponseMessage(
    val stx: Int,
    val messageId: Int,
    val dlc: Int,
    val payload: ByteArray,
    val crc: Int,
    val ackOrEtx: Int
) {
    companion object {
        fun fromPacket(packet: ByteArray): ZephyrResponseMessage? {
            val buffer = ByteBuffer.wrap(packet)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            return with(buffer) {
                try {
                    val stx = get().toInt()
                    val id = get().toInt()
                    val dlc = get().toInt()
                    val payload = ByteArray(dlc)
                    if (dlc > 0) {
                        get(payload)
                    }
                    val crc = get().toInt()
                    val ack = get().toInt()
                    ZephyrResponseMessage(
                        stx = stx,
                        messageId = id,
                        dlc = dlc,
                        payload = payload,
                        crc = crc,
                        ackOrEtx = ack
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override fun toString(): String {
        return "stx= $stx messageId= $messageId dlc= $dlc payload= " +
                "${payload.joinToString(", ")} crc= $crc ackOrEtx= $ackOrEtx"
    }
}
