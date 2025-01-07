package com.maximintegrated.algorithms.sports;

public class SportsCoachingHrStats {
    public static final int SIZE = 3;
    private int minHr = 0;
    private int maxHr = 0;
    private int meanHr = 0;

    public SportsCoachingHrStats() {

    }

    public SportsCoachingHrStats(int minHr, int maxHr, int meanHr) {
        this.minHr = minHr;
        this.maxHr = maxHr;
        this.meanHr = meanHr;
    }

    public int getMinHr() {
        return minHr;
    }

    public void setMinHr(int minHr) {
        this.minHr = minHr;
    }

    public int getMaxHr() {
        return maxHr;
    }

    public void setMaxHr(int maxHr) {
        this.maxHr = maxHr;
    }

    public int getMeanHr() {
        return meanHr;
    }

    public void setMeanHr(int meanHr) {
        this.meanHr = meanHr;
    }

    public void update(int minHr, int maxHr, int meanHr) {
        this.minHr = minHr;
        this.maxHr = maxHr;
        this.meanHr = meanHr;
    }
}
