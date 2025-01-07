package com.maximintegrated.maximsensorsapp.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.BleConnectionInfo
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.qardio.BloodPressureMeasurement
import kotlin.math.roundToInt

class QardioView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var bloodPressureMeasurement: BloodPressureMeasurement? = null
        set(value) {
            field = value
            updateBloodPressureMeasurementValues()
        }

    var bleConnectionInfo: BleConnectionInfo?
        get() = bleConnectionView.connectionInfo
        set(value) {
            bleConnectionView.connectionInfo = value
        }

    private val systolicValueView: TextView
    private val diastolicValueView: TextView
    private val pulseValueView: TextView
    private val bleConnectionView: BleConnectionView
    private val bptConnectionGroup: Group
    private val bptResultGroup: Group


    init {
        inflate(context, R.layout.view_bpt_ref_device, this)

        systolicValueView = findViewById(R.id.systolic_value_view)
        diastolicValueView = findViewById(R.id.diastolic_value_view)
        pulseValueView = findViewById(R.id.pulse_value_view)
        bleConnectionView = findViewById(R.id.ble_connection_view)
        bptConnectionGroup = findViewById(R.id.bptConnectionGroup)
        bptResultGroup = findViewById(R.id.bptResultGroup)
    }

    private fun updateBloodPressureMeasurementValues() {
        val measurement = bloodPressureMeasurement

        if (measurement == null || measurement.systolic.isNaN() || measurement.diastolic.isNaN()) {
            systolicValueView.setText(R.string.bpt_ref_empty_value)
            diastolicValueView.setText(R.string.bpt_ref_empty_value)
            pulseValueView.setText(R.string.bpt_ref_empty_value)
        } else {
            systolicValueView.text = measurement.systolic.roundToInt().toString()
            diastolicValueView.text = measurement.diastolic.roundToInt().toString()

            val pulseRate = measurement.pulseRate
            if (pulseRate == null) {
                pulseValueView.setText(R.string.bpt_ref_empty_value)
            } else {
                pulseValueView.text = pulseRate.roundToInt().toString()
            }
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

    fun setOnlyDisplay(){
        bptResultGroup.isVisible = true
        bptConnectionGroup.isVisible = false
    }

    fun setOnlyDevice(){
        bptResultGroup.isVisible = false
        bptConnectionGroup.isVisible = true
    }

    fun reset() {
        systolicValueView.setText(R.string.bpt_ref_empty_value)
        diastolicValueView.setText(R.string.bpt_ref_empty_value)
        pulseValueView.setText(R.string.bpt_ref_empty_value)
    }
}