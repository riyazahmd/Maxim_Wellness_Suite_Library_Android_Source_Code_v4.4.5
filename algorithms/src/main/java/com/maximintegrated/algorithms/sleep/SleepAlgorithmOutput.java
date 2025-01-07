package com.maximintegrated.algorithms.sleep;

public class SleepAlgorithmOutput {

    public enum SleepWakeDecisionStatus {
        NOT_CALCULATED(0),
        CALCULATED(1);

        int value;

        SleepWakeDecisionStatus(int value) {
            this.value = value;
        }
    }

    public enum SleepWakeDecision {
        WAKE(0),
        RESTLESS(1),
        SLEEP(2);

        int value;

        SleepWakeDecision(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum SleepPhaseOutputStatus {
        NOT_CALCULATED(0),
        READY(1);

        int value;

        SleepPhaseOutputStatus(int value) {
            this.value = value;
        }
    }

    public enum SleepPhaseOutput {
        UNDEFINED(-1),
        WAKE(0),
        REM(2),
        LIGHT(3),
        DEEP(4);
        int value;

        SleepPhaseOutput(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private int sleepWakeDecisionStatus;
    private int sleepWakeDecision;
    private int sleepWakeDetentionLatency;
    private float sleepWakeOutputConfLevel;
    private int sleepPhaseOutputStatus;
    private int sleepPhaseOutput;
    private float sleepPhaseOutputConfLevel;
    private int encodedOutput_sleepPhaseOutput;
    private int encodedOutput_duration;
    private boolean encodedOutput_needsStorage;
    private float hr;
    private float accMag;
    private float ibi;
    private float sleepRestingHR;

    public SleepAlgorithmOutput(int sleepWakeDecisionStatus, int sleepWakeDecision, int sleepWakeDetentionLatency, float sleepWakeOutputConfLevel, int sleepPhaseOutputStatus, int sleepPhaseOutput, float sleepPhaseOutputConfLevel, int encodedOutput_sleepPhaseOutput, int encodedOutput_duration, boolean encodedOutput_needsStorage, float hr, float accMag, float ibi, float sleepRestingHR) {
        this.sleepWakeDecisionStatus = sleepWakeDecisionStatus;
        this.sleepWakeDecision = sleepWakeDecision;
        this.sleepWakeDetentionLatency = sleepWakeDetentionLatency;
        this.sleepWakeOutputConfLevel = sleepWakeOutputConfLevel;
        this.sleepPhaseOutputStatus = sleepPhaseOutputStatus;
        this.sleepPhaseOutput = sleepPhaseOutput;
        this.sleepPhaseOutputConfLevel = sleepPhaseOutputConfLevel;
        this.encodedOutput_sleepPhaseOutput = encodedOutput_sleepPhaseOutput;
        this.encodedOutput_duration = encodedOutput_duration;
        this.encodedOutput_needsStorage = encodedOutput_needsStorage;
        this.hr = hr;
        this.accMag = accMag;
        this.ibi = ibi;
        this.sleepRestingHR = sleepRestingHR;
    }

    public SleepAlgorithmOutput() {

    }

    public int getSleepWakeDecisionStatus() {
        return sleepWakeDecisionStatus;
    }

    public void setSleepWakeDecisionStatus(int sleepWakeDecisionStatus) {
        this.sleepWakeDecisionStatus = sleepWakeDecisionStatus;
    }

    public int getSleepWakeDecision() {
        return sleepWakeDecision;
    }

    public void setSleepWakeDecision(int sleepWakeDecision) {
        this.sleepWakeDecision = sleepWakeDecision;
    }

    public int getSleepWakeDetentionLatency() {
        return sleepWakeDetentionLatency;
    }

    public void setSleepWakeDetentionLatency(int sleepWakeDetentionLatency) {
        this.sleepWakeDetentionLatency = sleepWakeDetentionLatency;
    }

    public float getSleepWakeOutputConfLevel() {
        return sleepWakeOutputConfLevel;
    }

    public void setSleepWakeOutputConfLevel(float sleepWakeOutputConfLevel) {
        this.sleepWakeOutputConfLevel = sleepWakeOutputConfLevel;
    }

    public int getSleepPhaseOutputStatus() {
        return sleepPhaseOutputStatus;
    }

    public void setSleepPhaseOutputStatus(int sleepPhaseOutputStatus) {
        this.sleepPhaseOutputStatus = sleepPhaseOutputStatus;
    }

    public int getSleepPhaseOutput() {
        return sleepPhaseOutput;
    }

    public void setSleepPhaseOutput(int sleepPhaseOutput) {
        this.sleepPhaseOutput = sleepPhaseOutput;
    }

    public float getSleepPhaseOutputConfLevel() {
        return sleepPhaseOutputConfLevel;
    }

    public void setSleepPhaseOutputConfLevel(float sleepPhaseOutputConfLevel) {
        this.sleepPhaseOutputConfLevel = sleepPhaseOutputConfLevel;
    }

    public int getEncodedOutput_sleepPhaseOutput() {
        return encodedOutput_sleepPhaseOutput;
    }

    public void setEncodedOutput_sleepPhaseOutput(int encodedOutput_sleepPhaseOutput) {
        this.encodedOutput_sleepPhaseOutput = encodedOutput_sleepPhaseOutput;
    }

    public int getEncodedOutput_duration() {
        return encodedOutput_duration;
    }

    public void setEncodedOutput_duration(int encodedOutput_duration) {
        this.encodedOutput_duration = encodedOutput_duration;
    }

    public boolean isEncodedOutput_needsStorage() {
        return encodedOutput_needsStorage;
    }

    public void setEncodedOutput_needsStorage(boolean encodedOutput_needsStorage) {
        this.encodedOutput_needsStorage = encodedOutput_needsStorage;
    }

    public float getHr() {
        return hr;
    }

    public void setHr(float hr) {
        this.hr = hr;
    }

    public float getAccMag() {
        return accMag;
    }

    public void setAccMag(float accMag) {
        this.accMag = accMag;
    }

    public float getIbi() {
        return ibi;
    }

    public void setIbi(float ibi) {
        this.ibi = ibi;
    }

    public float getSleepRestingHR() {
        return sleepRestingHR;
    }

    public void setSleepRestingHR(float sleepRestingHR) {
        this.sleepRestingHR = sleepRestingHR;
    }

    public void update(int sleepWakeDecisionStatus, int sleepWakeDecision, int sleepWakeDetentionLatency, float sleepWakeOutputConfLevel, int sleepPhaseOutputStatus, int sleepPhaseOutput, float sleepPhaseOutputConfLevel, int encodedOutput_sleepPhaseOutput, int encodedOutput_duration, boolean encodedOutput_needsStorage, float hr, float accMag, float ibi, float sleepRestingHR) {
        this.sleepWakeDecisionStatus = sleepWakeDecisionStatus;
        this.sleepWakeDecision = sleepWakeDecision;
        this.sleepWakeDetentionLatency = sleepWakeDetentionLatency;
        this.sleepWakeOutputConfLevel = sleepWakeOutputConfLevel;
        this.sleepPhaseOutputStatus = sleepPhaseOutputStatus;
        this.sleepPhaseOutput = sleepPhaseOutput;
        this.sleepPhaseOutputConfLevel = sleepPhaseOutputConfLevel;
        this.encodedOutput_sleepPhaseOutput = encodedOutput_sleepPhaseOutput;
        this.encodedOutput_duration = encodedOutput_duration;
        this.encodedOutput_needsStorage = encodedOutput_needsStorage;
        this.hr = hr;
        this.accMag = accMag;
        this.ibi = ibi;
        this.sleepRestingHR = sleepRestingHR;
    }

    public SleepAlgorithmEncodedOutput getEncodedOutput(){
        return new SleepAlgorithmEncodedOutput(encodedOutput_sleepPhaseOutput, encodedOutput_duration, encodedOutput_needsStorage);
    }
}
