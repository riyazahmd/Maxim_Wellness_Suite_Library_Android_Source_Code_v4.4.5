package com.maximintegrated.maximsensorsapp

data class HrvOfflineChartData(
    val avnn: Float,
    val sdnn: Float,
    val rmssd: Float,
    val pnn50: Float,
    val ulf: Float,
    val vlf: Float,
    val lf: Float,
    val hf: Float,
    val lfOverHf: Float,
    val totPwr: Float
)