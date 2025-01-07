package com.maximintegrated.maximsensorsapp.sleep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.maximintegrated.maximsensorsapp.DeviceSettings
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.exts.addFragment
import com.maximintegrated.maximsensorsapp.sleep.adapters.PieChartAdapter
import com.maximintegrated.maximsensorsapp.sleep.utils.ColorUtil
import com.maximintegrated.maximsensorsapp.sleep.viewmodels.SourceAndAllSleepsViewModel
import com.maximintegrated.maximsensorsapp.sleep.viewmodels.SourceViewModel
import kotlinx.android.synthetic.main.include_sleep_fragment_content.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round

private enum class DataState {
    NONE,
    EMPTY,
    AVAILABLE
}

class SleepFragment : Fragment(),
    PieChartAdapter.OnItemClickListener {
    companion object {
        fun newInstance() = SleepFragment()
    }

    private lateinit var sourceAndAllSleepsViewModel: SourceAndAllSleepsViewModel
    private lateinit var sourceViewModel: SourceViewModel
    private val pieChartAdapter = PieChartAdapter()

    private var dataState = DataState.NONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sleep, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sourceViewModel = ViewModelProviders.of(requireActivity()).get(SourceViewModel::class.java)

        sourceViewModel.setUserId(DeviceSettings.selectedUserId)

        sourceViewModel.busy.observe(this, Observer {
            if (it) {
                sleepProgressBar.isVisible = true
                dataAvailableGroup.isVisible = false
                dataNotAvailableGroup.isVisible = false
            } else {
                sleepProgressBar.isVisible = dataState == DataState.NONE
                dataAvailableGroup.isVisible = dataState == DataState.AVAILABLE
                dataNotAvailableGroup.isVisible = dataState == DataState.EMPTY
            }
        })

        sourceViewModel.getSleepData()

        sourceAndAllSleepsViewModel =
            ViewModelProviders.of(requireActivity()).get(SourceAndAllSleepsViewModel::class.java)

        sourceAndAllSleepsViewModel.sourceWithSleepsList.observe(this, Observer { t ->

            if (t != null) {
                val result: ArrayList<SleepDataModel> = arrayListOf()
                for (data in t) {
                    val sleepDataModel = SleepDataModel.parseSleepList(data)
                    if ((sleepDataModel != null) and (sleepDataModel?.userId.equals(DeviceSettings.selectedUserId))) {
                        result.add(sleepDataModel!!)
                    }
                }

                val temp = result.sortedBy { it.date }.reversed().take(7)
                val barChartEntryList = temp.map {
                    BarEntry(
                        temp.indexOf(it).toFloat(),
                        getFormattedHour(it.totalCount.toFloat())
                    )
                }
                val barSet = BarDataSet(barChartEntryList, "")
                barSet.color = ColorUtil.COLOR_REM

                val dataSets = ArrayList<IBarDataSet>()
                dataSets.add(barSet)
                val barData = BarData(barSet)
                barData.setDrawValues(false)
                weeklyChart.data = barData
                barData.barWidth = 0.9f
                val xAxis = weeklyChart.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.labelCount = 7

                xAxis.setDrawLabels(true)
                val sdf = SimpleDateFormat("dd MMM", Locale.US)
                val list =
                    result.sortedBy { it.date }.reversed().take(7).map { sdf.format(it.date) }
                        .toTypedArray()
                xAxis.valueFormatter = DateAxisValueFormatter(list)

                weeklyChart.axisRight.isEnabled = false
                weeklyChart.legend.isEnabled = false
                weeklyChart.description.isEnabled = false
                val axisLeft = weeklyChart.axisLeft
                axisLeft.valueFormatter = HourValueFormatter()
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = 15f
                axisLeft.labelCount = 3
                axisLeft.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
                axisLeft.yOffset = -5f

                weeklyChart.notifyDataSetChanged()
                weeklyChart.invalidate()
                val ascOrder = result.sortedBy { it.date }.reversed()

                pieChartAdapter.dataSet = ascOrder
                dataState = if (ascOrder.isNotEmpty()) {
                    DataState.AVAILABLE
                } else {
                    DataState.EMPTY
                }
                if (sourceViewModel.busy.value == false) {
                    sleepProgressBar.isVisible = false
                    dataAvailableGroup.isVisible = dataState == DataState.AVAILABLE
                    dataNotAvailableGroup.isVisible = dataState == DataState.EMPTY
                }
            }
        })

        pieChartAdapter.setOnItemClickListener(this)
        rvSleepData.adapter = pieChartAdapter
    }

    override fun onItemClick(model: SleepDataModel) {
        requireActivity().addFragment(SleepDetailsFragment.newInstance(model))
    }

    private fun getFormattedHour(time: Float): Float {
        var hours = (time / 60).toInt()
        val minutes = (hours % 60) / 60.0
        hours /= 60
        val res = hours.toDouble() + minutes
        return (round(res * 100) / 100).toFloat()
    }
}