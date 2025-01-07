package com.maximintegrated.algorithms.hrv;

import androidx.annotation.NonNull;

public class TimeDomainHrvMetrics {
    private float avnn;
    private float sdnn;
    private float rmssd;
    private float pnn50;

    public TimeDomainHrvMetrics() {
    }

    public TimeDomainHrvMetrics(float avnn, float sdnn, float rmssd, float pnn50) {
        this.avnn = avnn;
        this.sdnn = sdnn;
        this.rmssd = rmssd;
        this.pnn50 = pnn50;
    }

    public float getAvnn() {
        return avnn;
    }

    public void setAvnn(float avnn) {
        this.avnn = avnn;
    }

    public float getSdnn() {
        return sdnn;
    }

    public void setSdnn(float sdnn) {
        this.sdnn = sdnn;
    }

    public float getRmssd() {
        return rmssd;
    }

    public void setRmssd(float rmssd) {
        this.rmssd = rmssd;
    }

    public float getPnn50() {
        return pnn50;
    }

    public void setPnn50(float pnn50) {
        this.pnn50 = pnn50;
    }

    public void update(float avnn,
                       float sdnn,
                       float rmssd,
                       float pnn50) {
        this.avnn = avnn;
        this.sdnn = sdnn;
        this.rmssd = rmssd;
        this.pnn50 = pnn50;
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeDomainHrvMetrics{" +
                "avnn=" + avnn +
                ", sdnn=" + sdnn +
                ", rmssd=" + rmssd +
                ", pnn50=" + pnn50 +
                '}';
    }
}
