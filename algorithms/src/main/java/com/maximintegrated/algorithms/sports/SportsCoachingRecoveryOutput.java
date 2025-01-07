package com.maximintegrated.algorithms.sports;

public class SportsCoachingRecoveryOutput {
    public static final int SIZE = 5;
    private int recoveryTimeMin = 0;
    private float epoc = 0;
    private int hr0 = 0;
    private int lastHr = 0;
    private int recoveryPercentage = 0;

    public SportsCoachingRecoveryOutput() {

    }

    public SportsCoachingRecoveryOutput(int recoveryTimeMin, float epoc, int hr0, int lastHr, int recoveryPercentage) {
        this.recoveryTimeMin = recoveryTimeMin;
        this.epoc = epoc;
        this.hr0 = hr0;
        this.lastHr = lastHr;
        this.recoveryPercentage = recoveryPercentage;
    }

    public int getRecoveryTimeMin() {
        return recoveryTimeMin;
    }

    public void setRecoveryTimeMin(int recoveryTimeMin) {
        this.recoveryTimeMin = recoveryTimeMin;
    }

    public float getEpoc() {
        return epoc;
    }

    public void setEpoc(float epoc) {
        this.epoc = epoc;
    }

    public int getHr0() {
        return hr0;
    }

    public void setHr0(int hr0) {
        this.hr0 = hr0;
    }

    public int getLastHr() {
        return lastHr;
    }

    public void setLastHr(int lastHr) {
        this.lastHr = lastHr;
    }

    public int getRecoveryPercentage() {
        return recoveryPercentage;
    }

    public void setRecoveryPercentage(int recoveryPercentage) {
        this.recoveryPercentage = recoveryPercentage;
    }

    public void update(int recoveryTimeMin, float epoc, int hr0, int lastHr, int recoveryPercentage) {
        this.recoveryTimeMin = recoveryTimeMin;
        this.epoc = epoc;
        this.hr0 = hr0;
        this.lastHr = lastHr;
        this.recoveryPercentage = recoveryPercentage;
    }
}
