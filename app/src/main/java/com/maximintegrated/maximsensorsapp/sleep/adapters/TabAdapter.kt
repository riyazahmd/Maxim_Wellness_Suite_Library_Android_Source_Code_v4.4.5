package com.maximintegrated.maximsensorsapp.sleep.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.sleep.database.entity.SourceAndAllSleeps
import com.maximintegrated.maximsensorsapp.sleep.view.SleepTabView

class TabAdapter(context: Context, private val sourceAndAllSleeps: SourceAndAllSleeps) :
    PagerAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return 4
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Stages"
            1 -> "Motion"
            2 -> "HR"
            3 -> "IBI"
            4 -> "SpO2"
            else -> {
                "unknown"
            }
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = layoutInflater.inflate(R.layout.view_sleep_tab, container, false)

        val sleepTabView = itemView.findViewById<SleepTabView>(R.id.chartTabView)
        val sleepList = sourceAndAllSleeps.sleepList

        sleepTabView.setupChart(position, sleepList, container.height)
        container.addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}