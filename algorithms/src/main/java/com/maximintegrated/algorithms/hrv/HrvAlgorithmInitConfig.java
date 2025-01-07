package com.maximintegrated.algorithms.hrv;

import androidx.annotation.NonNull;

public class HrvAlgorithmInitConfig {
    private float samplingPeriod;
    private short windowSizeInSec;
    private short windowShiftSizeInSec;

    public HrvAlgorithmInitConfig() {

    }

    public HrvAlgorithmInitConfig(float samplingPeriod, short windowSizeInSec, short windowShiftSizeInSec) {
        this.samplingPeriod = samplingPeriod;
        this.windowSizeInSec = windowSizeInSec;
        this.windowShiftSizeInSec = windowShiftSizeInSec;
    }

    public float getSamplingPeriod() {
        return samplingPeriod;
    }

    public void setSamplingPeriod(float samplingPeriod) {
        this.samplingPeriod = samplingPeriod;
    }

    public short getWindowSizeInSec() {
        return windowSizeInSec;
    }

    public void setWindowSizeInSec(short windowSizeInSec) {
        this.windowSizeInSec = windowSizeInSec;
    }

    public short getWindowShiftSizeInSec() {
        return windowShiftSizeInSec;
    }

    public void setWindowShiftSizeInSec(short windowShiftSizeInSec) {
        this.windowShiftSizeInSec = windowShiftSizeInSec;
    }

    @NonNull
    @Override
    public String toString() {
        return "HrvAlgorithmInitConfig{" +
                "samplingPeriod=" + samplingPeriod +
                ", windowSizeInSec=" + windowSizeInSec +
                ", windowShiftSizeInSec=" + windowShiftSizeInSec +
                '}';
    }
}
