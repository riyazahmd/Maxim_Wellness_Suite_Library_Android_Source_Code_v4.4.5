package com.maximintegrated.maximsensorsapp

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class OfflineDataAdapter : RecyclerView.Adapter<OfflineDataViewHolder>() {
    var dataSetList: List<OfflineChartData> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineDataViewHolder {
        return OfflineDataViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return dataSetList.size
    }

    override fun onBindViewHolder(holder: OfflineDataViewHolder, position: Int) {
        holder.bind(dataSetList[position])
    }
}