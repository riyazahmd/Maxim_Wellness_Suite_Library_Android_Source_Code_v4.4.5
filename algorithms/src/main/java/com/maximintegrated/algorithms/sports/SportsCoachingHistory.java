package com.maximintegrated.algorithms.sports;

import java.util.ArrayList;

public class SportsCoachingHistory {
    private int numberOfRecords = 0;
    private ArrayList<SportsCoachingHistoryItem> records = new ArrayList<>();

    public SportsCoachingHistory(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
        if (numberOfRecords <= 0) return;
        for (int i = 0; i < numberOfRecords; i++) {
            records.add(new SportsCoachingHistoryItem());
        }
    }

    public SportsCoachingHistory(ArrayList<SportsCoachingHistoryItem> records) {
        this.records = records;
        numberOfRecords = records.size();
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public ArrayList<SportsCoachingHistoryItem> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<SportsCoachingHistoryItem> records) {
        this.records = records;
        numberOfRecords = records.size();
    }

    public float[] toFloat() {
        int size = 1 + numberOfRecords * SportsCoachingHistoryItem.SIZE;
        float[] array = new float[size];
        array[0] = numberOfRecords;
        int offset = 1;
        for (int i = 0; i < numberOfRecords; i++) {
            array[offset++] = records.get(i).getTimeStampUpper();
            array[offset++] = records.get(i).getTimeStampLower();
            array[offset++] = records.get(i).getScores().getReadiness().getReadinessScore();
            array[offset++] = records.get(i).getScores().getVo2max().getRelax();
            array[offset++] = records.get(i).getScores().getVo2max().getVo2();
            array[offset++] = records.get(i).getScores().getVo2max().getFitnessAge();
            array[offset++] = records.get(i).getScores().getVo2max().getFitnessRegionPoorMedium();
            array[offset++] = records.get(i).getScores().getVo2max().getFitnessRegionMediumGood();
            array[offset++] = records.get(i).getScores().getVo2max().getFitnessRegionGoodExcellent();
            array[offset++] = records.get(i).getScores().getRecovery().getRecoveryTimeMin();
            array[offset++] = records.get(i).getScores().getRecovery().getEpoc();
            array[offset++] = records.get(i).getScores().getRecovery().getHr0();
            array[offset++] = records.get(i).getScores().getRecovery().getLastHr();
            array[offset++] = records.get(i).getScores().getRecovery().getRecoveryPercentage();
            array[offset++] = records.get(i).getHrStats().getMinHr();
            array[offset++] = records.get(i).getHrStats().getMaxHr();
            array[offset++] = records.get(i).getHrStats().getMeanHr();
            array[offset++] = records.get(i).getSession().value;
        }
        return array;
    }
}
