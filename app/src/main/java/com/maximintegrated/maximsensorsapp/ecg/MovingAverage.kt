package com.maximintegrated.maximsensorsapp.ecg

class MovingAverage(windowSize: Int) {

    private val window = FloatArray(windowSize)
    private var windowSum = 0f
    private var windowItemCount = 0
    private var windowStartIndex = 0

    val average
        get() = if (windowItemCount != 0) windowSum / windowItemCount else 0f

    fun add(number: Float) {
        if (windowItemCount == window.size) {
            windowSum -= window[windowStartIndex]
        } else {
            windowItemCount++
        }
        windowSum += number
        window[windowStartIndex++] = number
        if (windowStartIndex == windowItemCount) {
            windowStartIndex = 0
        }
    }

    fun reset() {
        windowSum = 0f
        windowItemCount = 0
        windowStartIndex = 0
    }
}
