package com.maximintegrated.algorithms.sports;

public class SportsCoachingAlgorithmOutput {
    private SportsCoachingSession session = SportsCoachingSession.UNDEFINED;
    private int percentCompleted = 0;
    private SportsCoachingHrStats hrStats = new SportsCoachingHrStats();
    private SportsCoachingEstimateOutput estimates = new SportsCoachingEstimateOutput();
    private SportsCoachingStatus status = SportsCoachingStatus.PROGRESS;
    private boolean newOutputReady = false;
    private long timestamp = 0L;

    public SportsCoachingAlgorithmOutput() {

    }

    public SportsCoachingAlgorithmOutput(SportsCoachingSession session,
                                         int percentCompleted, SportsCoachingHrStats hrStats,
                                         SportsCoachingEstimateOutput estimates,
                                         SportsCoachingStatus status, boolean newOutputReady, long timestamp) {
        this.session = session;
        this.percentCompleted = percentCompleted;
        this.hrStats = hrStats;
        this.estimates = estimates;
        this.status = status;
        this.newOutputReady = newOutputReady;
        this.timestamp = timestamp;
    }

    public SportsCoachingSession getSession() {
        return session;
    }

    public void setSession(SportsCoachingSession session) {
        this.session = session;
    }

    public int getPercentCompleted() {
        return percentCompleted;
    }

    public void setPercentCompleted(int percentCompleted) {
        this.percentCompleted = percentCompleted;
    }

    public SportsCoachingHrStats getHrStats() {
        return hrStats;
    }

    public void setHrStats(SportsCoachingHrStats hrStats) {
        this.hrStats = hrStats;
    }

    public SportsCoachingEstimateOutput getEstimates() {
        return estimates;
    }

    public void setEstimates(SportsCoachingEstimateOutput estimates) {
        this.estimates = estimates;
    }

    public SportsCoachingStatus getStatus() {
        return status;
    }

    public void setStatus(SportsCoachingStatus status) {
        this.status = status;
    }

    public boolean isNewOutputReady() {
        return newOutputReady;
    }

    public void setNewOutputReady(boolean newOutputReady) {
        this.newOutputReady = newOutputReady;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void update(int session, int percentCompleted, int minHr, int maxHr, int meanHr, float readiness,
                       float relax, float vo2, float age, float poorMedium, float mediumGood,
                       float goodExcellent, int recoveryTime, float epoc, int hr0, int lastHr,
                       int recoveryPercentage, int status, boolean newOutputReady, long timestamp) {
        this.session = SportsCoachingSession.values()[session];
        this.percentCompleted = percentCompleted;
        hrStats.update(minHr, maxHr, meanHr);
        estimates.update(readiness, relax, vo2, age, poorMedium, mediumGood, goodExcellent, recoveryTime, epoc, hr0, lastHr, recoveryPercentage);
        this.status = SportsCoachingStatus.values()[status];
        this.newOutputReady = newOutputReady;
        this.timestamp = timestamp;
    }
}
