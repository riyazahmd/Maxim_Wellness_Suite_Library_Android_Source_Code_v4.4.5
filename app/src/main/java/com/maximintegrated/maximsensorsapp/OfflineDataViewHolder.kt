package com.maximintegrated.maximsensorsapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.offline_data_item.view.*
import java.util.*

class OfflineDataViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.offline_data_item,
        parent,
        false
    )
) {
    private val lineChart: LineChart by lazy { itemView.lineChart }
    private val chartTitle: TextView by lazy { itemView.chartTitle }

    fun bind(data: OfflineChartData) {

        setupChart()

        val dataSet = LineDataSet(emptyList(), "")

        dataSet.setDrawCircles(false)
        dataSet.label = data.title
        dataSet.setDrawFilled(false)
        dataSet.lineWidth = 2f
        dataSet.values = data.dataSetValues
        //dataSet.values = data.dataSetValues.map { Entry(it.second, it.first) }

        chartTitle.text = data.title

        lineChart.data = LineData(dataSet)
    }

    private fun setupChart() {
        lineChart.extraBottomOffset = 10f
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.setDrawGridBackground(false)
        lineChart.isDragEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setScaleEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.setDrawGridLines(false)


        val xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.valueFormatter = IAxisValueFormatter { value, axis -> convert(value) } as ValueFormatter?
        xAxis.textSize = 14f
        lineChart.axisLeft.textSize = 14f
    }

    fun convert(time: Float): String {
        val cal = Calendar.getInstance()
        cal.time = Date(time.toLong())
        return cal.get(Calendar.HOUR_OF_DAY).toString() + ":" + if (cal.get(Calendar.MINUTE) < 10) ("0" + cal.get(
            Calendar.MINUTE
        )) else cal.get(Calendar.MINUTE)
    }
}