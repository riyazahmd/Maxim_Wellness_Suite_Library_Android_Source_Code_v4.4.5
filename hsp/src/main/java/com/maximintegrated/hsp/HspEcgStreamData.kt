package com.maximintegrated.hsp

import java.sql.Timestamp

data class HspEcgStreamData(
    val sampleCount: Int,
    val ecg: Int,
    val eTag: Int,
    val pTag: Int,
    val rToR: Int,
    val currentRToRBpm: Int,
    val ecgMv: Float = ecg * ADC_TO_MV / ECG_GAIN,
    var filteredEcg: Float = 0f,
    var averagedRToRBpm: Float = 0f,
    var counterToReport: Int = 0
) {
    companion object {

        val CSV_HEADER_ARRAY = arrayOf(
            "Time", "Sample Count", "Filtered ECG (mV)", "Raw ECG (mV)",
            "ETAG[2:0]", "PTAG[2:0]", "Averaged R-to-R (bpm)", "Current R-to-R (bpm)", "CounterToReport"
        )

        private const val ECG_DATA_LENGTH_WITHOUT_SIGN_BIT = 17
        private const val VOLT_TO_MV = 1000
        private const val ADC_TO_MV = VOLT_TO_MV * 1f / (1 shl ECG_DATA_LENGTH_WITHOUT_SIGN_BIT)

        var ECG_GAIN = EcgRegisterMap.getDefaultEcgGain()

        fun fromPacket(packet: ByteArray): Array<HspEcgStreamData> {
            return with(BitStreamReader(packet, 8)) {
                val sampleCount = nextInt(8)
                val rToR = nextInt(14)
                val rToRBpm = nextInt(8)
                Array(4) {index ->
                    HspEcgStreamData(
                        sampleCount = sampleCount + index,
                        pTag = nextInt(3),
                        eTag = nextInt(3),
                        ecg = nextSignedInt(ECG_DATA_LENGTH_WITHOUT_SIGN_BIT + 1),
                        rToR = if(index == 0) rToR else 0,
                        currentRToRBpm = if(index == 0) rToRBpm else 0
                    )
                }
            }
        }
    }

    val timestamp = Timestamp(System.currentTimeMillis())

    fun toCsvModel(): String {
        return arrayOf(
            timestamp, sampleCount, filteredEcg, ecgMv, eTag, pTag,
            averagedRToRBpm, currentRToRBpm, counterToReport
        ).joinToString(separator = ",")
    }
}