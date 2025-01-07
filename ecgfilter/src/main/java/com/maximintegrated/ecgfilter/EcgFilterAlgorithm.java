package com.maximintegrated.ecgfilter;

public final class EcgFilterAlgorithm {

    static {
        System.loadLibrary("EcgFilterAlgorithm");
    }

    private EcgFilterAlgorithm() {
    }

    public static boolean init(EcgFilterInitConfig initConfig) {
        return init(initConfig.getSamplingFrequency(), initConfig.getAlgorithmOutputGain(),
                initConfig.isCicFilterCompensationOn(), initConfig.isInAdcCount());
    }

    public static boolean run(EcgFilterInput input, EcgFilterOutput output) {
        return run(input.getEcgRaw(), input.getNotchFrequency(), input.getCutoffFrequency(),
                input.isAdaptiveFilterOn(), input.isBaselineRemovalOn(), output);
    }

    public static native boolean init(int samplingFrequency, float algorithmOutputGain,
                                      boolean isCicFilterCompensationOn, boolean isInAdcCount);

    public static native boolean run(float ecgRaw, int notchFrequency, int cutoffFrequency,
                                     boolean isAdaptiveFilterOn, boolean isBaselineRemovalOn,
                                     EcgFilterOutput output);

    public static native boolean end();
}
