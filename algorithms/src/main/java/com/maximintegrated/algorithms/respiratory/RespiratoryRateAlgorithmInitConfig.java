package com.maximintegrated.algorithms.respiratory;

public class RespiratoryRateAlgorithmInitConfig {

    public static int DEFAULT_MOTION_MAGNITUDE_LIMIT = 800;

    public enum SourceOptions {
        WRIST(0),
        FINGER(1);

        public final int value;

        SourceOptions(int value) {
            this.value = value;
        }
    }

    public enum LedCodes {
        GREEN(0),
        IR(1),
        RED(2);

        public final int value;

        LedCodes(int value) {
            this.value = value;
        }
    }

    public enum SamplingRateOption {
        Hz_25(25),
        Hz_100(100);

        public final int value;

        SamplingRateOption(int value) {
            this.value = value;
        }
    }

    private SourceOptions sourceOptions;
    private LedCodes ledCodes;
    private SamplingRateOption samplingRateOption;
    private int motionMagnitudeLimitInMg;

    public RespiratoryRateAlgorithmInitConfig() {
        sourceOptions = SourceOptions.WRIST;
        ledCodes = LedCodes.GREEN;
        samplingRateOption = SamplingRateOption.Hz_25;
        motionMagnitudeLimitInMg = DEFAULT_MOTION_MAGNITUDE_LIMIT;
    }

    public RespiratoryRateAlgorithmInitConfig(SourceOptions sourceOptions, LedCodes ledCodes, SamplingRateOption samplingRateOption, int motionMagnitudeLimitInMg) {
        this.sourceOptions = sourceOptions;
        this.ledCodes = ledCodes;
        this.samplingRateOption = samplingRateOption;
        this.motionMagnitudeLimitInMg = motionMagnitudeLimitInMg;
    }

    public SourceOptions getSourceOptions() {
        return sourceOptions;
    }

    public void setSourceOptions(SourceOptions sourceOptions) {
        this.sourceOptions = sourceOptions;
    }

    public LedCodes getLedCodes() {
        return ledCodes;
    }

    public void setLedCodes(LedCodes ledCodes) {
        this.ledCodes = ledCodes;
    }

    public SamplingRateOption getSamplingRateOption() {
        return samplingRateOption;
    }

    public void setSamplingRateOption(SamplingRateOption samplingRateOption) {
        this.samplingRateOption = samplingRateOption;
    }

    public int getMotionMagnitudeLimitInMg() {
        return motionMagnitudeLimitInMg;
    }

    public void setMotionMagnitudeLimitInMg(int motionMagnitudeLimitInMg) {
        this.motionMagnitudeLimitInMg = motionMagnitudeLimitInMg;
    }

    @Override
    public String toString() {
        return "RespiratoryRateAlgorithmInitConfig{" +
                "sourceOptions=" + sourceOptions +
                ", ledCodes=" + ledCodes +
                ", samplingRateOption=" + samplingRateOption +
                ", motionMagnitudeLimitInMg=" + motionMagnitudeLimitInMg +
                '}';
    }
}
