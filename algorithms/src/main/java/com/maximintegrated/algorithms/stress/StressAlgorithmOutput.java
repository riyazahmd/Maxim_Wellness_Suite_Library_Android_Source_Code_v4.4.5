package com.maximintegrated.algorithms.stress;

public class StressAlgorithmOutput {

    private boolean stressClass;
    private int stressScore;
    private float stressScorePrc;

    public StressAlgorithmOutput() {

    }

    public StressAlgorithmOutput(boolean stressClass, int stressScore, float stressScorePrc) {
        this.stressClass = stressClass;
        this.stressScore = stressScore;
        this.stressScorePrc = stressScorePrc;
    }

    public boolean isStressClass() {
        return stressClass;
    }

    public void setStressClass(boolean stressClass) {
        this.stressClass = stressClass;
    }

    public int getStressScore() {
        return stressScore;
    }

    public void setStressScore(int stressScore) {
        this.stressScore = stressScore;
    }

    public float getStressScorePrc() {
        return stressScorePrc;
    }

    public void setStressScorePrc(float stressScorePrc) {
        this.stressScorePrc = stressScorePrc;
    }

    public void update(boolean stressClass, int stressScore, float stressScorePrc) {
        this.stressClass = stressClass;
        this.stressScore = stressScore;
        this.stressScorePrc = stressScorePrc;
    }

    @Override
    public String toString() {
        return "StressAlgorithmOutput{" +
                "stressClass=" + stressClass +
                ", stressScore=" + stressScore +
                ", stressScorePrc=" + stressScorePrc +
                '}';
    }
}
