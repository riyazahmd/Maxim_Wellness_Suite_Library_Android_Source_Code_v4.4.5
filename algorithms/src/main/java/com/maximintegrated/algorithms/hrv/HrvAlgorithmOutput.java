package com.maximintegrated.algorithms.hrv;

import androidx.annotation.NonNull;

public class HrvAlgorithmOutput {
    private TimeDomainHrvMetrics timeDomainHrvMetrics;
    private FreqDomainHrvMetrics freqDomainHrvMetrics;
    private int percentCompleted;
    private boolean isHrvCalculated;

    public HrvAlgorithmOutput() {
        timeDomainHrvMetrics = new TimeDomainHrvMetrics();
        freqDomainHrvMetrics = new FreqDomainHrvMetrics();
    }

    public HrvAlgorithmOutput(TimeDomainHrvMetrics timeDomainHrvMetrics, FreqDomainHrvMetrics freqDomainHrvMetrics, int percentCompleted, boolean isHrvCalculated) {
        this.timeDomainHrvMetrics = timeDomainHrvMetrics;
        this.freqDomainHrvMetrics = freqDomainHrvMetrics;
        this.percentCompleted = percentCompleted;
        this.isHrvCalculated = isHrvCalculated;
    }

    public TimeDomainHrvMetrics getTimeDomainHrvMetrics() {
        return timeDomainHrvMetrics;
    }

    public void setTimeDomainHrvMetrics(TimeDomainHrvMetrics timeDomainHrvMetrics) {
        this.timeDomainHrvMetrics = timeDomainHrvMetrics;
    }

    public FreqDomainHrvMetrics getFreqDomainHrvMetrics() {
        return freqDomainHrvMetrics;
    }

    public void setFreqDomainHrvMetrics(FreqDomainHrvMetrics freqDomainHrvMetrics) {
        this.freqDomainHrvMetrics = freqDomainHrvMetrics;
    }

    public int getPercentCompleted() {
        return percentCompleted;
    }

    public void setPercentCompleted(int percentCompleted) {
        this.percentCompleted = percentCompleted;
    }

    public boolean isHrvCalculated() {
        return isHrvCalculated;
    }

    public void setHrvCalculated(boolean hrvCalculated) {
        isHrvCalculated = hrvCalculated;
    }

    public void update(float avnn,
                       float sdnn,
                       float rmssd,
                       float pnn50,

                       float ulf,
                       float vlf,
                       float lf,
                       float hf,
                       float lfOverHf,
                       float totPwr, int percentCompleted, boolean isHrvCalculated) {
        freqDomainHrvMetrics.update(ulf, vlf, lf, hf, lfOverHf, totPwr);
        timeDomainHrvMetrics.update(avnn, sdnn, rmssd, pnn50);
        this.percentCompleted = percentCompleted;
        this.isHrvCalculated = isHrvCalculated;
    }

    @NonNull
    @Override
    public String toString() {
        return "HrvAlgorithmInput{" +
                "timeDomainHrvMetrics=" + timeDomainHrvMetrics.toString() +
                ", freqDomainHrvMetrics=" + freqDomainHrvMetrics.toString() +
                ", percentCompleted=" + percentCompleted +
                ", isHrvCalculated=" + isHrvCalculated +
                '}';
    }
}
