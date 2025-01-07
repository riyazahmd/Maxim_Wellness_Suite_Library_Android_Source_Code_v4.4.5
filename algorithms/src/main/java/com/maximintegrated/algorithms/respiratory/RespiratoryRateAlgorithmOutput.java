package com.maximintegrated.algorithms.respiratory;

import androidx.annotation.NonNull;

public class RespiratoryRateAlgorithmOutput {
    private float respirationRate;
    private float confidenceLevel;
    private boolean motionFlag;
    private boolean ibiLowQualityFlag;
    private boolean ppgLowQualityFlag;

    public RespiratoryRateAlgorithmOutput() {
    }

    public RespiratoryRateAlgorithmOutput(float respirationRate, float confidenceLevel, boolean motionFlag, boolean ibiLowQualityFlag, boolean ppgLowQualityFlag) {
        this.respirationRate = respirationRate;
        this.confidenceLevel = confidenceLevel;
        this.motionFlag = motionFlag;
        this.ibiLowQualityFlag = ibiLowQualityFlag;
        this.ppgLowQualityFlag = ppgLowQualityFlag;
    }

    public float getRespirationRate() {
        return respirationRate;
    }

    public void setRespirationRate(float respirationRate) {
        this.respirationRate = respirationRate;
    }

    public float getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(float confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public boolean isMotionFlag() {
        return motionFlag;
    }

    public void setMotionFlag(boolean motionFlag) {
        this.motionFlag = motionFlag;
    }

    public boolean isIbiLowQualityFlag() {
        return ibiLowQualityFlag;
    }

    public void setIbiLowQualityFlag(boolean ibiLowQualityFlag) {
        this.ibiLowQualityFlag = ibiLowQualityFlag;
    }

    public boolean isPpgLowQualityFlag() {
        return ppgLowQualityFlag;
    }

    public void setPpgLowQualityFlag(boolean ppgLowQualityFlag) {
        this.ppgLowQualityFlag = ppgLowQualityFlag;
    }

    public void update(float respirationRate, float confidenceLevel, boolean motionFlag, boolean ibiLowQualityFlag, boolean ppgLowQualityFlag) {
        this.respirationRate = respirationRate;
        this.confidenceLevel = confidenceLevel;
        this.motionFlag = motionFlag;
        this.ibiLowQualityFlag = ibiLowQualityFlag;
        this.ppgLowQualityFlag = ppgLowQualityFlag;
    }

    @NonNull
    @Override
    public String toString() {
        return "RespiratoryRateAlgorithmOutput{" +
                "respirationRate=" + respirationRate +
                ", confidenceLevel=" + confidenceLevel +
                ", motionFlag=" + motionFlag +
                ", ibiLowQualityFlag=" + ibiLowQualityFlag +
                ", ppgLowQualityFlag=" + ppgLowQualityFlag +
                '}';
    }
}
