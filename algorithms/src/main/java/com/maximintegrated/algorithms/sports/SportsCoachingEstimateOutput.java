package com.maximintegrated.algorithms.sports;

public class SportsCoachingEstimateOutput {
    public static final int SIZE = SportsCoachingReadinessOutput.SIZE + SportsCoachingVo2MaxOutput.SIZE + SportsCoachingRecoveryOutput.SIZE;
    private SportsCoachingReadinessOutput readiness = new SportsCoachingReadinessOutput();
    private SportsCoachingVo2MaxOutput vo2max = new SportsCoachingVo2MaxOutput();
    private SportsCoachingRecoveryOutput recovery = new SportsCoachingRecoveryOutput();

    public SportsCoachingEstimateOutput() {

    }

    public SportsCoachingEstimateOutput(SportsCoachingReadinessOutput readiness, SportsCoachingVo2MaxOutput vo2max, SportsCoachingRecoveryOutput recovery) {
        this.readiness = readiness;
        this.vo2max = vo2max;
        this.recovery = recovery;
    }

    public SportsCoachingReadinessOutput getReadiness() {
        return readiness;
    }

    public void setReadiness(SportsCoachingReadinessOutput readiness) {
        this.readiness = readiness;
    }

    public SportsCoachingVo2MaxOutput getVo2max() {
        return vo2max;
    }

    public void setVo2max(SportsCoachingVo2MaxOutput vo2max) {
        this.vo2max = vo2max;
    }

    public SportsCoachingRecoveryOutput getRecovery() {
        return recovery;
    }

    public void setRecovery(SportsCoachingRecoveryOutput recovery) {
        this.recovery = recovery;
    }

    public void update(float readiness, float relax, float vo2, float age, float poorMedium,
                       float mediumGood, float goodExcellent, int recoveryTime, float epoc, int hr0,
                       int lastHr, int recoveryPercentage) {
        this.readiness.update(readiness);
        this.vo2max.update(relax, vo2, age, poorMedium, mediumGood, goodExcellent);
        this.recovery.update(recoveryTime, epoc, hr0, lastHr, recoveryPercentage);
    }
}
