package com.maximintegrated.algorithms.sleep;

public class SleepAlgorithmInitConfig {

    public enum DetectableSleepDuration {

        MINIMUM_30_MIN(30),
        MINIMUM_40_MIN(40),
        MINIMUM_50_MIN(50),
        MINIMUM_60_MIN(60);

        public final int value;

        DetectableSleepDuration(int value) {
            this.value = value;
        }
    }

    private DetectableSleepDuration detectableSleepDuration;
    private boolean restingHrAvailable = true;
    private boolean confidenceLevelAvailableHr = true;
    private boolean confidenceLevelAvailableIbi = true;
    private boolean activityAvailable = true;


    public SleepAlgorithmInitConfig(DetectableSleepDuration detectableSleepDuration, boolean restingHrAvailable, boolean confidenceLevelAvailableHr, boolean confidenceLevelAvailableIbi, boolean activityAvailable) {
        this.detectableSleepDuration = detectableSleepDuration;
        this.restingHrAvailable = restingHrAvailable;
        this.confidenceLevelAvailableHr = confidenceLevelAvailableHr;
        this.confidenceLevelAvailableIbi = confidenceLevelAvailableIbi;
        this.activityAvailable = activityAvailable;
    }

    public SleepAlgorithmInitConfig() {
        detectableSleepDuration = DetectableSleepDuration.MINIMUM_30_MIN;
    }

    public DetectableSleepDuration getDetectableSleepDuration() {
        return detectableSleepDuration;
    }

    public void setDetectableSleepDuration(DetectableSleepDuration detectableSleepDuration) {
        this.detectableSleepDuration = detectableSleepDuration;
    }

    public boolean isRestingHrAvailable() {
        return restingHrAvailable;
    }

    public void setRestingHrAvailable(boolean restingHrAvailable) {
        this.restingHrAvailable = restingHrAvailable;
    }

    public boolean isConfidenceLevelAvailableHr() {
        return confidenceLevelAvailableHr;
    }

    public void setConfidenceLevelAvailableHr(boolean confidenceLevelAvailableHr) {
        this.confidenceLevelAvailableHr = confidenceLevelAvailableHr;
    }

    public boolean isConfidenceLevelAvailableIbi() {
        return confidenceLevelAvailableIbi;
    }

    public void setConfidenceLevelAvailableIbi(boolean confidenceLevelAvailableIbi) {
        this.confidenceLevelAvailableIbi = confidenceLevelAvailableIbi;
    }

    public boolean isActivityAvailable() {
        return activityAvailable;
    }

    public void setActivityAvailable(boolean activityAvailable) {
        this.activityAvailable = activityAvailable;
    }
}
