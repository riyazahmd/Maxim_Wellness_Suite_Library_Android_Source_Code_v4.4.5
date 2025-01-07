package com.maximintegrated.algorithms.sports;

public class SportsCoachingHistoryItem {
    public static final int SIZE = 2 + SportsCoachingEstimateOutput.SIZE + SportsCoachingHrStats.SIZE + 1;
    private long timestamp = 0; // 2 byte
    private SportsCoachingEstimateOutput scores = new SportsCoachingEstimateOutput();
    private SportsCoachingHrStats hrStats = new SportsCoachingHrStats();
    private SportsCoachingSession session = SportsCoachingSession.UNDEFINED;

    public SportsCoachingHistoryItem() {

    }

    public SportsCoachingHistoryItem(long timestamp, SportsCoachingEstimateOutput scores, SportsCoachingHrStats hrStats, SportsCoachingSession session) {
        this.timestamp = timestamp;
        this.scores = scores;
        this.hrStats = hrStats;
        this.session = session;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public SportsCoachingEstimateOutput getScores() {
        return scores;
    }

    public void setScores(SportsCoachingEstimateOutput scores) {
        this.scores = scores;
    }

    public SportsCoachingHrStats getHrStats() {
        return hrStats;
    }

    public void setHrStats(SportsCoachingHrStats hrStats) {
        this.hrStats = hrStats;
    }

    public SportsCoachingSession getSession() {
        return session;
    }

    public void setSession(SportsCoachingSession session) {
        this.session = session;
    }

    public int getTimeStampUpper() {
        return (int) (timestamp >> 32);
    }

    public int getTimeStampLower() {
        return (int) (timestamp);
    }
}
