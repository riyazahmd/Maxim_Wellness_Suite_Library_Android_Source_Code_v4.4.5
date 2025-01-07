package com.maximintegrated.algorithms.sports;

public class SportsCoachingVo2MaxOutput {
    public static final int SIZE = 6;
    private float relax = 0;
    private float vo2 = 0;
    private float fitnessAge = 0;
    private float fitnessRegionPoorMedium = 25;
    private float fitnessRegionMediumGood = 50;
    private float fitnessRegionGoodExcellent = 75;

    public SportsCoachingVo2MaxOutput() {

    }

    public SportsCoachingVo2MaxOutput(float relax, float vo2, float fitnessAge, float fitnessRegionPoorMedium, float fitnessRegionMediumGood, float fitnessRegionGoodExcellent) {
        this.relax = relax;
        this.vo2 = vo2;
        this.fitnessAge = fitnessAge;
        this.fitnessRegionPoorMedium = fitnessRegionPoorMedium;
        this.fitnessRegionMediumGood = fitnessRegionMediumGood;
        this.fitnessRegionGoodExcellent = fitnessRegionGoodExcellent;
    }

    public float getRelax() {
        return relax;
    }

    public void setRelax(float relax) {
        this.relax = relax;
    }

    public float getVo2() {
        return vo2;
    }

    public void setVo2(float vo2) {
        this.vo2 = vo2;
    }

    public float getFitnessAge() {
        return fitnessAge;
    }

    public void setFitnessAge(float fitnessAge) {
        this.fitnessAge = fitnessAge;
    }

    public float getFitnessRegionPoorMedium() {
        return fitnessRegionPoorMedium;
    }

    public void setFitnessRegionPoorMedium(float fitnessRegionPoorMedium) {
        this.fitnessRegionPoorMedium = fitnessRegionPoorMedium;
    }

    public float getFitnessRegionMediumGood() {
        return fitnessRegionMediumGood;
    }

    public void setFitnessRegionMediumGood(float fitnessRegionMediumGood) {
        this.fitnessRegionMediumGood = fitnessRegionMediumGood;
    }

    public float getFitnessRegionGoodExcellent() {
        return fitnessRegionGoodExcellent;
    }

    public void setFitnessRegionGoodExcellent(float fitnessRegionGoodExcellent) {
        this.fitnessRegionGoodExcellent = fitnessRegionGoodExcellent;
    }

    public void update(float relax, float vo2, float fitnessAge, float fitnessRegionPoorMedium,
                       float fitnessRegionMediumGood, float fitnessRegionGoodExcellent) {
        this.relax = relax;
        this.vo2 = vo2;
        this.fitnessAge = fitnessAge;
        this.fitnessRegionPoorMedium = fitnessRegionPoorMedium;
        this.fitnessRegionMediumGood = fitnessRegionMediumGood;
        this.fitnessRegionGoodExcellent = fitnessRegionGoodExcellent;
    }
}
