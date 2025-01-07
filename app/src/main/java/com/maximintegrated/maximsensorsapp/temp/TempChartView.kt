package com.maximintegrated.maximsensorsapp.temp

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.exts.accentColor
import kotlinx.android.synthetic.main.view_temp_chart.view.*
import java.text.DecimalFormat


class TempChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    companion object {
        const val MAX_ENTRY_COUNT = 20
    }

    private val temperatureDataSet = LineDataSet(null, context.getString(R.string.temp_legend))


    private val valueFormatter = DecimalFormat("#.00")

    private val celsiusUnit = context.getString(R.string.temp_unit_celsius)
    private val fahrenheitUnit = context.getString(R.string.temp_unit_fahrenheit)

    private val temperatureUnit
        get() = if (celsiusChip.isChecked) {
            celsiusUnit
        } else {
            fahrenheitUnit
        }

    init {
        inflate(context, R.layout.view_temp_chart, this)

        setupChart()
        setupUnitConverter()
    }

    private fun setupChart() {
        setupDataSet(temperatureDataSet, context.accentColor)

        chart.apply {
            setNoDataTextColor(context.accentColor)

            legend.isEnabled = false
            description.isEnabled = false

            xAxis.isEnabled = false
            axisRight.isEnabled = false

            axisLeft.isEnabled = true
            axisLeft.setDrawTopYLabelEntry(true)
            axisLeft.textSize = context.resources.getDimension(R.dimen.chart_y_axis_text_size)

            axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return "${valueFormatter.format(value)} $temperatureUnit"
                }
            }

            setTouchEnabled(false)
            isAutoScaleMinMaxEnabled = true

            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = MAX_ENTRY_COUNT.toFloat()

            setVisibleXRangeMaximum(MAX_ENTRY_COUNT.toFloat())
        }
    }

    private fun setupUnitConverter() {
        celsiusChip.setOnCheckedChangeListener { _, toCelsius ->
            (0 until temperatureDataSet.entryCount).forEach { index ->
                val entry = temperatureDataSet.getEntryForIndex(index)

                entry.y = if (toCelsius) {
                    fahrenheitToCelsius(entry.y)
                } else {
                    celsiusToFahrenheit(entry.y)
                }
            }

            temperatureDataSet.notifyDataSetChanged()
            chart.data?.notifyDataChanged()
            chart.invalidate()
        }
    }

    private fun setupDataSet(dataSet: LineDataSet, @ColorInt dataSetColor: Int) {
        dataSet.apply {
            color = dataSetColor
            lineWidth = 2f
            setCircleColor(dataSetColor)
            circleHoleColor = dataSetColor
            setDrawCircles(true)
            setDrawValues(false)
        }
    }

    private fun addEntryToDataSet(dataSet: LineDataSet, xValue: Float, yValue: Float) {
        if (dataSet.entryCount == MAX_ENTRY_COUNT) {
            dataSet.removeFirst()
        }

        val y = if (celsiusChip.isChecked) {
            yValue
        } else {
            celsiusToFahrenheit(yValue)
        }

        dataSet.addEntry(Entry(xValue, y))
    }

    fun clearData() {
        val chartData = chart.data ?: return

        chartData.removeDataSet(temperatureDataSet)
        temperatureDataSet.clear()

        chart.apply {
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = MAX_ENTRY_COUNT.toFloat()

            data = null
            invalidate()
        }
    }

    fun addTempData(temperature: Float) {
        if (chart.data == null) {
            chart.data = LineData()
        }

        // we are adding data sets here because drawing the chart with empty data set throws exception
        if (chart.data.dataSets.isEmpty()) {
            chart.data.addDataSet(temperatureDataSet)
        }

        val newEntryX = if (temperatureDataSet.entryCount != 0) {
            temperatureDataSet.getEntryForIndex(temperatureDataSet.entryCount - 1).x + 1f
        } else {
            0f
        }

        addEntryToDataSet(temperatureDataSet, newEntryX, temperature)

        chart.apply {
            if (newEntryX > MAX_ENTRY_COUNT) {
                xAxis.resetAxisMinimum()
                xAxis.resetAxisMaximum()
            }

            data.notifyDataChanged()
            notifyDataSetChanged()
            moveViewToX(newEntryX)
        }
    }


}