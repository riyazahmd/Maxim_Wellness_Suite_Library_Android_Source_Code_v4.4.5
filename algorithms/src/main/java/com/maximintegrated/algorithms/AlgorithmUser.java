package com.maximintegrated.algorithms;

import java.util.Calendar;

public class AlgorithmUser {

    public enum Gender {
        MALE,
        FEMALE
    }

    private String username;
    private Gender gender;
    private int birthYear;
    private int heightInCm;
    private int weightInKg;
    private float initialHr;
    private float sleepRestingHr;
    private int age;

    public AlgorithmUser() {
        username = "";
        gender = Gender.MALE;
        birthYear = 1980;
        heightInCm = 170;
        weightInKg = 70;
        initialHr = 70;
        sleepRestingHr = 0;
        age = Calendar.getInstance().get(Calendar.YEAR) - birthYear;
    }

    public AlgorithmUser(String username, Gender gender, int birthYear, int heightInCm, int weightInKg, float initialHr, float sleepRestingHr) {
        this.username = username;
        this.gender = gender;
        this.birthYear = birthYear;
        this.heightInCm = heightInCm;
        this.weightInKg = weightInKg;
        this.initialHr = initialHr;
        this.sleepRestingHr = sleepRestingHr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public int getHeightInCm() {
        return heightInCm;
    }

    public void setHeightInCm(int heightInCm) {
        this.heightInCm = heightInCm;
    }

    public int getWeightInKg() {
        return weightInKg;
    }

    public void setWeightInKg(int weightInKg) {
        this.weightInKg = weightInKg;
    }

    public float getInitialHr() {
        return initialHr;
    }

    public void setInitialHr(float initialHr) {
        this.initialHr = initialHr;
    }

    public float getSleepRestingHr() {
        return sleepRestingHr;
    }

    public void setSleepRestingHr(float sleepRestingHr) {
        this.sleepRestingHr = sleepRestingHr;
    }

    public int getAge() {
        return Calendar.getInstance().get(Calendar.YEAR) - birthYear;
    }
}
