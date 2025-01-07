package com.maximintegrated.maximsensorsapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OfflineDataModel(
    val hr: Float,
    val spo2: Float,
    var hrv: Float = 0f,
    val steps: Float,
    var respiration: Float = 0f,
    val motion: Float,
    val rr: Float,
    var stress: Float = 0f,
    var sleepData: Float = 0f,
    val date: Float,
    val green: Float,
    val red: Float,
    val ir: Float,
    val rrConfidence: Int
) : Parcelable