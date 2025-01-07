package com.maximintegrated.maximsensorsapp

import com.maximintegrated.hsp.HspStreamData

class DataGenerator {
    private var sampleCount = 0
    private var readIndex = 0
    private var writeIndex = 0
    private var buffer = Array<HspStreamData?>(BUFFER_SIZE) { null }
    private var sampleIndex = 0

    companion object {
        private const val BUFFER_SIZE = 1000
    }

    private fun getQueueCount(): Int {
        return if (writeIndex - readIndex >= 0) {
            writeIndex - readIndex
        } else {
            BUFFER_SIZE - (readIndex - writeIndex)
        }
    }

    fun generateData(packet: HspStreamData): ArrayList<HspStreamData> {
        val list: ArrayList<HspStreamData> = arrayListOf()
        sampleCount++
        buffer[writeIndex] = packet
        val repeated = buffer[writeIndex]
        val nTimesRepeatPrevSample = packet.ibiOffset
        if(sampleCount != 0) {
            for(i in 1 until nTimesRepeatPrevSample) {
                val copy = repeated?.copy()
                copy?.ibiOffset = 0
                copy?.rr = 0f
                copy?.rrConfidence = 0
                copy?.sampleCount = sampleIndex++
                buffer[writeIndex++] = copy
                if(writeIndex == BUFFER_SIZE) {
                    writeIndex = 0
                }
            }
        }
        repeated?.sampleCount = sampleIndex++
        buffer[writeIndex++] = repeated
        if(writeIndex == BUFFER_SIZE) {
            writeIndex = 0
        }
        val numberOfSamplesInBuffer = getQueueCount()
        val numberOf25HzBlocksBeReported = numberOfSamplesInBuffer / 25
        var j = numberOf25HzBlocksBeReported
        while (j > 0) {
            for(i in 1..25) {
                val data = buffer[readIndex++]
                data?.let {
                    list.add(it)
                }
                if(readIndex == BUFFER_SIZE) {
                    readIndex = 0
                }
            }
            j--
        }
        return list
    }

    fun reset() {
        sampleCount = 0
        readIndex = 0
        writeIndex = 0
        sampleIndex = 0
    }
}