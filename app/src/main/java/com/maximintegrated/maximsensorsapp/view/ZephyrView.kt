package com.maximintegrated.maximsensorsapp.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.BleConnectionInfo
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.decimalFormat
import com.maximintegrated.zephyr.ZephyrSummary
import kotlinx.android.synthetic.main.view_multi_channel_chart.view.*

class ZephyrView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var zephyrSummary: ZephyrSummary? = null
        set(value) {
            field = value
            updateSummaryValues(value)
        }

    var bleConnectionInfo: BleConnectionInfo?
        get() = bleConnectionView.connectionInfo
        set(value) {
            bleConnectionView.connectionInfo = value
        }

    private val hrValueView: TextView
    private val rrValueView: TextView
    private val rrConfValueView: TextView
    private val activityValueView: TextView
    private val bleConnectionView: BleConnectionView
    private val zephyrChartView: MultiChannelChartView

    init {
        inflate(context, R.layout.view_rr_ref_device, this)

        hrValueView = findViewById(R.id.hr_value_view)
        rrValueView = findViewById(R.id.rr_value_view)
        rrConfValueView = findViewById(R.id.rr_conf_value_view)
        activityValueView = findViewById(R.id.activity_value_view)
        bleConnectionView = findViewById(R.id.ble_connection_view)
        zephyrChartView = findViewById(R.id.chartView)
    }

    private fun updateSummaryValues(summary: ZephyrSummary?) {
        if (summary == null) {
            hrValueView.setText(R.string.rr_ref_empty_value)
            rrValueView.setText(R.string.rr_ref_empty_value)
            rrConfValueView.setText(R.string.rr_ref_empty_value)
            activityValueView.setText(R.string.rr_ref_empty_value)
        } else {
            if (summary.hr != ZephyrSummary.INVALID_VALUE) {
                hrValueView.text = summary.hr.toString()
            } else {
                hrValueView.setText(R.string.rr_ref_empty_value)
            }
            if (summary.rr != ZephyrSummary.INVALID_VALUE.toFloat()) {
                rrValueView.text = "%.1f".format(summary.rr)
            } else {
                rrValueView.setText(R.string.rr_ref_empty_value)
            }
            if (summary.rrConf != ZephyrSummary.INVALID_VALUE) {
                rrConfValueView.text = summary.rrConf.toString()
            } else {
                rrConfValueView.setText(R.string.rr_ref_empty_value)
            }
            if (summary.activity != ZephyrSummary.INVALID_VALUE.toFloat()) {
                activityValueView.text = "%.2f".format(summary.activity)
            } else {
                activityValueView.setText(R.string.rr_ref_empty_value)
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

    fun setupChart() {
        zephyrChartView.dataSetInfoList = listOf(
            DataSetInfo(R.string.respiration_rate, R.color.channel_red)
        )

        zephyrChartView.titleView.text = context.getString(R.string.rr_breath_waveform)
        zephyrChartView.maximumEntryCount = 1080
        zephyrChartView.setFormatterForYAxis(FloatValueFormatter(decimalFormat))
        zephyrChartView.setVisibilityForChipGroup(false)
    }

    fun addData(data: Int) {
        zephyrChartView.addData(data)
    }

    fun reset() {
        hrValueView.setText(R.string.rr_ref_empty_value)
        rrValueView.setText(R.string.rr_ref_empty_value)
        rrConfValueView.setText(R.string.rr_ref_empty_value)
        activityValueView.setText(R.string.rr_ref_empty_value)
        zephyrChartView.clearChart()
    }
}