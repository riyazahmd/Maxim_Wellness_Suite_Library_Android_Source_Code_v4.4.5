package com.maximintegrated.hsp

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.maximintegrated.hsp.protocol.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class HspViewModel(application: Application) : AndroidViewModel(application),
    HspResponseCallback {

    enum class StreamType {
        PPG,
        ECG,
        TEMP,
        BPT
    }

    private val hspManager = HspManager(application)

    var bluetoothDevice: BluetoothDevice? = null
        private set

    val connectionState: LiveData<Pair<BluetoothDevice?, Int>> =
        hspManager.state.map { Pair(hspManager.bluetoothDevice, hspManager.connectionState) }

    private val commandResponseMutable = MutableLiveData<HspResponse<*>>()
    val commandResponse: LiveData<HspResponse<*>> get() = commandResponseMutable

    private val streamDataMutable = MutableLiveData<HspStreamData>()
    val streamData: LiveData<HspStreamData> get() = streamDataMutable

    private val tempStreamDataMutable = MutableLiveData<HspTempStreamData>()
    val tempStreamData: LiveData<HspTempStreamData> get() = tempStreamDataMutable

    private val ecgStreamDataMutable = MutableLiveData<Array<HspEcgStreamData>>()
    val ecgStreamData: LiveData<Array<HspEcgStreamData>> get() = ecgStreamDataMutable

    private val bptStreamDataMutable = MutableLiveData<HspBptStreamData>()
    val bptStreamData: LiveData<HspBptStreamData> get() = bptStreamDataMutable

    val isDeviceSupported: LiveData<Boolean> =
        hspManager.state.map { connectionState -> connectionState.isReady }

    var deviceModel: MaximDevice = UNDEFINED

    var streamType = StreamType.PPG

    var ppgFormat = PpgFormat.PPG_9

    init {
        hspManager.hspResponseCallbacks = this
    }

    override fun onCleared() {
        if (hspManager.isConnected) {
            stopStreaming()
            disconnect()
        }
    }

    fun connect(device: BluetoothDevice) {
        if (bluetoothDevice == null) {
            bluetoothDevice = device
            hspManager
                .connect(device).enqueue()
            hspManager.requestMtu(250).enqueue()
        }
    }

    fun disconnect() {
        bluetoothDevice = null
        hspManager
            .disconnect()
            .enqueue()
    }

    /**
     * Reconnect to previously connected device
     */
    fun reconnect() {
        hspManager
            .disconnect()
            .enqueue()

        bluetoothDevice?.let {
            hspManager
                .connect(it)
                .retry(3, 100)
                .useAutoConnect(false)
                .enqueue()
        }
        hspManager.requestMtu(250).enqueue()
    }

    fun sendCommand(command: HspCommand) {
        hspManager.sendCommand(command)
    }

    fun startStreaming(isScdEnabled: Boolean = true, isLowPowerEnabled: Boolean = false) {
        if (isScdEnabled) {
            sendCommand(SetConfigurationCommand("scdpowersaving", " ", "1 10 5"))
        }

        sendCommand(SetConfigurationCommand("stream", "bin"))
        if (isLowPowerEnabled) {
            sendCommand(ReadCommand("ppg", 8))
        } else {
            sendCommand(ReadCommand("ppg", 9))
        }
    }

    fun startTempStreaming(sampleIntervalInMillis: Int) {
        sendCommand(SetConfigurationCommand("stream", "bin"))
        sendCommand(SetConfigurationCommand("temp", "sr", sampleIntervalInMillis.toString()))
        sendCommand(ReadCommand("temp", 0))
    }

    fun startEcgStreaming() {
        sendCommand(SetConfigurationCommand("stream", "bin"))
        sendCommand(ReadCommand("ecg", 2))
    }

    fun startBptCalibrationStreaming() {
        sendCommand(ReadCommand("bpt", 0))
    }

    fun startBptEstimationStreaming() {
        sendCommand(ReadCommand("bpt", 1))
    }

    fun setBptDateTime() {
        val timestamp = SimpleDateFormat("yyMMdd hhmmss").format(Date())
        sendCommand(SetConfigurationCommand("bpt", "date_time", timestamp))
    }

    fun setSysDiaBpValues(index: Int, sbp: Int, dbp: Int) {
        sendCommand(SetConfigurationCommand("bpt", "sys_dia", "$index $sbp $dbp"))
    }

    fun setSpO2Coefficients(a: Float, b: Float, c: Float) {
        val constant = 1e5f
        val strA = "%08X".format((a * constant).toInt())
        val strB = "%08X".format((b * constant).toInt())
        val strC = "%08X".format((c * constant).toInt())
        sendCommand(SetConfigurationCommand("bpt", "spo2_coefs", "$strA $strB $strC"))
    }

    fun setCalibrationIndex(index: Int) {
        sendCommand(SetConfigurationCommand("bpt", "cal_index", index.toString()))
    }

    fun setCalibrationResult(calibrationResultsInHexString: String) {
        sendCommand(SetConfigurationCommand("bpt", "cal_result", calibrationResultsInHexString))
    }

    fun getBptCalibrationResults() {
        sendCommand(GetConfigurationCommand("bpt", "cal_result"))
    }

    fun stopStreaming() {
        sendCommand(StopCommand())
    }

    override fun onCommandResponseReceived(
        device: BluetoothDevice,
        commandResponse: HspResponse<*>
    ) {
        commandResponseMutable.value = commandResponse
    }

    override fun onStreamDataReceived(device: BluetoothDevice, packet: ByteArray) {
        when (streamType) {
            StreamType.PPG -> streamDataMutable.value = HspStreamData.fromPacket(packet, ppgFormat)
            StreamType.ECG -> ecgStreamDataMutable.value = HspEcgStreamData.fromPacket(packet)
            StreamType.TEMP -> tempStreamDataMutable.value = HspTempStreamData.fromPacket(packet)
            StreamType.BPT -> bptStreamDataMutable.value = HspBptStreamData.fromPacket(packet, deviceModel)
        }
    }
}