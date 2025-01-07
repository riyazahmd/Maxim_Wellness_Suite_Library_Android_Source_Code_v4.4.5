package com.maximintegrated.algorithms.sports;

public class SportsCoachingEpocConfig {
    private int exerciseDurationMinutes = 0;
    private int exerciseIntensity = 0;
    private int minutesAfterExercise = 0;

    public SportsCoachingEpocConfig() {

    }

    public SportsCoachingEpocConfig(int exerciseDurationMinutes, int exerciseIntensity, int minutesAfterExercise) {
        this.exerciseDurationMinutes = exerciseDurationMinutes;
        this.exerciseIntensity = exerciseIntensity;
        this.minutesAfterExercise = minutesAfterExercise;
    }

    public int getExerciseDurationMinutes() {
        return exerciseDurationMinutes;
    }

    public void setExerciseDurationMinutes(int exerciseDurationMinutes) {
        this.exerciseDurationMinutes = exerciseDurationMinutes;
    }

    public int getExerciseIntensity() {
        return exerciseIntensity;
    }

    public void setExerciseIntensity(int exerciseIntensity) {
        this.exerciseIntensity = exerciseIntensity;
    }

    public int getMinutesAfterExercise() {
        return minutesAfterExercise;
    }

    public void setMinutesAfterExercise(int minutesAfterExercise) {
        this.minutesAfterExercise = minutesAfterExercise;
    }
}
