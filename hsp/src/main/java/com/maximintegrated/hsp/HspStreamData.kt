package com.maximintegrated.hsp

import java.text.SimpleDateFormat
import java.util.*

enum class PpgFormat {
    PPG_4_REPORT_1,
    PPG_4_REPORT_2,
    PPG_9
}

data class HspStreamData(
    var sampleCount: Int,
    val sampleTime: Int,
    val green: Int,
    val green2: Int,
    val ir: Int,
    val red: Int,
    val accelerationX: Float,
    val accelerationY: Float,
    val accelerationZ: Float,
    val operationMode: Int,
    val hr: Int,
    val hrConfidence: Int,
    var rr: Float,
    var rrConfidence: Int,
    val activity: Int,
    val r: Float,
    val wspo2Confidence: Int,
    val spo2: Float,
    val wspo2PercentageComplete: Int,
    val wspo2LowSnr: Int,
    val wspo2Motion: Int,
    val wspo2LowPi: Int,
    val wspo2UnreliableR: Int,
    var wspo2State: Int,
    val scdState: Int,
    val walkSteps: Int,
    val runSteps: Int,
    val kCal: Float,
    val totalActEnergy: Float,
    var ibiOffset: Int = 0,
    var currentTimeMillis: Long = System.currentTimeMillis()
) {

    var accelerationXInt = (accelerationX * 1000f).toInt()
    var accelerationYInt = (accelerationY * 1000f).toInt()
    var accelerationZInt = (accelerationZ * 1000f).toInt()
    var rrInt = (rr * 10f).toInt()
    var rInt = (r * 1000f).toInt()
    var spo2Int = (spo2 * 10f).toInt()
    var kCalInt = (kCal * 10f).toInt()
    var totalActEnergyInt = (totalActEnergy * 10f).toInt()

    var isLed1CurrentAdj = 0
    var adjLed1Current = 0
    var isLed2CurrentAdj = 0
    var adjLed2Current = 0
    var isLed3CurrentAdj = 0
    var adjLed3Current = 0
    var isTAdj = 0
    var adjT = 0
    var isFAdj = 0
    var adjF = 0
    var smpAve = 0
    var hrmAfeState = 0
    var isHighMotion = 0

    companion object {

        // get_format ppg 9 enc=bin cs=1 format={smpleCnt,8},{smpleTime,32},{grnCnt,20},{grn2Cnt,20},{irCnt,20},{redCnt,20},{accelX,14,3},
        //{accelY,14,3},{accelZ,14,3},{opMode,4},{hr,12},{hrconf,8},{rr,14,1},{rrconf,8},{activity,4},{r,12,3},{wspo2conf,8},{spo2,11,1},{wspo2percentcomplete,8},
        //{wspo2lowSNR,1},{wspo2motion,1},{wspo2lowpi,1},{wspo2unreliableR,1},{wspo2state,4},{scdstate,4},
        //{wSteps,32},{rSteps,32},{kCal,32, 1},{totalActEnergy,32, 1},{ibiOffset,8}


        // get_format ppg 4 enc=bin cs=1 format={smpleCnt,8},{smpleTime,32},{grnCnt,20},{grn2Cnt,20},{irCnt,20},{redCnt,20},{accelX,14,3},
        // {accelY,14,3},{accelZ,14,3},{opMode,4},{hr,12},{hrconf,8},{rr,14,1},{rrconf,8},{activity,4},{wSteps,32},{rSteps,32},{kCal,32,1},{totalActEnergy,32,1},
        // {isLed1CurrentAdj,1},{adjLed1Current,11},{isLed2CurrentAdj,1},{adjLed2Current,11},{isLed3CurrentAdj,1},{adjLed3Current,11},
        // {isTAdj,1},{adjT,2},{isFAdj,1},{adjF,3},{smpAve,3},{hrmAfeState,2},{isHighMotion,1},{r,12,3},{wspo2conf,8},{spo2,11,1},{wspo2percentcomplete,8},
        // {wspo2lowSNR,1},{wspo2motion,1},{wspo2lowpi,1},{wspo2unreliableR,1},{wspo2state,4},{scdstate,4},{ibiOffset,8}


        const val NUMBER_OF_BYTES_IN_PACKET_PPG_9 =
            52 // including 1 byte data stream start(aa), 1 byte crc

        const val NUMBER_OF_BYTES_IN_PACKET_PPG_4_REPORT_1 =
            36 // including 1 byte data stream start(aa), 1 byte crc

        const val NUMBER_OF_BYTES_IN_PACKET_PPG_4_REPORT_2 =
            58 // including 1 byte data stream start(aa), 1 byte crc

        const val NUMBER_OF_BYTES_IN_PACKET_PPG_10 =
            40 // including 1 byte data stream start(aa), 1 byte crc

        private val TIMESTAMP_FORMAT = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.US)
        val CSV_HEADER_HSP = arrayOf(
            "sample_count",
            "sample_time",
            "green",
            "green2",
            "ir",
            "red",
            "acceleration_x",
            "acceleration_y",
            "acceleration_z",
            "op_mode",
            "hr",
            "hr_confidence",
            "rr",
            "rr_confidence",
            "activity",
            "r",
            "spo2_confidence",
            "spo2",
            "spo2_percentage_complete",
            "spo2_low_snr",
            "spo2_motion",
            "spo2_low_pi",
            "spo2_unreliable_r",
            "spo2_state",
            "scd_state",
            "walking_steps",
            "running_steps",
            "calorie",
            "totalActEnergy",
            "ibiOffset",
            "timestamp",
            "timestamp_millis"
        )

        fun fromPacket(packet: ByteArray, format: PpgFormat = PpgFormat.PPG_9): HspStreamData {
            return when (format) {
                PpgFormat.PPG_9 -> parsePpg9FormattedData(packet)
                PpgFormat.PPG_4_REPORT_1 -> parsePpg4Report1FormattedData(packet)
                PpgFormat.PPG_4_REPORT_2 -> parsePpg4Report2FormattedData(packet)
            }
        }

        private fun parsePpg9FormattedData(packet: ByteArray): HspStreamData {
            return with(BitStreamReader(packet, 8)) {
                HspStreamData(
                    sampleCount = nextInt(8),
                    sampleTime = nextInt(32),
                    green = nextInt(20),
                    green2 = nextInt(20),
                    ir = nextInt(20),
                    red = nextInt(20),
                    accelerationX = nextSignedFloat(14, 1000),
                    accelerationY = nextSignedFloat(14, 1000),
                    accelerationZ = nextSignedFloat(14, 1000),
                    operationMode = nextInt(4),
                    hr = nextInt(12),
                    hrConfidence = nextInt(8),
                    rr = nextFloat(14, 10),
                    rrConfidence = nextInt(8),
                    activity = nextInt(4),
                    r = nextSignedFloat(12, 1000),
                    wspo2Confidence = nextInt(8),
                    spo2 = nextFloat(11, 10),
                    wspo2PercentageComplete = nextInt(8),
                    wspo2LowSnr = nextInt(1),
                    wspo2Motion = nextInt(1),
                    wspo2LowPi = nextInt(1),
                    wspo2UnreliableR = nextInt(1),
                    wspo2State = nextInt(4),
                    scdState = nextInt(4),
                    walkSteps = nextInt(32),
                    runSteps = nextInt(32),
                    kCal = nextSignedFloat(32, 10),
                    totalActEnergy = nextSignedFloat(32, 10),
                    ibiOffset = nextInt(8)
                )
            }
        }

        private fun parsePpg4Report1FormattedData(packet: ByteArray): HspStreamData {
            return with(BitStreamReader(packet, 8)) {
                HspStreamData(
                    sampleCount = nextInt(8),
                    sampleTime = nextInt(32),
                    green = nextInt(20),
                    green2 = nextInt(20),
                    ir = nextInt(20),
                    red = nextInt(20),
                    accelerationX = nextSignedFloat(14, 1000),
                    accelerationY = nextSignedFloat(14, 1000),
                    accelerationZ = nextSignedFloat(14, 1000),
                    operationMode = nextInt(4),
                    hr = nextInt(12),
                    hrConfidence = nextInt(8),
                    rr = nextFloat(14, 10),
                    rrConfidence = nextInt(8),
                    activity = nextInt(4),
                    r = nextSignedFloat(12, 1000),
                    wspo2Confidence = nextInt(8),
                    spo2 = nextFloat(11, 10),
                    wspo2PercentageComplete = nextInt(8),
                    wspo2LowSnr = nextInt(1),
                    wspo2Motion = nextInt(1),
                    wspo2LowPi = nextInt(1),
                    wspo2UnreliableR = nextInt(1),
                    wspo2State = nextInt(4),
                    scdState = nextInt(4),
                    walkSteps = 0,
                    runSteps = 0,
                    kCal = 0f,
                    totalActEnergy = 0f,
                    ibiOffset = nextInt(8)
                )
            }
        }

        private fun parsePpg4Report2FormattedData(packet: ByteArray): HspStreamData {

            return with(BitStreamReader(packet, 8)) {
                val sampleCount = nextInt(8)
                val sampleTime = nextInt(32)
                val green = nextInt(20)
                val green2 = nextInt(20)
                val ir = nextInt(20)
                val red = nextInt(20)
                val accelerationX = nextSignedFloat(14, 1000)
                val accelerationY = nextSignedFloat(14, 1000)
                val accelerationZ = nextSignedFloat(14, 1000)
                val operationMode = nextInt(4)
                val hr = nextInt(12)
                val hrConfidence = nextInt(8)
                val rr = nextFloat(14, 10)
                val rrConfidence = nextInt(8)
                val activity = nextInt(4)
                val walkSteps = nextInt(32)
                val runSteps = nextInt(32)
                val kCal = nextSignedFloat(32, 10)
                val totalActEnergy = nextSignedFloat(32, 10)
                val isLed1CurrentAdj = nextInt(1)
                val adjLed1Current = nextInt(11)
                val isLed2CurrentAdj = nextInt(1)
                val adjLed2Current = nextInt(11)
                val isLed3CurrentAdj = nextInt(1)
                val adjLed3Current = nextInt(11)
                val isTAdj = nextInt(1)
                val adjT = nextInt(2)
                val isFAdj = nextInt(1)
                val adjF = nextInt(3)
                val smpAve = nextInt(3)
                val hrmAfeState = nextInt(2)
                val isHighMotion = nextInt(1)
                val r = nextSignedFloat(12, 1000)
                val wspo2Confidence = nextInt(8)
                val spo2 = nextFloat(11, 10)
                val wspo2PercentageComplete = nextInt(8)
                val wspo2LowSnr = nextInt(1)
                val wspo2Motion = nextInt(1)
                val wspo2LowPi = nextInt(1)
                val wspo2UnreliableR = nextInt(1)
                val wspo2State = nextInt(4)
                val scdState = nextInt(4)
                val ibiOffset = nextInt(8)

                HspStreamData(
                    sampleCount = sampleCount,
                    sampleTime = sampleTime,
                    green = green,
                    green2 = green2,
                    ir = ir,
                    red = red,
                    accelerationX = accelerationX,
                    accelerationY = accelerationY,
                    accelerationZ = accelerationZ,
                    operationMode = operationMode,
                    hr = hr,
                    hrConfidence = hrConfidence,
                    rr = rr,
                    rrConfidence = rrConfidence,
                    activity = activity,
                    r = r,
                    wspo2Confidence = wspo2Confidence,
                    spo2 = spo2,
                    wspo2PercentageComplete = wspo2PercentageComplete,
                    wspo2LowSnr = wspo2LowSnr,
                    wspo2Motion = wspo2Motion,
                    wspo2LowPi = wspo2LowPi,
                    wspo2UnreliableR = wspo2UnreliableR,
                    wspo2State = wspo2State,
                    scdState = scdState,
                    walkSteps = walkSteps,
                    runSteps = runSteps,
                    kCal = kCal,
                    totalActEnergy = totalActEnergy,
                    ibiOffset = ibiOffset
                ).apply {
                    this.isLed1CurrentAdj = isLed1CurrentAdj
                    this.adjLed1Current = adjLed1Current
                    this.isLed2CurrentAdj = isLed2CurrentAdj
                    this.adjLed2Current = adjLed2Current
                    this.isLed3CurrentAdj = isLed3CurrentAdj
                    this.adjLed3Current = adjLed3Current
                    this.isTAdj = isTAdj
                    this.adjT = adjT
                    this.isFAdj = isFAdj
                    this.adjF = adjF
                    this.smpAve = smpAve
                    this.hrmAfeState = hrmAfeState
                    this.isHighMotion = isHighMotion
                }
            }
        }
    }

    fun toCsvModel(): String {
        return arrayOf(
            sampleCount,
            TIMESTAMP_FORMAT.format(Date(sampleTime.toLong() * 1000)),
            green,
            green2,
            ir,
            red,
            accelerationX,
            accelerationY,
            accelerationZ,
            operationMode,
            hr,
            hrConfidence,
            rr,
            rrConfidence,
            activity,
            r,
            wspo2Confidence,
            spo2,
            wspo2PercentageComplete,
            wspo2LowSnr,
            wspo2Motion,
            wspo2LowPi,
            wspo2UnreliableR,
            wspo2State,
            scdState,
            walkSteps,
            runSteps,
            kCal,
            totalActEnergy,
            ibiOffset,
            TIMESTAMP_FORMAT.format(Date(currentTimeMillis)),
            currentTimeMillis
        ).joinToString(separator = ",")
    }

    fun copy(): HspStreamData {
        return HspStreamData(
            sampleCount,
            sampleTime,
            green,
            green2,
            ir,
            red,
            accelerationX,
            accelerationY,
            accelerationZ,
            operationMode,
            hr,
            hrConfidence,
            rr,
            rrConfidence,
            activity,
            r,
            wspo2Confidence,
            spo2,
            wspo2PercentageComplete,
            wspo2LowSnr,
            wspo2Motion,
            wspo2LowPi,
            wspo2UnreliableR,
            wspo2State,
            scdState,
            walkSteps,
            runSteps,
            kCal,
            totalActEnergy,
            ibiOffset,
            currentTimeMillis
        )
    }
}