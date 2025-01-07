package com.maximintegrated.maximsensorsapp.sleep.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.sleep.SleepDataModel
import com.maximintegrated.maximsensorsapp.sleep.utils.ColorUtil
import kotlinx.android.synthetic.main.view_pie_chart.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val formatter: SimpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)

    init {
        inflate(context, R.layout.view_pie_chart, this)
    }

    fun setupChart(dataModel: SleepDataModel) {
        val pieChart: PieChart = this.findViewById(R.id.pieChart)

        val dateText: TextView = this.dateText
        val wakeText: TextView = this.wakeText
        val remText: TextView = this.remText
        val lightText: TextView = this.lightText
        val deepText: TextView = this.deepText
        val sleepInfoText: TextView = this.sleepInfo


        dateText.text = formatter.format(dataModel.date)

        wakeText.text = buildSpannedString {
            bold { append(context.getString(R.string.wake)).append(": ") }
            append(getFormattedHour(dataModel.wakeCount.toFloat()))
        }

        remText.text = buildSpannedString {
            bold { append(context.getString(R.string.rem)).append(": ") }
            append(getFormattedHour(dataModel.remCount.toFloat()))
        }

        lightText.text = buildSpannedString {
            bold { append(context.getString(R.string.light)).append(": ") }
            append(getFormattedHour(dataModel.lightCount.toFloat()))
        }

        deepText.text = buildSpannedString {
            bold { append(context.getString(R.string.deep)).append(": ") }
            append(getFormattedHour(dataModel.deepCount.toFloat()))
        }

        val totalSleepTime = dataModel.totalCount.toFloat()
        sleepInfoText.text = buildSpannedString {
            bold { append(context.getString(R.string.total_sleep_time)).append(": ") }
            append(getFormattedHour(totalSleepTime))
        }

        pieChart.setDrawCenterText(false)

        pieChart.centerText = dataModel.totalCount.toString()
        pieChart.setCenterTextSize(20f)

        pieChart.description.isEnabled = false


        pieChart.legend.isEnabled = false

        val dataSet = dataModel.pieDataSet

        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f


        val colors = ArrayList<Int>()

        colors.add(ColorUtil.COLOR_WAKE)
        colors.add(ColorUtil.COLOR_REM)
        colors.add(ColorUtil.COLOR_LIGHT)
        colors.add(ColorUtil.COLOR_DEEP)

        dataSet.colors = colors
        dataSet.valueFormatter = PercentFormatter(pieChart)

        val data = PieData(dataSet)


        data.setValueTextSize(11f)
        data.setValueTextColor(Color.rgb(218, 222, 229))
        pieChart.data = data

        with(pieChart) {
            setUsePercentValues(true)
            setTouchEnabled(false)
            highlightValues(null)
        }
    }

    private fun getFormattedHour(time: Float): String {
        var hours = (time / 60).toInt()
        val minutes = hours % 60
        hours /= 60
        return "${hours}h ${minutes}min"

    }
}