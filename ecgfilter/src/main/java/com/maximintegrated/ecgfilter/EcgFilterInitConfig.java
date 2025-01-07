package com.maximintegrated.ecgfilter;

public class EcgFilterInitConfig {
    private final int samplingFrequency;
    private final float algorithmOutputGain;
    private final boolean isCicFilterCompensationOn;
    private final boolean isInAdcCount;

    public EcgFilterInitConfig(int samplingFrequency, float algorithmOutputGain,
                               boolean isCicFilterCompensationOn, boolean isInAdcCount) {
        this.samplingFrequency = samplingFrequency;
        this.algorithmOutputGain = algorithmOutputGain;
        this.isCicFilterCompensationOn = isCicFilterCompensationOn;
        this.isInAdcCount = isInAdcCount;
    }

    public int getSamplingFrequency() {
        return samplingFrequency;
    }

    public float getAlgorithmOutputGain() {
        return algorithmOutputGain;
    }

    public boolean isCicFilterCompensationOn() {
        return isCicFilterCompensationOn;
    }

    public boolean isInAdcCount() {
        return isInAdcCount;
    }

    @Override
    public String toString() {
        return "EcgFilterInitConfig{" +
                "samplingFrequency=" + samplingFrequency +
                ", algorithmOutputGain=" + algorithmOutputGain +
                ", isCicFilterCompensationOn=" + isCicFilterCompensationOn +
                ", isInAdcCount=" + isInAdcCount +
                '}';
    }
}
