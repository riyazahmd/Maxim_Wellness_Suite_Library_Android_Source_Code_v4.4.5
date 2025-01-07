package com.maximintegrated.zephyr

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.MtuRequest
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class ZephyrManager(context: Context) : ObservableBleManager(context) {
    companion object {
        private fun uuidFromZephyrBase(shortCode16: String): UUID {
            return UUID.fromString("BEFD${shortCode16}-C979-11E1-9B21-0800200C9A66")
        }

        private val UUID_ZEPHYR_SERVICE = uuidFromZephyrBase("FF20")
        private val UUID_SUMMARY_CHARACTERISTIC = uuidFromZephyrBase("FF60")
        private val UUID_PERIOD_CONFIG_DESC_CHARACTERISTIC = uuidFromZephyrBase("FFA0")
        private val UUID_TX_QUEUE_CHARACTERISTIC = uuidFromZephyrBase("FF68") //Notify
        private val UUID_RX_QUEUE_CHARACTERISTIC = uuidFromZephyrBase("FF69") //Write
    }

    private var summaryCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null

    var zephyrManagerCallbacks: IZephyrManagerCallbacks? = null

    private val summaryCallback = object : ZephyrSummaryCallback() {
        override fun onSummaryReceived(device: BluetoothDevice, summary: ZephyrSummary) {
            zephyrManagerCallbacks?.onSummaryReceived(device, summary)
        }
    }

    private val txCallback = object : ZephyrTxCallback() {
        override fun onResponseReceived(device: BluetoothDevice, message: ZephyrResponseMessage) {
            zephyrManagerCallbacks?.onResponseReceived(device, message)
        }
    }

    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
            summaryCharacteristic = null
            txCharacteristic = null
            rxCharacteristic = null
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val zephyrService = gatt.getService(UUID_ZEPHYR_SERVICE)
            summaryCharacteristic =
                zephyrService?.getCharacteristic(UUID_SUMMARY_CHARACTERISTIC)
            txCharacteristic =
                zephyrService?.getCharacteristic(UUID_TX_QUEUE_CHARACTERISTIC)
            rxCharacteristic =
                zephyrService?.getCharacteristic(UUID_RX_QUEUE_CHARACTERISTIC)

            return summaryCharacteristic.hasNotifyProperty &&
                    txCharacteristic.hasNotifyProperty &&
                    rxCharacteristic.hasWriteProperty
        }
    }

    public override fun requestMtu(mtu: Int): MtuRequest {
        return super.requestMtu(mtu)
    }

    public override fun getMtu(): Int {
        return super.getMtu()
    }

    fun enableSummaryCharacteristicNotifications() {
        if (isConnected) {
            setNotificationCallback(summaryCharacteristic)
                .with(summaryCallback)
            enableNotifications(summaryCharacteristic)
                .done { device ->
                    Timber.i(
                        "Enabled summary notifications (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Failed to enable summary notifications (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }

    fun disableSummaryCharacteristicNotifications() {
        if (isConnected) {
            disableNotifications(summaryCharacteristic)
                .done { device ->
                    Timber.i(
                        "Disabled summary notifications (Device: %s)",
                        device
                    )
                }
                .enqueue()
        }
    }

    fun updateSummaryPeriod(periodInSec: Int) {
        if (isConnected) {
            val buffer = ByteBuffer.allocate(2)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.putShort(periodInSec.toShort())
            writeDescriptor(
                summaryCharacteristic?.getDescriptor(
                    UUID_PERIOD_CONFIG_DESC_CHARACTERISTIC
                ),
                buffer.array()
            ).done { device ->
                Timber.i(
                    "Disabled summary notifications (Device: %s)",
                    device
                )
            }
                .enqueue()
        }
    }

    fun enableTxCharacteristicNotifications() {
        if (isConnected) {
            setNotificationCallback(txCharacteristic)
                .with(txCallback)
            enableNotifications(txCharacteristic)
                .done { device ->
                    Timber.i(
                        "Enabled tx notifications (Device: %s)",
                        device
                    )
                }
                .fail { device, status ->
                    Timber.e(
                        "Failed to enable tx notifications (Device: %s, Status: %d)",
                        device,
                        status
                    )
                }
                .enqueue()
        }
    }

    fun disableTxCharacteristicNotifications() {
        if (isConnected) {
            disableNotifications(txCharacteristic)
                .done { device ->
                    Timber.i(
                        "Disabled tx notifications (Device: %s)",
                        device
                    )
                }
                .enqueue()
        }
    }

    fun startBreathWaveform() {
        if (isConnected) {
            writeCharacteristic(
                rxCharacteristic,
                createRequestMessagePacket(ZEPHYR_BREATH_WAVEFORM_TRANSMIT_ID, byteArrayOf(1))
            ).done { device ->
                Timber.i(
                    "startBreathWaveform data (Device: %s)",
                    device
                )
            }
                .enqueue()
        }
    }

    fun stopBreathWaveform() {
        if (isConnected) {
            writeCharacteristic(
                rxCharacteristic,
                createRequestMessagePacket(ZEPHYR_BREATH_WAVEFORM_TRANSMIT_ID, byteArrayOf(0))
            ).done { device ->
                Timber.i(
                    "stopBreathWaveform data (Device: %s)",
                    device
                )
            }
                .enqueue()
        }
    }
}