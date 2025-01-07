package com.maximintegrated.algorithms;

import com.maximintegrated.algorithms.hrv.HrvAlgorithmOutput;
import com.maximintegrated.algorithms.respiratory.RespiratoryRateAlgorithmOutput;
import com.maximintegrated.algorithms.sleep.SleepAlgorithmOutputFrame;
import com.maximintegrated.algorithms.sports.SportsCoachingAlgorithmOutput;
import com.maximintegrated.algorithms.stress.StressAlgorithmOutput;

public class AlgorithmOutput {
    private HrvAlgorithmOutput hrv;
    private RespiratoryRateAlgorithmOutput respiratory;
    private StressAlgorithmOutput stress;
    private SleepAlgorithmOutputFrame sleep;
    private SportsCoachingAlgorithmOutput sports;

    public AlgorithmOutput(HrvAlgorithmOutput hrv, RespiratoryRateAlgorithmOutput respiratory, StressAlgorithmOutput stress, SleepAlgorithmOutputFrame sleep, SportsCoachingAlgorithmOutput sports) {
        this.hrv = hrv;
        this.respiratory = respiratory;
        this.stress = stress;
        this.sleep = sleep;
        this.sports = sports;
    }

    public AlgorithmOutput() {
        hrv = new HrvAlgorithmOutput();
        respiratory = new RespiratoryRateAlgorithmOutput();
        stress = new StressAlgorithmOutput();
        sleep = new SleepAlgorithmOutputFrame();
        sports = new SportsCoachingAlgorithmOutput();
    }

    public HrvAlgorithmOutput getHrv() {
        return hrv;
    }

    public void setHrv(HrvAlgorithmOutput hrv) {
        this.hrv = hrv;
    }

    public RespiratoryRateAlgorithmOutput getRespiratory() {
        return respiratory;
    }

    public void setRespiratory(RespiratoryRateAlgorithmOutput respiratory) {
        this.respiratory = respiratory;
    }

    public StressAlgorithmOutput getStress() {
        return stress;
    }

    public void setStress(StressAlgorithmOutput stress) {
        this.stress = stress;
    }

    public SleepAlgorithmOutputFrame getSleep() {
        return sleep;
    }

    public void setSleep(SleepAlgorithmOutputFrame sleep) {
        this.sleep = sleep;
    }

    public SportsCoachingAlgorithmOutput getSports() {
        return sports;
    }

    public void setSports(SportsCoachingAlgorithmOutput sports) {
        this.sports = sports;
    }

    public void hrvUpdate(float avnn,
                          float sdnn,
                          float rmssd,
                          float pnn50,

                          float ulf,
                          float vlf,
                          float lf,
                          float hf,
                          float lfOverHf,
                          float totPwr, int percentCompleted, boolean isHrvCalculated) {
        hrv.update(avnn, sdnn, rmssd, pnn50, ulf, vlf, lf, hf, lfOverHf, totPwr, percentCompleted, isHrvCalculated);
    }

    public void respiratoryUpdate(float respirationRate, float confidenceLevel, boolean motionFlag, boolean ibiLowQualityFlag, boolean ppgLowQualityFlag) {
        respiratory.update(respirationRate, confidenceLevel, motionFlag, ibiLowQualityFlag, ppgLowQualityFlag);
    }

    public void stressUpdate(boolean stressClass, int stressScore, float stressScorePrc) {
        stress.update(stressClass, stressScore, stressScorePrc);
    }

    public void sleepUpdate(int sleepWakeDecisionStatus, int sleepWakeDecision,
                            int sleepWakeDetentionLatency, float sleepWakeOutputConfLevel,
                            int sleepPhaseOutputStatus, int sleepPhaseOutput,
                            float sleepPhaseOutputConfLevel, int encodedOutput_sleepPhaseOutput,
                            int encodedOutput_duration, boolean encodedOutput_needsStorage,
                            float hr, float accMag, float ibi, float sleepRestingHR,
                            int arrayLength, long dateInfo) {
        sleep.update(sleepWakeDecisionStatus, sleepWakeDecision, sleepWakeDetentionLatency,
                sleepWakeOutputConfLevel, sleepPhaseOutputStatus, sleepPhaseOutput,
                sleepPhaseOutputConfLevel, encodedOutput_sleepPhaseOutput, encodedOutput_duration,
                encodedOutput_needsStorage, hr, accMag, ibi, sleepRestingHR, arrayLength, dateInfo);
    }

    public void sportsUpdate(int session, int percentCompleted, int minHr, int maxHr, int meanHr, float readiness,
                             float relax, float vo2, float age, float poorMedium, float mediumGood,
                             float goodExcellent, int recoveryTime, float epoc, int hr0, int lastHr,
                             int recoveryPercentage, int status, boolean newOutputReady, long timestamp) {
        sports.update(session, percentCompleted, minHr, maxHr, meanHr, readiness, relax, vo2, age, poorMedium,
                mediumGood, goodExcellent, recoveryTime, epoc, hr0, lastHr, recoveryPercentage, status,
                newOutputReady, timestamp);
    }
}
