package com.maximintegrated.maximsensorsapp

import com.github.mikephil.charting.data.Entry

data class OfflineChartData(
    val dataSetValues: List<Entry>,
    val title: String,
    val label: String
)