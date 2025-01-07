package com.maximintegrated.hsp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.maximintegrated.hsp.protocol.HspCommand
import com.maximintegrated.hsp.protocol.HspResponse
import no.nordicsemi.android.ble.MtuRequest
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import timber.log.Timber
import java.util.*

class HspManager(context: Context) : ObservableBleManager(context) {
    companion object {
        private val UUID_HSP_SERVICE = UUID.fromString("00001523-1212-efde-1523-785feabcd123")
        private val UUID_HSP_COMMAND_CHARACTERISTIC =
            UUID.fromString("00001027-1212-efde-1523-785feabcd123")
        private val UUID_HSP_RESPONSE_CHARACTERISTIC =
            UUID.fromString("00001011-1212-efde-1523-785feabcd123")

        private val UUID_MRD104_SERVICE = UUID.fromString("6E400000-B5A3-F393-E0A9-E50E24DCCA9E")
        private val UUID_MRD104_COMMAND_CHARACTERISTIC =
            UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        private val UUID_MRD104_RESPONSE_CHARACTERISTIC =
            UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")

        const val COMMAND_SEPARATOR = '\n'
    }

    private var commandCharacteristic: BluetoothGattCharacteristic? = null
    private var responseCharacteristic: BluetoothGattCharacteristic? = null

    var hspResponseCallbacks: HspResponseCallback? = null

    private val responseDataCallback = object : HspResponseDataCallback() {
        override fun onCommandResponseReceived(
            device: BluetoothDevice,
            commandResponse: HspResponse<*>
        ) {
            hspResponseCallbacks?.onCommandResponseReceived(device, commandResponse)
        }

        override fun onStreamDataReceived(device: BluetoothDevice, packet: ByteArray) {
            hspResponseCallbacks?.onStreamDataReceived(device, packet)
        }
    }

    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
            commandCharacteristic = null
            responseCharacteristic = null
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            var service = gatt.getService(UUID_HSP_SERVICE)
            service?.let {
                commandCharacteristic = it.getCharacteristic(UUID_HSP_COMMAND_CHARACTERISTIC)
                responseCharacteristic = it.getCharacteristic(UUID_HSP_RESPONSE_CHARACTERISTIC)
            }

            if (service == null) {
                service = gatt.getService(UUID_MRD104_SERVICE)
                service?.let {
                    commandCharacteristic = it.getCharacteristic(UUID_MRD104_COMMAND_CHARACTERISTIC)
                    responseCharacteristic =
                        it.getCharacteristic(UUID_MRD104_RESPONSE_CHARACTERISTIC)
                }
            }

            return commandCharacteristic.hasWriteProperty && responseCharacteristic.hasNotifyProperty
        }

        override fun initialize() {
            super.initialize()
            enableResponseCharacteristicNotifications()

        }
    }

    fun enableResponseCharacteristicNotifications() {
        if (isConnected) {
            setNotificationCallback(responseCharacteristic)
                .merge(HspResponseDataMerger())
                .with(responseDataCallback)
            enableNotifications(responseCharacteristic)
                .done { device ->
                    Timber.i(
                        "Enabled response notifications (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Failed to enable response notifications (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }

    public override fun requestMtu(mtu: Int): MtuRequest {
        return super.requestMtu(mtu)
    }

    fun disableResponseCharacteristicNotifications() {
        if (isConnected) {
            disableIndications(responseCharacteristic)
                .done { device ->
                    Timber.i(
                        "Disabled response notifications (Device: %s)",
                        device
                    )
                }
                .enqueue()
        }
    }

    fun sendCommand(command: HspCommand) {
        if (isConnected) {
            val commandStr = command.toText()
            val paddedCommand = if (commandStr.endsWith(COMMAND_SEPARATOR)) {
                commandStr
            } else {
                commandStr + COMMAND_SEPARATOR
            }
            Timber.d("SENT PACKET: ${paddedCommand.toString()}")
            Timber.d("SENT PACKET in BYTE ${paddedCommand.map { "0x%02X".format(it.toByte())}}")
            writeCharacteristic(commandCharacteristic, Data.from(paddedCommand))
                .split(HspCommandDataSplitter())
                .enqueue()

//            val paddedCommand: String
//            if (type == "mrd104") {
//                paddedCommand = if (commandStr.endsWith(COMMAND_SEPARATOR)) {
//                    commandStr.removeSuffix(COMMAND_SEPARATOR.toString())
//                } else {
//                    commandStr
//                }
//            } else {
//                paddedCommand = if (commandStr.endsWith(COMMAND_SEPARATOR)) {
//                    commandStr
//                } else {
//                    commandStr + COMMAND_SEPARATOR
//                }
//            }
        }
    }
}
