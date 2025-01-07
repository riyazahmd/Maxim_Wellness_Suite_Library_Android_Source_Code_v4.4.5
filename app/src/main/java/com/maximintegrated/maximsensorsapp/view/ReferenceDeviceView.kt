package com.maximintegrated.maximsensorsapp.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.polar.HeartRateMeasurement
import com.maximintegrated.maximsensorsapp.BleConnectionInfo
import com.maximintegrated.maximsensorsapp.R

class ReferenceDeviceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var heartRateMeasurement: HeartRateMeasurement? = null
        set(value) {
            field = value
            updateHearRateMeasurementValues()
        }

    var bleConnectionInfo: BleConnectionInfo?
        get() = bleConnectionView.connectionInfo
        set(value) {
            bleConnectionView.connectionInfo = value
        }

    private val contactDetectedValueView: TextView
    private val heartRateValueView: TextView
    private val bleConnectionView: BleConnectionView


    init {
        inflate(context, R.layout.view_reference_device, this)

        contactDetectedValueView = findViewById(R.id.contact_detected_value_view)
        heartRateValueView = findViewById(R.id.heart_rate_value_view)
        bleConnectionView = findViewById(R.id.ble_connection_view)
    }

    private fun updateHearRateMeasurementValues() {
        val measurement = heartRateMeasurement

        if (measurement == null) {
            contactDetectedValueView.setText(R.string.polar_empty_value)
            heartRateValueView.setText(R.string.polar_empty_value)
        } else {
            contactDetectedValueView.setText(
                when (measurement.contactDetected) {
                    true -> R.string.polar_contact_detected_yes
                    false -> R.string.polar_contact_detected_no
                    else -> R.string.polar_empty_value
                }
            )

            heartRateValueView.text = measurement.heartRate.toString()
        }
    }

    fun onSearchButtonClick(action: () -> Unit) {
        bleConnectionView.onSearchButtonClick(action)
    }

    fun onConnectButtonClick(action: () -> Unit) {
        bleConnectionView.onConnectButtonClick(action)
    }

    fun onDisconnectClick(action: () -> Unit) {
        bleConnectionView.onDisconnectClick(action)
    }

    fun onChangeDeviceClick(action: () -> Unit) {
        bleConnectionView.onChangeDeviceClick(action)
    }
}