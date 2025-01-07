package com.maximintegrated.maximsensorsapp.ecg

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.maximintegrated.ecgfilter.EcgFilterAlgorithm
import com.maximintegrated.ecgfilter.EcgFilterInitConfig
import com.maximintegrated.ecgfilter.EcgFilterInput
import com.maximintegrated.ecgfilter.EcgFilterOutput
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.exts.accentColor
import kotlinx.android.synthetic.main.view_ecg_chart.view.*


class EcgChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    companion object {
        private const val SAMPLING_RATE = 128
        private const val X_AXIS_SCALE_IN_SECONDS = 4
        const val MAX_ENTRY_COUNT = SAMPLING_RATE * X_AXIS_SCALE_IN_SECONDS
    }

    var invertClicked: (() -> Unit)? = null

    private val ecgDataSet =
        LineDataSet(null, context.getString(R.string.ecg_data))

    private val ecgFilterInitConfig = EcgFilterInitConfig(SAMPLING_RATE, 1.0f, false, false)
    private val ecgFilterInput = EcgFilterInput()
    private val ecgFilterOutput = EcgFilterOutput()

    init {
        inflate(context, R.layout.view_ecg_chart, this)

        invertButton.setOnClickListener {
            invertClicked?.invoke()
        }

        setupChart()
    }

    private fun setupChart() {
        setupDataSet(ecgDataSet, context.accentColor)

        chart.apply {
            setNoDataTextColor(context.accentColor)

            legend.isEnabled = true
            legend.textSize = context.resources.getDimension(R.dimen.chart_legend_text_size)

            description.isEnabled = false

            xAxis.isEnabled = false
            axisRight.isEnabled = false

            axisLeft.isEnabled = true
            axisLeft.setDrawTopYLabelEntry(true)
            axisLeft.textSize = context.resources.getDimension(R.dimen.chart_y_axis_text_size)

            setTouchEnabled(false)
            isAutoScaleMinMaxEnabled = true

            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = MAX_ENTRY_COUNT.toFloat()

            setVisibleXRangeMaximum(MAX_ENTRY_COUNT.toFloat())
        }
    }

    private fun setupDataSet(dataSet: LineDataSet, @ColorInt dataSetColor: Int) {
        dataSet.apply {
            color = dataSetColor
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
        }
    }

    private fun addEntryToDataSet(dataSet: LineDataSet, xValue: Float, yValue: Float) {
        if (dataSet.entryCount == MAX_ENTRY_COUNT) {
            dataSet.removeFirst()
        }

        dataSet.addEntry(Entry(xValue, yValue))
    }


    fun clearData() {
        val chartData = chart.data ?: return

        chartData.removeDataSet(ecgDataSet)
        ecgDataSet.clear()

        chart.apply {
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = MAX_ENTRY_COUNT.toFloat()

            data = null
            invalidate()
        }

        invertButton.isEnabled = false

        EcgFilterAlgorithm.end()
    }

    fun applyFilterAndAddData(ecg: Float): Float {
        if (chart.data == null) {
            chart.data = LineData()
        }

        // we are adding data sets here because drawing the chart with empty data set throws exception
        if (chart.data.dataSets.isEmpty()) {
            chart.data.addDataSet(ecgDataSet)
            invertButton.isEnabled = true
            EcgFilterAlgorithm.init(ecgFilterInitConfig)
        }

        val newEntryX = if (ecgDataSet.entryCount != 0) {
            ecgDataSet.getEntryForIndex(ecgDataSet.entryCount - 1).x + 1f
        } else {
            0f
        }

        val filteredEcg = applySelectedFilters(ecg)

        addEntryToDataSet(ecgDataSet, newEntryX, filteredEcg)

        chart.apply {
            if (newEntryX > MAX_ENTRY_COUNT) {
                xAxis.resetAxisMinimum()
                xAxis.resetAxisMaximum()
            }

            data.notifyDataChanged()
            notifyDataSetChanged()
            moveViewToX(newEntryX)
        }

        return filteredEcg
    }

    private fun applySelectedFilters(ecg: Float): Float {
        ecgFilterInput.ecgRaw = ecg
        ecgFilterInput.cutoffFrequency = if (lowPass40Chip.isChecked) 40 else 0
        ecgFilterInput.notchFrequency = if (notch50Chip.isChecked) {
            if (notch60Chip.isChecked) 5060 else 50
        } else {
            if (notch60Chip.isChecked) 60 else 0
        }

        ecgFilterInput.isAdaptiveFilterOn = true
        ecgFilterInput.isBaselineRemovalOn = true

        EcgFilterAlgorithm.run(ecgFilterInput, ecgFilterOutput)

        return ecgFilterOutput.ecgProcessed
    }
}