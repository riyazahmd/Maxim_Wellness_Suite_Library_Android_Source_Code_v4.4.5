package com.maximintegrated.maximsensorsapp.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.BleConnectionInfo
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.nonin.PlxMeasurement

class NoninView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var plxMeasurement: PlxMeasurement? = null
        set(value) {
            field = value
            updatePlxMeasurementValues(value)
        }

    var bleConnectionInfo: BleConnectionInfo?
        get() = bleConnectionView.connectionInfo
        set(value) {
            bleConnectionView.connectionInfo = value
        }

    private val spo2ValueView: TextView
    private val prValueView: TextView
    private val paiValueView: TextView
    private val bleConnectionView: BleConnectionView

    init {
        inflate(context, R.layout.view_spo2_ref_device, this)

        spo2ValueView = findViewById(R.id.spo2_value_view)
        prValueView = findViewById(R.id.pr_value_view)
        paiValueView = findViewById(R.id.pulse_amp_index_value_view)
        bleConnectionView = findViewById(R.id.ble_connection_view)
    }

    private fun updatePlxMeasurementValues(plxMeasurement: PlxMeasurement?) {
        if (plxMeasurement == null) {
            spo2ValueView.setText(R.string.spo2_ref_empty_value)
            prValueView.setText(R.string.spo2_ref_empty_value)
            paiValueView.setText(R.string.spo2_ref_empty_value)
        } else {
            spo2ValueView.text = plxMeasurement.spo2Normal.toInt().toString()
            prValueView.text = plxMeasurement.prNormal.toInt().toString()
            paiValueView.text = plxMeasurement.pulseAmplitudeIndex.toInt().toString()
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

    fun reset() {
        spo2ValueView.setText(R.string.spo2_ref_empty_value)
        prValueView.setText(R.string.spo2_ref_empty_value)
        paiValueView.setText(R.string.spo2_ref_empty_value)
    }
}