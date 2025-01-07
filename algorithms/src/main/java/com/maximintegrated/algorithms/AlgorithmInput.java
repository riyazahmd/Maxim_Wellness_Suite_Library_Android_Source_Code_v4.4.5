package com.maximintegrated.algorithms;

public class AlgorithmInput {
    private int sampleCount;
    private int sampleTime;
    private int green;
    private int green2;
    private int ir;
    private int red;
    private int accelerationX;
    private int accelerationY;
    private int accelerationZ;
    private int operationMode;
    private int hr;
    private int hrConfidence;
    private int rr;
    private int rrConfidence;
    private int activity;
    private int r;
    private int wspo2Confidence;
    private int spo2;
    private int wspo2PercentageComplete;
    private int wspo2LowSnr;
    private int wspo2Motion;
    private int wspo2LowPi;
    private int wspo2UnreliableR;
    private int wspo2State;
    private int scdState;
    private int walkSteps;
    private int runSteps;
    private int kCal;
    private int totalActEnergy;
    private int ibiOffset;
    private long timestamp;

    public AlgorithmInput() {

    }

    public AlgorithmInput(int sampleCount, int sampleTime, int green, int green2, int ir, int red, int accelerationX, int accelerationY, int accelerationZ, int operationMode, int hr, int hrConfidence, int rr, int rrConfidence, int activity, int r, int wspo2Confidence, int spo2, int wspo2PercentageComplete, int wspo2LowSnr, int wspo2Motion, int wspo2LowPi, int wspo2UnreliableR, int wspo2State, int scdState, int walkSteps, int runSteps, int kCal, int totalActEnergy, int ibiOffset, long timestamp) {
        this.sampleCount = sampleCount;
        this.sampleTime = sampleTime;
        this.green = green;
        this.green2 = green2;
        this.ir = ir;
        this.red = red;
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.accelerationZ = accelerationZ;
        this.operationMode = operationMode;
        this.hr = hr;
        this.hrConfidence = hrConfidence;
        this.rr = rr;
        this.rrConfidence = rrConfidence;
        this.activity = activity;
        this.r = r;
        this.wspo2Confidence = wspo2Confidence;
        this.spo2 = spo2;
        this.wspo2PercentageComplete = wspo2PercentageComplete;
        this.wspo2LowSnr = wspo2LowSnr;
        this.wspo2Motion = wspo2Motion;
        this.wspo2LowPi = wspo2LowPi;
        this.wspo2UnreliableR = wspo2UnreliableR;
        this.wspo2State = wspo2State;
        this.scdState = scdState;
        this.walkSteps = walkSteps;
        this.runSteps = runSteps;
        this.kCal = kCal;
        this.totalActEnergy = totalActEnergy;
        this.ibiOffset = ibiOffset;
        this.timestamp = timestamp;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    public int getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(int sampleTime) {
        this.sampleTime = sampleTime;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getGreen2() {
        return green2;
    }

    public void setGreen2(int green2) {
        this.green2 = green2;
    }

    public int getIr() {
        return ir;
    }

    public void setIr(int ir) {
        this.ir = ir;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getAccelerationX() {
        return accelerationX;
    }

    public void setAccelerationX(int accelerationX) {
        this.accelerationX = accelerationX;
    }

    public int getAccelerationY() {
        return accelerationY;
    }

    public void setAccelerationY(int accelerationY) {
        this.accelerationY = accelerationY;
    }

    public int getAccelerationZ() {
        return accelerationZ;
    }

    public void setAccelerationZ(int accelerationZ) {
        this.accelerationZ = accelerationZ;
    }

    public int getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(int operationMode) {
        this.operationMode = operationMode;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getHrConfidence() {
        return hrConfidence;
    }

    public void setHrConfidence(int hrConfidence) {
        this.hrConfidence = hrConfidence;
    }

    public int getRr() {
        return rr;
    }

    public void setRr(int rr) {
        this.rr = rr;
    }

    public int getRrConfidence() {
        return rrConfidence;
    }

    public void setRrConfidence(int rrConfidence) {
        this.rrConfidence = rrConfidence;
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getWspo2Confidence() {
        return wspo2Confidence;
    }

    public void setWspo2Confidence(int wspo2Confidence) {
        this.wspo2Confidence = wspo2Confidence;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public int getWspo2PercentageComplete() {
        return wspo2PercentageComplete;
    }

    public void setWspo2PercentageComplete(int wspo2PercentageComplete) {
        this.wspo2PercentageComplete = wspo2PercentageComplete;
    }

    public int getWspo2LowSnr() {
        return wspo2LowSnr;
    }

    public void setWspo2LowSnr(int wspo2LowSnr) {
        this.wspo2LowSnr = wspo2LowSnr;
    }

    public int getWspo2Motion() {
        return wspo2Motion;
    }

    public void setWspo2Motion(int wspo2Motion) {
        this.wspo2Motion = wspo2Motion;
    }

    public int getWspo2LowPi() {
        return wspo2LowPi;
    }

    public void setWspo2LowPi(int wspo2LowPi) {
        this.wspo2LowPi = wspo2LowPi;
    }

    public int getWspo2UnreliableR() {
        return wspo2UnreliableR;
    }

    public void setWspo2UnreliableR(int wspo2UnreliableR) {
        this.wspo2UnreliableR = wspo2UnreliableR;
    }

    public int getWspo2State() {
        return wspo2State;
    }

    public void setWspo2State(int wspo2State) {
        this.wspo2State = wspo2State;
    }

    public int getScdState() {
        return scdState;
    }

    public void setScdState(int scdState) {
        this.scdState = scdState;
    }

    public int getWalkSteps() {
        return walkSteps;
    }

    public void setWalkSteps(int walkSteps) {
        this.walkSteps = walkSteps;
    }

    public int getRunSteps() {
        return runSteps;
    }

    public void setRunSteps(int runSteps) {
        this.runSteps = runSteps;
    }

    public int getKCal() {
        return kCal;
    }

    public void setKCal(int kCal) {
        this.kCal = kCal;
    }

    public int getTotalActEnergy() {
        return totalActEnergy;
    }

    public void setTotalActEnergy(int totalActEnergy) {
        this.totalActEnergy = totalActEnergy;
    }

    public int getIbiOffset() {
        return ibiOffset;
    }

    public void setIbiOffset(int ibiOffset) {
        this.ibiOffset = ibiOffset;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTimeStampUpper() {
        return (int) (timestamp >> 32);
    }

    public int getTimeStampLower() {
        return (int) (timestamp);
    }
}
