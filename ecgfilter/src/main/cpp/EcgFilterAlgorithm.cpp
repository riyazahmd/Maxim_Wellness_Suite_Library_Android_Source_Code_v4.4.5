#include <jni.h>
#include "ECG_API.h"
#include "AndroidDebug.h"


static ecgConfig initData;
static ecgInput inputData;
static ecgOutput outputData;

static jmethodID updateOutputMethodId;


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_maximintegrated_ecgfilter_EcgFilterAlgorithm_init(JNIEnv *env, jclass type,
                                                           jint samplingFrequency,
                                                           jfloat algorithmOutputGain,
                                                           jboolean isCicFilterCompensationOn,
                                                           jboolean isInAdcCount) {

    initData.Fs = (unsigned int) samplingFrequency;
    initData.algoGain = algorithmOutputGain;
    initData.cicCompensate = isCicFilterCompensationOn;
    initData.inADC = isInAdcCount;

    int returnCode = ecgInit(&initData);
    if (returnCode != 0) {
        LOGE("ecgInit -> FAILURE(%d)", returnCode);
        return JNI_FALSE;
    }

    LOGI("ecgInit -> SUCCESS");
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_maximintegrated_ecgfilter_EcgFilterAlgorithm_run(JNIEnv *env, jclass type, jfloat ecgRaw,
                                                          jint notchFrequency, jint cutoffFrequency,
                                                          jboolean isAdaptiveFilterOn,
                                                          jboolean isBaselineRemovalOn,
                                                          jobject output) {
    inputData.ecgRaw = ecgRaw;
    inputData.notchFreq = (unsigned int) notchFrequency;
    inputData.cutoffFreq = (unsigned int) cutoffFrequency;
    inputData.adaptiveFilterOn = isAdaptiveFilterOn;
    inputData.baselineRemoveOn = isBaselineRemovalOn;

    int returnCode = ecgProcess(&inputData, &outputData);
    if (returnCode != 0) {
        LOGE("ecgProcess -> FAILURE(%d)", returnCode);
        return JNI_FALSE;
    }

    env->CallVoidMethod(output, updateOutputMethodId, outputData.output, outputData.HR,
                        outputData.HRV, outputData.baseline, outputData.ecgPulseFlag);

    LOGI("ecgProcess -> SUCCESS");
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_maximintegrated_ecgfilter_EcgFilterAlgorithm_end(JNIEnv *env, jclass type) {
    int returnCode = ecgEnd();
    if (returnCode != 0) {
        LOGE("ecgEnd -> FAILURE(%d)", returnCode);
        return JNI_FALSE;
    }

    LOGI("ecgEnd -> SUCCESS");
    return JNI_TRUE;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass outputClass = env->FindClass("com/maximintegrated/ecgfilter/EcgFilterOutput");
    updateOutputMethodId = env->GetMethodID(outputClass, "update", "(FFFFZ)V");

    return JNI_VERSION_1_6;
}