package com.maximintegrated.ecgfilter;

public class EcgFilterOutput {
    private float ecgProcessed;
    private float hr;
    private float hrv;
    private float baseline;
    private boolean isEcgPulse;

    public EcgFilterOutput() {
    }

    public EcgFilterOutput(float ecgProcessed, float hr, float hrv,
                           float baseline, boolean isEcgPulse) {
        this.ecgProcessed = ecgProcessed;
        this.hr = hr;
        this.hrv = hrv;
        this.baseline = baseline;
        this.isEcgPulse = isEcgPulse;
    }

    private void update(float ecgProcessed, float hr, float hrv,
                        float baseline, boolean isEcgPulse) {
        this.ecgProcessed = ecgProcessed;
        this.hr = hr;
        this.hrv = hrv;
        this.baseline = baseline;
        this.isEcgPulse = isEcgPulse;
    }

    public float getEcgProcessed() {
        return ecgProcessed;
    }

    public void setEcgProcessed(float ecgProcessed) {
        this.ecgProcessed = ecgProcessed;
    }

    public float getHr() {
        return hr;
    }

    public void setHr(float hr) {
        this.hr = hr;
    }

    public float getHrv() {
        return hrv;
    }

    public void setHrv(float hrv) {
        this.hrv = hrv;
    }

    public float getBaseline() {
        return baseline;
    }

    public void setBaseline(float baseline) {
        this.baseline = baseline;
    }

    public boolean isEcgPulse() {
        return isEcgPulse;
    }

    public void setEcgPulse(boolean ecgPulse) {
        isEcgPulse = ecgPulse;
    }

    @Override
    public String toString() {
        return "EcgFilterOutput{" +
                "ecgProcessed=" + ecgProcessed +
                ", hr=" + hr +
                ", hrv=" + hrv +
                ", baseline=" + baseline +
                ", isEcgPulse=" + isEcgPulse +
                '}';
    }
}
