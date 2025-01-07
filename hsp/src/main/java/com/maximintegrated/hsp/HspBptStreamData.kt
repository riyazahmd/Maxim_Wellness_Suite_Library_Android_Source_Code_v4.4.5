package com.maximintegrated.hsp

import java.text.SimpleDateFormat
import java.util.*

data class HspBptStreamData(
    val status: Int,
    val irCnt: Int,
    val redCnt: Int,
    val hr: Int,
    val progress: Int,
    var sbp: Int,
    var dbp: Int,
    val spo2: Int,
    val pulseFlag: Int,
    val r: Float,
    val ibi: Int,
    val spo2Confidence: Int,
    val bpReportFlag: Int,
    val spo2ReportFlag: Int,
    val endBpt: Int
) {

    var sampleCount: Int = 0
    var sampleTime: Int = 0
    var green: Int = 0
    var green2: Int = 0
    var accelerationX: Float = 0f
    var accelerationY: Float = 0f
    var accelerationZ: Float = 0f
    var operationMode: Int = 0
    var hrConfidence: Int = 0
    var rr: Float = 0f
    var rrConfidence: Int = 0
    var activity: Int = 0
    var wspo2PercentageComplete: Int = 0
    var wspo2LowSnr: Int = 0
    var wspo2Motion: Int = 0
    var wspo2LowPi: Int = 0
    var wspo2UnreliableR: Int = 0
    var scdState: Int = 0
    var walkSteps: Int = 0
    var runSteps: Int = 0
    var kCal: Float = 0f
    var totalActEnergy: Float = 0f
    var ibiOffset: Int = 0

    companion object {
        val CSV_HEADER_ARRAY = arrayOf(
            "Timestamp", "irCnt", "redCnt", "status", "percentCompleted", "HR", "SpO2", "pulseFlag",
            "estimatedSBP", "estimatedDBP", "r", "ibi", "spo2Confidence", "bpReportFlag", "spo2ReportFlag",
            "endBpt"
        )

        val TIMESTAMP_FORMAT = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.US)

        fun fromPacket(packet: ByteArray, deviceType: MaximDevice = ME11D): HspBptStreamData? {
            return when (deviceType) {
                ME11D -> parseME11DFormattedData(packet)
                ME15 -> parseME15FormattedData(packet)
                else -> null
            }
        }

        private fun parseME11DFormattedData(packet: ByteArray): HspBptStreamData {
            return with(BitStreamReader(packet, 8)) {
                HspBptStreamData(
                    status = nextInt(6),
                    irCnt = nextInt(19),
                    redCnt = nextInt(19),
                    hr = nextInt(9),
                    progress = nextInt(9),
                    sbp = nextInt(9),
                    dbp = nextInt(9),
                    spo2 = nextInt(8),
                    pulseFlag = nextInt(8),
                    r = nextFloat(16, 1000),
                    ibi = nextInt(16),
                    spo2Confidence = nextInt(8),
                    bpReportFlag = nextInt(1),
                    spo2ReportFlag = nextInt(1),
                    endBpt = nextInt(1)
                )
            }
        }

        private fun parseME15FormattedData(packet: ByteArray): HspBptStreamData {
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
                val walkSteps = nextInt(32)
                val runSteps = nextInt(32)
                val kCal = nextSignedFloat(32, 10)
                val totalActEnergy = nextSignedFloat(32, 10)
                val ibiOffset = nextInt(8)
                val bptStatus = nextInt(4)
                val bptProg = nextInt(9)
                val bptSysBp = nextInt(9)
                val bptDiaBp = nextInt(9)
                val bptPulseFlag = nextInt(1)

                HspBptStreamData(
                    status = bptStatus,
                    irCnt = ir,
                    redCnt = red,
                    hr = hr,
                    progress = bptProg,
                    sbp = bptSysBp,
                    dbp = bptDiaBp,
                    spo2 = (spo2 * 10f).toInt(),
                    pulseFlag = bptPulseFlag,
                    r = r,
                    ibi = 0,
                    spo2Confidence = wspo2Confidence,
                    bpReportFlag = bptPulseFlag,
                    spo2ReportFlag = wspo2State,
                    endBpt = 0
                ).apply {
                    this.sampleCount = sampleCount
                    this.sampleTime = sampleTime
                    this.green = green
                    this.green2 = green2
                    this.accelerationX = accelerationX
                    this.accelerationY = accelerationY
                    this.accelerationZ = accelerationZ
                    this.operationMode = operationMode
                    this.hrConfidence = hrConfidence
                    this.rr = rr
                    this.rrConfidence = rrConfidence
                    this.activity = activity
                    this.wspo2PercentageComplete = wspo2PercentageComplete
                    this.wspo2LowSnr = wspo2LowSnr
                    this.wspo2Motion = wspo2Motion
                    this.wspo2LowPi = wspo2LowPi
                    this.wspo2UnreliableR = wspo2UnreliableR
                    this.scdState = scdState
                    this.walkSteps = walkSteps
                    this.runSteps = runSteps
                    this.kCal = kCal
                    this.totalActEnergy = totalActEnergy
                    this.ibiOffset = ibiOffset
                }
            }
        }
    }

    val timestamp = System.currentTimeMillis()

    fun toCsvModel(): String {
        return arrayOf(
            timestamp,
            irCnt,
            redCnt,
            status,
            progress,
            hr,
            spo2,
            pulseFlag,
            sbp,
            dbp,
            r,
            ibi,
            spo2Confidence,
            bpReportFlag,
            spo2ReportFlag,
            endBpt
        )
            .joinToString(separator = ",")
    }
}