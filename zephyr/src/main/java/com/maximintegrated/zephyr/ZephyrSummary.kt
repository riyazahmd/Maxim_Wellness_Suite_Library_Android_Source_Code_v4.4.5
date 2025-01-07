package com.maximintegrated.zephyr

data class ZephyrSummary(
    val timestamp: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val deviceWornDetectionLevel: Int, //0: Full Conf.   3: No Conf.
    val buttonPressDetection: Int, //0: No Press   1: Press
    val notFittedToGarment: Int, //0: Fitted  1: Not Fitted
    val hrUnreliable: Int, // 0: Reliable  1: Unreliable
    val rrUnreliable: Int, // 0: Reliable  1: Unreliable
    val estimatedCoreTemperatureUnreliable: Int, // 0: Reliable  1: Unreliable
    val hrvUnreliable: Int, // 0: Reliable  1: Unreliable
    private val reserved: Int,
    val trainingZone: Int, // 0, 6: Reserved  1-5: Training Zone  7: Invalid
    val externalSensorConnected: Int, //0: No ext. sensor connected  1: Ext. Sensor connected
    var hr: Int, //0-240   255: Invalid
    var rr: Float, //0-70   6553.5: Invalid
    var deviceTemp: Float, //-40-80   6553.5: Invalid
    var posture: Int, //-180-180   -32768: Invalid
    var activity: Float, //0-16   655.35: Invalid
    var hrv: Int, //0-65534   65535: Invalid
    var batteryLevel: Int, //0-100   255: Invalid
    var hrConf: Int, //0-100   255: Invalid
    var rrConf: Int, //0-100   255: Invalid
    var heatStressLevel: Float, //0-10   255: Invalid
    var physStrainIndex: Float, //0-10   255: Invalid
    var coreTemperature: Float //35-45   -128: Invalid (40 - 12.8)
) {

    companion object {

        const val INVALID_VALUE = Short.MAX_VALUE.toInt()

        fun fromPacket(packet: ByteArray): ZephyrSummary {
            val summary = with(BitStreamReader(packet)) {
                ZephyrSummary(
                    version = nextInt(8),
                    deviceWornDetectionLevel = nextInt(2),
                    buttonPressDetection = nextInt(1),
                    notFittedToGarment = nextInt(1),
                    hrUnreliable = nextInt(1),
                    rrUnreliable = nextInt(1),
                    estimatedCoreTemperatureUnreliable = nextInt(1),
                    hrvUnreliable = nextInt(1),
                    reserved = nextInt(4),
                    trainingZone = nextInt(3),
                    externalSensorConnected = nextInt(1),
                    hr = nextInt(8),
                    rr = nextFloat(16, 10),
                    deviceTemp = nextSignedFloat(16, 10),
                    posture = nextSignedInt(16),
                    activity = nextFloat(16, 100),
                    hrv = nextInt(16),
                    batteryLevel = nextInt(8),
                    hrConf = nextInt(8),
                    rrConf = nextInt(8),
                    heatStressLevel = nextFloat(8, 10),
                    physStrainIndex = nextFloat(8, 10),
                    coreTemperature = 40 + nextSignedFloat(8, 10)
                )
            }
            summary.fixValues()
            return summary
        }

        val CSV_HEADER = arrayOf(
            "timestamp",
            "version",
            "deviceWornDetectionLevel",
            "buttonPressDetection",
            "notFittedToGarment",
            "hrUnreliable",
            "rrUnreliable",
            "estimatedCoreTemperatureUnreliable",
            "hrvUnreliable",
            "reserved",
            "trainingZone",
            "externalSensorConnected",
            "hr",
            "rr",
            "deviceTemp",
            "posture",
            "activity",
            "hrv",
            "batteryLevel",
            "hrConf",
            "rrConf",
            "heatStressLevel",
            "physStrainIndex",
            "coreTemperature"
        )
    }

    private fun fixValues() {
        if (hr > 240) hr = INVALID_VALUE
        if (rr > 70f) rr = INVALID_VALUE.toFloat()
        if (deviceTemp < -40f || deviceTemp > 80f) deviceTemp = INVALID_VALUE.toFloat()
        if (posture < -180 || posture > 180) posture = INVALID_VALUE
        if (activity > 16) activity = INVALID_VALUE.toFloat()
        if (batteryLevel > 100) batteryLevel = INVALID_VALUE
        if (hrConf > 100) hrConf = INVALID_VALUE
        if (rrConf > 100) rrConf = INVALID_VALUE
        if (heatStressLevel > 10) heatStressLevel = INVALID_VALUE.toFloat()
        if (physStrainIndex > 10) physStrainIndex = INVALID_VALUE.toFloat()
        if (coreTemperature < 30 || coreTemperature > 50) coreTemperature = INVALID_VALUE.toFloat()
    }

    fun toCsvModel(): String {
        return arrayOf(
            timestamp,
            version,
            deviceWornDetectionLevel,
            buttonPressDetection,
            notFittedToGarment,
            hrUnreliable,
            rrUnreliable,
            estimatedCoreTemperatureUnreliable,
            hrvUnreliable,
            reserved,
            trainingZone,
            externalSensorConnected,
            hr,
            rr,
            deviceTemp,
            posture,
            activity,
            hrv,
            batteryLevel,
            hrConf,
            rrConf,
            heatStressLevel,
            physStrainIndex,
            coreTemperature
        ).joinToString(separator = ",")
    }
}