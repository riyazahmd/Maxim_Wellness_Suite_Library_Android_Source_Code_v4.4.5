package com.maximintegrated.maximsensorsapp.sleep

import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

class HourValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return value.toString() + "h"
    }
}

class DateAxisValueFormatter(private val data: Array<String>?) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return if (data.isNullOrEmpty()) "" else data[min(
            data.size - 1,
            value.roundToInt()
        )]
    }
}

class LineChartValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val cal = Calendar.getInstance()
        cal.time = Date(value.toLong())
        return cal.get(Calendar.HOUR_OF_DAY).toString() + ":" + if (cal.get(Calendar.MINUTE) < 10) ("0" + cal.get(
            Calendar.MINUTE
        )) else cal.get(Calendar.MINUTE)
    }
}

class StagesYAxisFormatter : ValueFormatter() {
    private val map: HashMap<Float, String> =
        hashMapOf(1f to "Awake", 2f to "REM", 3f to "Light", 4f to "Deep", 5f to "")

    override fun getFormattedValue(value: Float): String {
        return map[value].toString()
    }
}