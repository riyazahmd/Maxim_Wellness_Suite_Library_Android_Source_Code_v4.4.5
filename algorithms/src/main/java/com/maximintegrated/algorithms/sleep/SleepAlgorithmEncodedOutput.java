package com.maximintegrated.algorithms.sleep;

public class SleepAlgorithmEncodedOutput {

    private int sleepPhaseOutput;
    private int duration;
    private boolean needsStorage;

    public SleepAlgorithmEncodedOutput(int sleepPhaseOutput, int duration, boolean needsStorage) {
        this.sleepPhaseOutput = sleepPhaseOutput;
        this.duration = duration;
        this.needsStorage = needsStorage;
    }

    public SleepAlgorithmEncodedOutput() {

    }

    public int getSleepPhaseOutput() {
        return sleepPhaseOutput;
    }

    public void setSleepPhaseOutput(int sleepPhaseOutput) {
        this.sleepPhaseOutput = sleepPhaseOutput;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isNeedsStorage() {
        return needsStorage;
    }

    public void setNeedsStorage(boolean needsStorage) {
        this.needsStorage = needsStorage;
    }

    public void update(int sleepPhaseOutput, int duration, boolean needsStorage) {
        this.sleepPhaseOutput = sleepPhaseOutput;
        this.duration = duration;
        this.needsStorage = needsStorage;
    }
}
