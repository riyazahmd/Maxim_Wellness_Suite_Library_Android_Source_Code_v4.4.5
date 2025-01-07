package com.maximintegrated.hsp

object EcgRegisterMap {
    val Defaults = sortedMapOf(
        0x15 to 0x805000,
        0x10 to 0x80000,
        0x12 to 0x4000,
        0x1D to 0x35A300,
        0x14 to 0x0
    )

    val ecgGainValues = intArrayOf(20, 40, 80, 160)

    val ecgGain = RegisterField(0x15, 2, 16)

    fun getDefaultEcgGain(): Int {
        val registerValue = Defaults[ecgGain.address]
        val valueIndex = ecgGain.extractValue(registerValue ?: 0)

        return ecgGainValues[valueIndex]
    }
}