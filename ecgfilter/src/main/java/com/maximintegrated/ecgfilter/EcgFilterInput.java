package com.maximintegrated.ecgfilter;

public class EcgFilterInput {
    private float ecgRaw;
    private int notchFrequency;
    private int cutoffFrequency;
    private boolean isAdaptiveFilterOn;
    private boolean isBaselineRemovalOn;

    public EcgFilterInput() {
    }

    public EcgFilterInput(float ecgRaw, int notchFrequency, int cutoffFrequency,
                          boolean isAdaptiveFilterOn, boolean isBaselineRemovalOn) {
        this.ecgRaw = ecgRaw;
        this.notchFrequency = notchFrequency;
        this.cutoffFrequency = cutoffFrequency;
        this.isAdaptiveFilterOn = isAdaptiveFilterOn;
        this.isBaselineRemovalOn = isBaselineRemovalOn;
    }

    public float getEcgRaw() {
        return ecgRaw;
    }

    public void setEcgRaw(float ecgRaw) {
        this.ecgRaw = ecgRaw;
    }

    public int getNotchFrequency() {
        return notchFrequency;
    }

    public void setNotchFrequency(int notchFrequency) {
        this.notchFrequency = notchFrequency;
    }

    public int getCutoffFrequency() {
        return cutoffFrequency;
    }

    public void setCutoffFrequency(int cutoffFrequency) {
        this.cutoffFrequency = cutoffFrequency;
    }

    public boolean isAdaptiveFilterOn() {
        return isAdaptiveFilterOn;
    }

    public void setAdaptiveFilterOn(boolean adaptiveFilterOn) {
        isAdaptiveFilterOn = adaptiveFilterOn;
    }

    public boolean isBaselineRemovalOn() {
        return isBaselineRemovalOn;
    }

    public void setBaselineRemovalOn(boolean baselineRemovalOn) {
        isBaselineRemovalOn = baselineRemovalOn;
    }

    @Override
    public String toString() {
        return "EcgFilterInput{" +
                "ecgRaw=" + ecgRaw +
                ", notchFrequency=" + notchFrequency +
                ", cutoffFrequency=" + cutoffFrequency +
                ", isAdaptiveFilterOn=" + isAdaptiveFilterOn +
                ", isBaselineRemovalOn=" + isBaselineRemovalOn +
                '}';
    }
}
