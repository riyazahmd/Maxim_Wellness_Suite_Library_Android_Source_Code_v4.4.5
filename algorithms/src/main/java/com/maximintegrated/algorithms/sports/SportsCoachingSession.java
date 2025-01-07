package com.maximintegrated.algorithms.sports;

public enum SportsCoachingSession {

    UNDEFINED(0),
    VO2MAX_RELAX(1),
    VO2(2),
    RECOVERY_TIME(3),
    READINESS(4),
    VO2MAX_FROM_HISTORY(5),
    EPOC_RECOVERY(6);

    public final int value;

    SportsCoachingSession(int value) {
        this.value = value;
    }
}
