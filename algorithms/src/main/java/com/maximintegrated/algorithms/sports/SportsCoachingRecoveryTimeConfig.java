package com.maximintegrated.algorithms.sports;

public class SportsCoachingRecoveryTimeConfig {
    private long lastEpocRecoveryTimestamp = 0;
    private int lastRecoveryEstimateInMinutes = 0;
    private int lastHr = 0;

    public SportsCoachingRecoveryTimeConfig() {

    }

    public SportsCoachingRecoveryTimeConfig(long lastEpocRecoveryTimestamp, int lastRecoveryEstimateInMinutes, int lastHr) {
        this.lastEpocRecoveryTimestamp = lastEpocRecoveryTimestamp;
        this.lastRecoveryEstimateInMinutes = lastRecoveryEstimateInMinutes;
        this.lastHr = lastHr;
    }

    public long getLastEpocRecoveryTimestamp() {
        return lastEpocRecoveryTimestamp;
    }

    public void setLastEpocRecoveryTimestamp(long lastEpocRecoveryTimestamp) {
        this.lastEpocRecoveryTimestamp = lastEpocRecoveryTimestamp;
    }

    public int getLastRecoveryEstimateInMinutes() {
        return lastRecoveryEstimateInMinutes;
    }

    public void setLastRecoveryEstimateInMinutes(int lastRecoveryEstimateInMinutes) {
        this.lastRecoveryEstimateInMinutes = lastRecoveryEstimateInMinutes;
    }

    public int getLastHr() {
        return lastHr;
    }

    public void setLastHr(int lastHr) {
        this.lastHr = lastHr;
    }
}
