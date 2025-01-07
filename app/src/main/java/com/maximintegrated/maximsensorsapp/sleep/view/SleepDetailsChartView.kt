package com.maximintegrated.maximsensorsapp.sleep.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.sleep.SleepDataModel
import com.maximintegrated.maximsensorsapp.sleep.utils.ColorUtil
import kotlinx.android.synthetic.main.view_sleep_details_chart.view.*
import java.util.*
import kotlin.math.min
import kotlin.math.round

class SleepDetailsChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {
    init {
        inflate(context, R.layout.view_sleep_details_chart, this)
    }

    var sleepDataModel: SleepDataModel? = null

    fun setupChart(sleepDataModel: SleepDataModel) {

        this.sleepDataModel = sleepDataModel

        val bedDate = Calendar.getInstance()
        val wakeDate = Calendar.getInstance()

        bedDate.time = sleepDataModel.bedTime
        wakeDate.time = sleepDataModel.wakeTime

        bedTime.text = buildSpannedString {
            bold { append(context.getString(R.string.bed_time)).append(": ") }
            append(
                bedDate.get(Calendar.HOUR).toString() + ":" + if (bedDate.get(Calendar.MINUTE) < 10) ("0" + bedDate.get(
                    Calendar.MINUTE
                )) else bedDate.get(Calendar.MINUTE)
            )
        }

        wakeTime.text = buildSpannedString {
            bold { append(context.getString(R.string.wake_time)).append(": ") }
            append(
                wakeDate.get(Calendar.HOUR_OF_DAY).toString() + ":" + if (wakeDate.get(Calendar.MINUTE) < 10) ("0" + wakeDate.get(
                    Calendar.MINUTE
                )) else wakeDate.get(Calendar.MINUTE)
            )
        }


        configureChart(
            wakeChart,
            sleepDataModel.wakeCount.toFloat(),
            sleepDataModel.totalCount.toFloat(),
            ColorUtil.COLOR_WAKE
        )
        configureChart(
            remChart,
            sleepDataModel.remCount.toFloat(),
            sleepDataModel.totalCount.toFloat(),
            ColorUtil.COLOR_REM
        )
        configureChart(
            lightChart,
            sleepDataModel.lightCount.toFloat(),
            sleepDataModel.totalCount.toFloat(),
            ColorUtil.COLOR_LIGHT
        )
        configureChart(
            deepChart,
            sleepDataModel.deepCount.toFloat(),
            sleepDataModel.totalCount.toFloat(),
            ColorUtil.COLOR_DEEP
        )

        wakeInfo.text = getFormattedHour(sleepDataModel.wakeCount)
        remInfo.text = getFormattedHour(sleepDataModel.remCount)
        lightInfo.text = getFormattedHour(sleepDataModel.lightCount)
        deepInfo.text = getFormattedHour(sleepDataModel.deepCount)

        totalTime.text = buildSpannedString {
            bold { append(context.getString(R.string.total_sleep_time)).append(": ") }
            append(getFormattedHour(sleepDataModel.totalCount))
        }

    }

    private fun getPieData(
        first: Float,
        second: Float,
        color: Int,
        chart: PieChart? = null
    ): PieData {
        val entryList = ArrayList<PieEntry>()
        entryList.add(PieEntry(first))
        entryList.add(PieEntry(second - first))
        val dataSet = PieDataSet(entryList, "")
        dataSet.setDrawValues(false)
        val colors = ArrayList<Int>()

        colors.add(color)
        colors.add(lighten(color))

        dataSet.colors = colors
        dataSet.valueFormatter = PercentFormatter(chart)
        return PieData(dataSet)
    }

    private fun lighten(color: Int, fraction: Double = 0.2): Int {
        var red = Color.red(color)
        var green = Color.green(color)
        var blue = Color.blue(color)
        red = lightenColor(red, fraction)
        green = lightenColor(green, fraction)
        blue = lightenColor(blue, fraction)
        val alpha = Color.alpha(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun lightenColor(color: Int, fraction: Double): Int {
        return min(color + color * fraction, 255.0).toInt()
    }

    private fun getFormattedHour(time: Int): String {
        var hours = time / 60
        val minutes = hours % 60
        hours /= 60
        return "${hours}h ${minutes}min"

    }

    private fun configureChart(chart: PieChart, value: Float, total: Float, color: Int) {
        with(chart) {
            data = getPieData(value, total, color, this)
            setUsePercentValues(true)
            setTouchEnabled(false)
            setDrawEntryLabels(false)
            highlightValues(null)
            legend.isEnabled = false
            description.isEnabled = false
            highlightValues(null)
            setDrawCenterText(true)
            centerText = buildSpannedString {
                bold { italic { append(String.format("%.1f", (value / total) * 100) + "%") } }
            }
            setCenterTextColor(ColorUtil.COLOR_CENTER_TEXT)
            setCenterTextSize(12f)
        }
    }

    private fun getAsHour(time: Int): Float {
        var hours = (time / 60)
        val minutes = (hours % 60) / 60.0
        hours /= 60
        val res = hours.toDouble() + minutes

        return (round(res * 100) / 100).toFloat()
    }
}