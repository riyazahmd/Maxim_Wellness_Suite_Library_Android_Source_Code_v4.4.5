package com.maximintegrated.maximsensorsapp

class MovingAverage(val size: Int) {
    private var sum = 0f
    private var index = 0
    private val samples = FloatArray(size)
    private var count = 0

    fun add(value: Float){
        sum -= samples[index]
        samples[index] = value
        sum += value
        if(++index == size){
            index = 0
        }
        if(count < size){
            count++
        }
    }

    fun add(value: Int){
        add(value.toFloat())
    }

    fun average(): Float {
        var avg = 0f
        if(count != 0){
            avg = sum / count
        }
        return avg
    }
}