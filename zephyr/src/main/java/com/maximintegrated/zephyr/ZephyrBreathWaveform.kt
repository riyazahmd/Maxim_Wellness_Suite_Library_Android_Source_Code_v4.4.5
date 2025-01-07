package com.maximintegrated.zephyr

private const val WAVEFORM_SAMPLES_LENGTH = 18

class ZephyrBreathWaveform(
    val sequenceNumber: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val msOfDay: Int,
    val data: List<Int>
) {
    companion object {
        fun fromPacket(payload: ByteArray): ZephyrBreathWaveform {
            return with(BitStreamReader(payload)) {
                val sequence = nextInt(8)
                val year = nextInt(16)
                val month = nextInt(8)
                val day = nextInt(8)
                val msOfDay = nextInt(32)
                val list = ArrayList<Int>()
                for (i in 0 until WAVEFORM_SAMPLES_LENGTH) {
                    list.add(nextInt(10))
                }
                ZephyrBreathWaveform(
                    sequenceNumber = sequence,
                    year = year,
                    month = month,
                    day = day,
                    msOfDay = msOfDay,
                    data = list
                )
            }
        }

        val CSV_HEADER = arrayOf(
            "sequenceNumber",
            "year",
            "month",
            "day",
            "msOfDay",
            "sample1",
            "sample2",
            "sample3",
            "sample4",
            "sample5",
            "sample6",
            "sample7",
            "sample8",
            "sample9",
            "sample10",
            "sample11",
            "sample12",
            "sample13",
            "sample14",
            "sample15",
            "sample16",
            "sample17",
            "sample18"
        )
    }

    fun toCsvModel(): String {
        return arrayOf(
            sequenceNumber,
            year,
            month,
            day,
            msOfDay,
            data.joinToString(separator = ",")
        ).joinToString(separator = ",")
    }

    override fun toString(): String {
        return "seq= $sequenceNumber year= $year month= $month day= $day msOfDay= $msOfDay data= " +
                data.joinToString(", ")
    }
}