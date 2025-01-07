package com.maximintegrated.algorithms.sports;

public class SportsCoachingReadinessOutput {
    public static final int SIZE = 1;
    private float readinessScore = 0f;

    public SportsCoachingReadinessOutput() {

    }

    public SportsCoachingReadinessOutput(float readinessScore) {
        this.readinessScore = readinessScore;
    }

    public float getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(float readinessScore) {
        this.readinessScore = readinessScore;
    }

    public void update(float readiness) {
        this.readinessScore = readiness;
    }
}
