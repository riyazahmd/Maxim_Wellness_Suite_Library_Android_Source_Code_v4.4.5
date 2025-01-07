package com.maximintegrated.hsp

data class HspTempStreamData(
    val sampleCount: Int,
    val temperature: Float
) {

    companion object {
        val CSV_HEADER_ARRAY = arrayOf(
            "Time", "Sample Count", "Temperature (°C)"
        )

        fun fromPacket(packet: ByteArray): HspTempStreamData {
            return with(BitStreamReader(packet, 8)) {
                HspTempStreamData(
                    sampleCount = nextInt(8),
                    temperature = nextSignedFloat(16, 100)
                )
            }
        }
    }

    val timestamp = System.currentTimeMillis()

    fun toCsvModel(): String {
        return arrayOf(timestamp, sampleCount, temperature).joinToString(separator = ",")
    }
}