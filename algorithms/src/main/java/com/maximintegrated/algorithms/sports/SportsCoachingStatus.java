package com.maximintegrated.algorithms.sports;

public enum SportsCoachingStatus {
    NO_INPUT(0),
    PROGRESS(1),
    SUCCESS(2),
    FAILURE(3);

    public final int value;

    SportsCoachingStatus(int value) {
        this.value = value;
    }
}
