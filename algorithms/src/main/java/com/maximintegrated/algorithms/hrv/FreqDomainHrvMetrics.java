package com.maximintegrated.algorithms.hrv;

import androidx.annotation.NonNull;

public class FreqDomainHrvMetrics {
    private float ulf;
    private float vlf;
    private float lf;
    private float hf;
    private float lfOverHf;
    private float totPwr;

    public FreqDomainHrvMetrics() {
    }

    public FreqDomainHrvMetrics(float ulf, float vlf, float lf, float hf, float lfOverHf, float totPwr) {
        this.ulf = ulf;
        this.vlf = vlf;
        this.lf = lf;
        this.hf = hf;
        this.lfOverHf = lfOverHf;
        this.totPwr = totPwr;
    }

    public float getUlf() {
        return ulf;
    }

    public void setUlf(float ulf) {
        this.ulf = ulf;
    }

    public float getVlf() {
        return vlf;
    }

    public void setVlf(float vlf) {
        this.vlf = vlf;
    }

    public float getLf() {
        return lf;
    }

    public void setLf(float lf) {
        this.lf = lf;
    }

    public float getHf() {
        return hf;
    }

    public void setHf(float hf) {
        this.hf = hf;
    }

    public float getLfOverHf() {
        return lfOverHf;
    }

    public void setLfOverHf(float lfOverHf) {
        this.lfOverHf = lfOverHf;
    }

    public float getTotPwr() {
        return totPwr;
    }

    public void setTotPwr(float totPwr) {
        this.totPwr = totPwr;
    }

    public void update(float ulf,
                       float vlf,
                       float lf,
                       float hf,
                       float lfOverHf,
                       float totPwr) {
        this.ulf = ulf;
        this.vlf = vlf;
        this.lf = lf;
        this.hf = hf;
        this.lfOverHf = lfOverHf;
        this.totPwr = totPwr;
    }


    @NonNull
    @Override
    public String toString() {
        return "FreqDomainHrvMetrics{" +
                "ulf=" + ulf +
                ", vlf=" + vlf +
                ", lf=" + lf +
                ", hf=" + hf +
                ", lfOverHf=" + lfOverHf +
                ", totPwr=" + totPwr +
                '}';
    }
}
