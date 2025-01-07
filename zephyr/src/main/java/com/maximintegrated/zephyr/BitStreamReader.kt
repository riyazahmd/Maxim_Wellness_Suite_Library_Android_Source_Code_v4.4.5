package com.maximintegrated.zephyr

import java.util.BitSet

class BitStreamReader @JvmOverloads constructor(
    bytes: ByteArray,
    private var currentBitIndex: Int = 0
) {

    private val bitSet: BitSet = BitSet.valueOf(bytes)

    fun nextInt(sizeInBits: Int): Int {
        val value = getInt(currentBitIndex, sizeInBits)
        currentBitIndex += sizeInBits
        return value
    }

    fun nextSignedInt(sizeInBits: Int): Int {
        val value = getSignedInt(currentBitIndex, sizeInBits)
        currentBitIndex += sizeInBits
        return value
    }

    fun nextFloat(sizeInBits: Int, divisor: Int): Float {
        val value = getInt(currentBitIndex, sizeInBits).toFloat()
        currentBitIndex += sizeInBits
        return value / divisor.toFloat()
    }

    fun nextSignedFloat(sizeInBits: Int, divisor: Int): Float {
        val value = getSignedInt(currentBitIndex, sizeInBits).toFloat()
        currentBitIndex += sizeInBits
        return value / divisor.toFloat()
    }

    private fun getInt(startIndex: Int, sizeInBits: Int): Int {
        if(startIndex + sizeInBits > bitSet.size()) return 0
        val longArray = bitSet.get(startIndex, startIndex + sizeInBits).toLongArray()
        var value = 0
        if (longArray.isNotEmpty()) {
            value = longArray[0].toInt()
        }

        return value
    }

    private fun getSignedInt(startIndex: Int, sizeInBits: Int): Int {
        val sizeWithoutSignBit = sizeInBits - 1
        if(startIndex + sizeWithoutSignBit >= bitSet.size()) return 0
        val signBit = bitSet.get(startIndex + sizeWithoutSignBit)

        return if (signBit) {
            for (i in startIndex until startIndex + sizeWithoutSignBit) {
                if (bitSet.get(i)) {
                    bitSet.clear(i)
                } else {
                    bitSet.set(i)
                }
            }
            -getInt(startIndex, sizeWithoutSignBit) - 1
        } else {
            getInt(startIndex, sizeWithoutSignBit)
        }
    }
}