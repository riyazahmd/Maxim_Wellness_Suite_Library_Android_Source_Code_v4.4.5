package com.maximintegrated.algorithms.sleep;

public class SleepAlgorithmOutputFrame {

    private SleepAlgorithmOutput output;
    private int outputDataArrayLength;
    private long dateInfo;

    public SleepAlgorithmOutputFrame() {
        output = new SleepAlgorithmOutput();
        outputDataArrayLength = 0;
        dateInfo = 0;
    }

    public SleepAlgorithmOutputFrame(SleepAlgorithmOutput output, int outputDataArrayLength, long dateInfo) {
        this.output = output;
        this.outputDataArrayLength = outputDataArrayLength;
        this.dateInfo = dateInfo;
    }

    public SleepAlgorithmOutput getOutput() {
        return output;
    }

    public void setOutput(SleepAlgorithmOutput output) {
        this.output = output;
    }

    public int getOutputDataArrayLength() {
        return outputDataArrayLength;
    }

    public void setOutputDataArrayLength(int outputDataArrayLength) {
        this.outputDataArrayLength = outputDataArrayLength;
    }

    public long getDateInfo() {
        return dateInfo;
    }

    public void setDateInfo(long dateInfo) {
        this.dateInfo = dateInfo;
    }

    public void update(int sleepWakeDecisionStatus, int sleepWakeDecision, int sleepWakeDetentionLatency, float sleepWakeOutputConfLevel, int sleepPhaseOutputStatus, int sleepPhaseOutput, float sleepPhaseOutputConfLevel, int encodedOutput_sleepPhaseOutput, int encodedOutput_duration, boolean encodedOutput_needsStorage, float hr, float accMag, float ibi, float sleepRestingHR, int arrayLength, long dateInfo) {
        output.update(sleepWakeDecisionStatus, sleepWakeDecision, sleepWakeDetentionLatency, sleepWakeOutputConfLevel, sleepPhaseOutputStatus, sleepPhaseOutput, sleepPhaseOutputConfLevel, encodedOutput_sleepPhaseOutput, encodedOutput_duration, encodedOutput_needsStorage, hr, accMag, ibi, sleepRestingHR);
        outputDataArrayLength = arrayLength;
        this.dateInfo = dateInfo;
    }
}
