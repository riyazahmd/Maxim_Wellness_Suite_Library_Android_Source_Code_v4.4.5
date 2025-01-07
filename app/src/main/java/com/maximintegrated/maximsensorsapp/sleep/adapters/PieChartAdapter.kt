package com.maximintegrated.maximsensorsapp.sleep.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.sleep.SleepDataModel
import com.maximintegrated.maximsensorsapp.sleep.view.PieChartView

class PieChartAdapter : RecyclerView.Adapter<PieChartAdapter.ViewHolder>() {
    var dataSet: List<SleepDataModel> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private lateinit var clickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(model: SleepDataModel)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pie: PieChartView = itemView.findViewById(R.id.pie)
        private val detailsButton: Button = itemView.findViewById(R.id.detailsButton)

        init {
            detailsButton.setOnClickListener {
                clickListener.onItemClick(dataSet[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context: Context = parent.context
        val inflater: LayoutInflater = LayoutInflater.from(context)

        val chartView: View = inflater.inflate(R.layout.sleep_daily_item, parent, false)

        return ViewHolder(chartView)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pie = dataSet.get(position)
        val chart: PieChartView = holder.pie
        chart.setupChart(pie)

    }
}