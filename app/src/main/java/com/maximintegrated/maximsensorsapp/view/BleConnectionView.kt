package com.maximintegrated.maximsensorsapp.view

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.AttributeSet
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.constraintlayout.helper.widget.Layer
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.maximintegrated.maximsensorsapp.BleConnectionInfo
import com.maximintegrated.maximsensorsapp.R
import eo.view.batterymeter.BatteryMeterView
import eo.view.bluetoothstate.BluetoothStateView

class BleConnectionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var connectionInfo: BleConnectionInfo? = null
        set(value) {
            field = value

            if (value != null) {
                showBleConnectionInfo(value)
            } else {
                showSearchDevice()
            }
        }

    private val bluetoothStateView: BluetoothStateView
    private val deviceNameView: TextView
    private val deviceAddressView: TextView
    private val deviceLayer: Layer
    private val batteryMeterView: BatteryMeterView
    private val batteryLevelTexView: TextView
    private val searchButton: MaterialButton
    private val connectButton: MaterialButton
    private val connectingMessageView: TextView

    private var onDisconnectClickListener: (() -> Unit)? = null
    private var onChangeDeviceClickListener: (() -> Unit)? = null

    init {
        inflate(context, R.layout.view_ble_connection, this)

        bluetoothStateView = findViewById(R.id.bluetooth_state_view)
        deviceNameView = findViewById(R.id.bluetooth_device_name)
        deviceAddressView = findViewById(R.id.bluetooth_device_address)
        deviceLayer = findViewById(R.id.device_layer)
        batteryMeterView = findViewById(R.id.battery_meter_view)
        batteryLevelTexView = findViewById(R.id.battery_level_text_view)
        searchButton = findViewById(R.id.search_button)
        connectButton = findViewById(R.id.connect_button)
        connectingMessageView = findViewById(R.id.connecting_message_view)

        deviceLayer.setOnClickListener {
            showDeviceMenu()
        }
    }

    fun onSearchButtonClick(action: () -> Unit) {
        searchButton.setOnClickListener {
            action()
        }
    }

    fun onConnectButtonClick(action: () -> Unit) {
        connectButton.setOnClickListener {
            action()
        }
    }

    fun onDisconnectClick(action: () -> Unit) {
        onDisconnectClickListener = action
    }

    fun onChangeDeviceClick(action: () -> Unit) {
        onChangeDeviceClickListener = action
    }

    private fun showBleConnectionInfo(connectionInfo: BleConnectionInfo) {
        bluetoothStateView.state = connectionInfo.bluetoothState
        deviceNameView.text = connectionInfo.deviceName
            ?: context.getString(R.string.ble_connection_unknown_device_name)
        deviceAddressView.text = connectionInfo.deviceAddress ?: ""
        batteryMeterView.chargeLevel = connectionInfo.batteryLevel
        batteryMeterView.isCharging = connectionInfo.isCharging
        connectionInfo.batteryLevel?.let {
            val str = "$it%"
            batteryLevelTexView.text = str
        }

        updateBatteryLevelTooltip(connectionInfo.batteryLevel, connectionInfo.isCharging)

        searchButton.isInvisible = true

        deviceLayer.isVisible = true
        bluetoothStateView.isVisible = true
        deviceNameView.isVisible = true
        deviceAddressView.isVisible = true
        batteryMeterView.isVisible =
            connectionInfo.connectionStateCode == BluetoothAdapter.STATE_CONNECTED
        batteryLevelTexView.isVisible =
            connectionInfo.connectionStateCode == BluetoothAdapter.STATE_CONNECTED
        connectButton.isVisible =
            connectionInfo.connectionStateCode == BluetoothAdapter.STATE_DISCONNECTED
        connectingMessageView.isVisible =
            connectionInfo.connectionStateCode == BluetoothAdapter.STATE_CONNECTING
    }

    private fun showSearchDevice() {
        ViewCompat.setTooltipText(batteryMeterView, null)
        bluetoothStateView.isVisible = false
        deviceNameView.isVisible = false
        deviceAddressView.isVisible = false
        batteryMeterView.isVisible = false
        batteryLevelTexView.isVisible = false
        connectButton.isVisible = false
        connectingMessageView.isVisible = false
        deviceLayer.isVisible = false

        searchButton.isVisible = true
    }

    private fun updateBatteryLevelTooltip(batteryLevel: Int?, isCharging: Boolean) {
        ViewCompat.setTooltipText(
            batteryMeterView,
            when {
                batteryLevel == null -> context.getString(R.string.ble_connection_battery_level_unknown)
                isCharging -> context.getString(
                    R.string.ble_connection_battery_level_charging,
                    batteryLevel
                )
                else -> context.getString(R.string.ble_connection_battery_level, batteryLevel)
            }
        )
    }

    private fun showDeviceMenu() {
        val popup = PopupMenu(context, deviceLayer)
        popup.menuInflater.inflate(getDeviceMenuResource(), popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.disconnect_action -> {
                    onDisconnectClickListener?.invoke()
                    true
                }
                R.id.change_device_action -> {
                    onChangeDeviceClickListener?.invoke()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    @MenuRes
    private fun getDeviceMenuResource() = if (connectButton.isVisible) {
        R.menu.ble_connection_disconnected_device
    } else {
        R.menu.ble_connection_connected_device
    }
}