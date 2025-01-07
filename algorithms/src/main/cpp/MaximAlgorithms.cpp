//
// Created by Meliksah.Cakir on 12/9/2019.
//

#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <AlgoWrapper.h>

#define TAG "ALGORITHMS"
#define LOGD(...) //__android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define ERROR_ALGO_INIT (0xFF)

static mxm_algosuite_init_data initData;

static jmethodID updateHrvOutputMethodId;
static jmethodID updateRespOutputMethodId;
static jmethodID updateStressOutputMethodId;
static jmethodID updateSleepOutputMethodId;
static jmethodID updateSportsOutputMethodId;
static jmethodID setVersionMethodId;

extern "C"
JNIEXPORT jint JNICALL
Java_com_maximintegrated_algorithms_MaximAlgorithms_init(JNIEnv *env, jclass clazz,
                                                         jint enable_flag,
                                                         jfloat hrv_sampling_period,
                                                         jint hrv_window_size_in_sec,
                                                         jint hrv_window_shift_size_in_sec,
                                                         jint resp_source, jint resp_led_code,
                                                         jint resp_sampling_rate,
                                                         jint resp_motion_mag_limit_in_mg,
                                                         jint stress_config, jint sleep_duration,
                                                         jfloat resting_hr, jint sampling_rate, jint session,
                                                         jint age, jint birth_year, jint gender, jint weight,
                                                         jint height, jboolean is_metric,
                                                         jfloatArray records,
                                                         jint exercise_duration_min,
                                                         jint exercise_intensity,
                                                         jint min_after_exercise,
                                                         jlong last_epoc_recovery_timestamp,
                                                         jint last_recovery_estimate_in_minutes,
                                                         jint last_hr) {
    mxm_algosuite_return_code status;

    initData.enabledAlgorithms = (unsigned char) enable_flag;
    initData.hrvConfig.samplingPeriod = hrv_sampling_period;
    initData.hrvConfig.windowSizeInSec = (uint16_t) hrv_window_size_in_sec;
    initData.hrvConfig.windowShiftSizeInSec = (uint16_t) hrv_window_shift_size_in_sec;
    initData.respConfig.signal_source_option = (mxm_respiration_rate_manager_ppg_source_options) resp_source;
    initData.respConfig.led_code = (mxm_respiration_rate_manager_led_codes) resp_led_code;
    initData.respConfig.sampling_rate = (mxm_respiration_rate_manager_sampling_rate_option) resp_sampling_rate;
    initData.stressConfig.dummy_config_for_compilation = (uint8_t) stress_config;
    initData.sleepConfig.mxm_sleep_detection_duration = (mxm_sleep_manager_minimum_detectable_sleep_duration) sleep_duration;
    initData.sleepConfig.user_info.age = (uint16_t) age;
    initData.sleepConfig.user_info.weight = (uint16_t) weight;
    initData.sleepConfig.user_info.gender = (mxm_sleep_manager_gender) gender;
    initData.sleepConfig.user_info.sleep_resting_hr = resting_hr;
    initData.sc_config.sampling_rate = sampling_rate;
    initData.sc_config.session_code = (mxm_sc_session) session;
    initData.sc_config.user.birth_year = birth_year;
    initData.sc_config.user.birth_day = 1;
    initData.sc_config.user.birth_month = 1;
    initData.sc_config.user.gender = (mxm_sc_gender) gender;
    initData.sc_config.user.weight = weight;
    initData.sc_config.user.height = height;
    initData.sc_config.user.is_metric = is_metric;

    jfloat *history = env->GetFloatArrayElements(records, 0);

    int numOfRecords = (int) history[0];
    if(numOfRecords > MXM_SC_MAX_HISTORY_LENGTH){
        numOfRecords = MXM_SC_MAX_HISTORY_LENGTH;
    }
    initData.sc_config.history.number_of_records = numOfRecords;
    int offset = 1;
    for(int i = 0; i < numOfRecords; i++){
        auto upper = (unsigned long) history[offset++];
        auto lower = (unsigned int) history[offset++];
        initData.sc_config.history.records[i].record_date = (upper << 32) | lower;
        initData.sc_config.history.records[i].score.readiness.readiness_score = history[offset++];
        initData.sc_config.history.records[i].score.vo2_max.relax = history[offset++];
        initData.sc_config.history.records[i].score.vo2_max.VO2 = history[offset++];
        initData.sc_config.history.records[i].score.vo2_max.fitness_age = history[offset++];
        initData.sc_config.history.records[i].score.vo2_max.fitness_region_poor_medium = history[offset++];
        initData.sc_config.history.records[i].score.vo2_max.fitness_region_medium_good = history[offset++];
        initData.sc_config.history.records[i].score.vo2_max.fitness_region_good_excellent = history[offset++];
        initData.sc_config.history.records[i].score.recovery.recovery_time_min = (int) history[offset++];
        initData.sc_config.history.records[i].score.recovery.epoc = history[offset++];
        initData.sc_config.history.records[i].score.recovery.hr0 = (int) history[offset++];
        initData.sc_config.history.records[i].score.recovery.last_hr = (int) history[offset++];
        initData.sc_config.history.records[i].score.recovery.recovery_percentage = (int) history[offset++];
        initData.sc_config.history.records[i].heart_rate_stat.min_hr = (int) history[offset++];
        initData.sc_config.history.records[i].heart_rate_stat.max_hr = (int) history[offset++];
        initData.sc_config.history.records[i].heart_rate_stat.mean_hr = (int) history[offset++];
        initData.sc_config.history.records[i].session = (mxm_sc_session)((int)history[offset++]);
    }
    initData.sc_config.epoc_mode_config.exercise_duration_minutes = exercise_duration_min;
    initData.sc_config.epoc_mode_config.exercise_intensity = exercise_intensity;
    initData.sc_config.epoc_mode_config.minutes_after_exercise = min_after_exercise;
    initData.sc_config.recovery_time_mode_config.last_epoc_recovery_timestamp = (unsigned long long int)(last_epoc_recovery_timestamp);
    initData.sc_config.recovery_time_mode_config.last_recovery_estimate_in_mins = last_recovery_estimate_in_minutes;
    initData.sc_config.recovery_time_mode_config.last_hr = last_hr;

    env->ReleaseFloatArrayElements(records, history, 0);

    mxm_algosuite_manager_init(&initData, &status);

    int result = 0;

    if (initData.enabledAlgorithms == 0) {
        LOGD("INIT: ALGORITHMS NOT ENABLED");
        return ERROR_ALGO_INIT;
    }

    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_HRV) > 0) {
        if (status.hrv_status != MXM_HRV_SUCCESS) {
            LOGD("HRV INIT FAIL");
            result |= MXM_ALGOSUITE_ENABLE_HRV;
        } else {
            LOGD("HRV INIT SUCCESS");
        }
    }
    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_RESP) > 0) {
        if (status.resp_status != MXM_RESPIRATION_RATE_MANAGER_SUCCESS) {
            LOGD("RESP INIT FAIL");
            result |= MXM_ALGOSUITE_ENABLE_RESP;
        } else {
            LOGD("RESP INIT SUCCESS");
        }
    }
    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_STRESS) > 0) {
        if (status.stress_status != MXM_STRESS_MONITORING_SUCCESS) {
            LOGD("STRESS INIT FAIL");
            result |= MXM_ALGOSUITE_ENABLE_STRESS;
        } else {
            LOGD("STRESS INIT SUCCESS");
        }
    }
    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_SLEEP) > 0) {
        if (status.sleep_status != MXM_SLEEP_MANAGER_SUCCESS) {
            LOGD("SLEEP INIT FAIL");
            result |= MXM_ALGOSUITE_ENABLE_SLEEP;
        } else {
            LOGD("SLEEP INIT SUCCESS");
        }
    }
    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_SPORTS) > 0) {
        if (status.sports_coaching_status != MXM_SC_NO_ERROR) {
            LOGD("SPORTS INIT FAIL");
            result |= MXM_ALGOSUITE_ENABLE_SPORTS;
        } else {
            LOGD("SPORTS INIT SUCCESS");
        }
    }
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_maximintegrated_algorithms_MaximAlgorithms_run(JNIEnv *env, jclass clazz,
                                                        jint sample_count, jint green,
                                                        jint green2, jint ir, jint red,
                                                        jint acceleration_x, jint acceleration_y,
                                                        jint acceleration_z, jint operation_mode,
                                                        jint hr, jint hr_confidence, jint rr,
                                                        jint rr_confidence, jint activity, jint r,
                                                        jint wspo2_confidence, jint spo2,
                                                        jint wspo2_percentage_complete,
                                                        jint wspo2_low_snr, jint wspo2_motion,
                                                        jint wspo2_low_pi, jint wspo2_unreliable_r,
                                                        jint wspo2_state, jint scd_state,
                                                        jint walk_steps, jint run_steps, jint k_cal,
                                                        jint totalActEnergy, jint timestamp_upper,
                                                        jint timestamp_lower, jobject joutput) {
    mxm_algosuite_input_data input;
    input.inp_sample_count = (uint32_t) sample_count;
    input.grn_count = (uint32_t) green;
    input.grn2Cnt = (uint32_t) green2;
    input.irCnt = (uint32_t) ir;
    input.redCnt = (uint32_t) red;
    input.accelx = (uint32_t) acceleration_x;
    input.accely = (uint32_t) acceleration_y;
    input.accelz = (uint32_t) acceleration_z;
    input.whrm_suite_curr_opmode = (uint32_t) operation_mode;
    input.hearth_rate_estim = (uint32_t) hr;
    input.hr_confidence = (uint32_t) hr_confidence;
    input.rr_interbeat_interval = (uint32_t) rr;
    input.rr_confidence = (uint32_t) rr_confidence;
    input.activity_class = (uint32_t) activity;
    input.r_spo2 = (uint32_t) r;
    input.spo2_confidence = (uint32_t) wspo2_confidence;
    input.spo2_estim = (uint32_t) spo2;
    input.spo2_calc_percentage = (uint32_t) wspo2_percentage_complete;
    input.spo2_low_sign_quality_flag = (uint32_t) wspo2_low_snr;
    input.spo2_motion_flag = (uint32_t) wspo2_motion;
    input.spo2_low_pi_flag = (uint32_t) wspo2_low_pi;
    input.spo2_unreliable_r_flag = (uint32_t) wspo2_unreliable_r;
    input.spo2_state = (uint32_t) wspo2_state;
    input.skin_contact_state = (uint32_t) scd_state;
    input.walk_steps = (uint32_t) walk_steps;
    input.run_steps = (uint32_t) run_steps;
    input.kcal = (uint32_t) k_cal;
    input.cadence = (uint32_t) totalActEnergy;
    input.timestampUpper32bit = (uint32_t) timestamp_upper;
    input.timestampLower32bit = (uint32_t) timestamp_lower;

    mxm_algosuite_output_data output;
    mxm_algosuite_return_code status;

    mxm_algosuite_manager_run(&input, &output, &status);

    int result = 0;

    if (initData.enabledAlgorithms == 0) {
        LOGD("RUN: ALGORITHMS NOT ENABLED");
        return ERROR_ALGO_INIT;
    }

    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_HRV) > 0) {
        if (status.hrv_status != MXM_HRV_SUCCESS) {
            LOGD("HRV RUN FAIL");
            result |= MXM_ALGOSUITE_ENABLE_HRV;
        } else {
            env->CallVoidMethod(joutput, updateHrvOutputMethodId,
                                output.hrv_out_sample.timeDomainMetrics.avnn,
                                output.hrv_out_sample.timeDomainMetrics.sdnn,
                                output.hrv_out_sample.timeDomainMetrics.rmssd,
                                output.hrv_out_sample.timeDomainMetrics.pnn50,
                                output.hrv_out_sample.freqDomainMetrics.ulf,
                                output.hrv_out_sample.freqDomainMetrics.vlf,
                                output.hrv_out_sample.freqDomainMetrics.lf,
                                output.hrv_out_sample.freqDomainMetrics.hf,
                                output.hrv_out_sample.freqDomainMetrics.lfOverHf,
                                output.hrv_out_sample.freqDomainMetrics.totPwr,
                                output.hrv_out_sample.percentCompleted,
                                output.hrv_out_sample.isHrvCalculated);
            LOGD("HRV RUN SUCCESS");
        }
    }
    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_RESP) > 0) {
        if (status.resp_status != MXM_RESPIRATION_RATE_MANAGER_SUCCESS) {
            LOGD("RESP RUN FAIL");
            result |= MXM_ALGOSUITE_ENABLE_RESP;
        } else {
            if(output.resp_out_sample.respiration_rate < 0){
                output.resp_out_sample.respiration_rate = 0;
            }else if(output.resp_out_sample.respiration_rate > 1000){
                output.resp_out_sample.respiration_rate = 0;
            }
            env->CallVoidMethod(joutput, updateRespOutputMethodId,
                                output.resp_out_sample.respiration_rate,
                                output.resp_out_sample.confidence_level,
                                output.resp_out_sample.motion_flag,
                                output.resp_out_sample.ibi_low_quality_flag,
                                output.resp_out_sample.ppg_low_quality_flag);
            LOGD("RESP RUN SUCCESS--> rate: %f  conf: %f", output.resp_out_sample.respiration_rate, output.resp_out_sample.confidence_level);
        }
    }
    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_STRESS) > 0) {
        if (status.stress_status != MXM_STRESS_MONITORING_SUCCESS) {
            LOGD("STRESS RUN FAIL");
            result |= MXM_ALGOSUITE_ENABLE_STRESS;
        } else {
            env->CallVoidMethod(joutput, updateStressOutputMethodId,
                                output.stress_out_sample.stress_class,
                                output.stress_out_sample.stress_score,
                                output.stress_out_sample.stress_score_prc);
            LOGD("STRESS RUN SUCCESS");
        }
    }
    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_SLEEP) > 0) {
        if (status.sleep_status != MXM_SLEEP_MANAGER_SUCCESS) {
            LOGD("SLEEP RUN FAIL");
            result |= MXM_ALGOSUITE_ENABLE_SLEEP;
        } else {
            env->CallVoidMethod(joutput, updateSleepOutputMethodId,
                                (int) output.sleep_out_Sample.output_data_arr->sleep_wake_decision_status,
                                (int) output.sleep_out_Sample.output_data_arr->sleep_wake_decision,
                                output.sleep_out_Sample.output_data_arr->sleep_wake_detection_latency,
                                output.sleep_out_Sample.output_data_arr->sleep_wake_output_conf_level,
                                (int) output.sleep_out_Sample.output_data_arr->sleep_phase_output_status,
                                (int) output.sleep_out_Sample.output_data_arr->sleep_phase_output,
                                output.sleep_out_Sample.output_data_arr->sleep_phase_output_conf_level,
                                (int) output.sleep_out_Sample.output_data_arr->encoded_output.sleep_phase,
                                output.sleep_out_Sample.output_data_arr->encoded_output.duration,
                                output.sleep_out_Sample.output_data_arr->encoded_output.needs_storage,
                                output.sleep_out_Sample.output_data_arr->hr,
                                output.sleep_out_Sample.output_data_arr->acc_mag,
                                output.sleep_out_Sample.output_data_arr->interbeat_interval,
                                output.sleep_out_Sample.output_data_arr->sleep_resting_hr,
                                output.sleep_out_Sample.output_data_arr_length,
                                output.sleep_out_Sample.date_info);

            LOGD("SLEEP RUN SUCCESS: %ld", output.sleep_out_Sample.date_info);
        }
    }
    if ((initData.enabledAlgorithms & MXM_ALGOSUITE_ENABLE_SPORTS) > 0) {
        env->CallVoidMethod(joutput, updateSportsOutputMethodId,
                            output.sc_out_sample.session,
                            output.sc_out_sample.percent_completed,
                            output.sc_out_sample.hr_stats.min_hr,
                            output.sc_out_sample.hr_stats.max_hr,
                            output.sc_out_sample.hr_stats.mean_hr,
                            output.sc_out_sample.scores.readiness.readiness_score,
                            output.sc_out_sample.scores.vo2_max.relax,
                            output.sc_out_sample.scores.vo2_max.VO2,
                            output.sc_out_sample.scores.vo2_max.fitness_age,
                            output.sc_out_sample.scores.vo2_max.fitness_region_poor_medium,
                            output.sc_out_sample.scores.vo2_max.fitness_region_medium_good,
                            output.sc_out_sample.scores.vo2_max.fitness_region_good_excellent,
                            output.sc_out_sample.scores.recovery.recovery_time_min,
                            output.sc_out_sample.scores.recovery.epoc,
                            output.sc_out_sample.scores.recovery.hr0,
                            output.sc_out_sample.scores.recovery.last_hr,
                            output.sc_out_sample.scores.recovery.recovery_percentage,
                            output.sc_out_sample.status,
                            output.sc_out_sample.new_output_ready,
                            output.sc_out_sample.timestamp);
        if (status.sports_coaching_status != MXM_SC_NO_ERROR) {
            LOGD("SPORTS RUN FAIL");
            result |= MXM_ALGOSUITE_ENABLE_SPORTS;
        } else {
            LOGD("SPORTS RUN SUCCESS");
        }
    }
    return result;
}



extern "C"
JNIEXPORT jint JNICALL
Java_com_maximintegrated_algorithms_MaximAlgorithms_end(JNIEnv *env, jclass clazz,
                                                        jint disable_flag) {
    mxm_algosuite_return_code status;

    mxm_algosuite_manager_end((unsigned char) disable_flag, &status);

    int result = 0;

    if ((disable_flag & MXM_ALGOSUITE_ENABLE_HRV) > 0) {
        if (status.hrv_status != MXM_HRV_SUCCESS) {
            LOGD("HRV END FAIL");
            result |= MXM_ALGOSUITE_ENABLE_HRV;
        } else {
            LOGD("HRV END SUCCESS");
        }
    }
    if ((disable_flag & MXM_ALGOSUITE_ENABLE_RESP) > 0) {
        if (status.resp_status != MXM_RESPIRATION_RATE_MANAGER_SUCCESS) {
            LOGD("RESP END FAIL");
            result |= MXM_ALGOSUITE_ENABLE_RESP;
        } else {
            LOGD("RESP END SUCCESS");
        }
    }
    if ((disable_flag & MXM_ALGOSUITE_ENABLE_STRESS) > 0) {
        if (status.stress_status != MXM_STRESS_MONITORING_SUCCESS) {
            LOGD("STRESS END FAIL");
            result |= MXM_ALGOSUITE_ENABLE_STRESS;
        } else {
            LOGD("STRESS END SUCCESS");
        }
    }
    if ((disable_flag & MXM_ALGOSUITE_ENABLE_SLEEP) > 0) {
        if (status.sleep_status != MXM_SLEEP_MANAGER_SUCCESS) {
            LOGD("SLEEP END FAIL");
            result |= MXM_ALGOSUITE_ENABLE_SLEEP;
        } else {
            LOGD("SLEEP END SUCCESS");
        }
    }
    if ((disable_flag & MXM_ALGOSUITE_ENABLE_SPORTS) > 0) {
        if (status.sports_coaching_status != MXM_SC_NO_ERROR) {
            LOGD("SPORTS END FAIL");
            result |= MXM_ALGOSUITE_ENABLE_SPORTS;
        } else {
            LOGD("SPORTS END SUCCESS");
        }
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_maximintegrated_algorithms_MaximAlgorithms_getVersion(JNIEnv *env, jclass clazz,
                                                               jobject jversion) {
    mxm_algosuite_version_str versionStr;
    mxm_algosuite_manager_get_versions(&versionStr);

    env->CallVoidMethod(jversion, setVersionMethodId,
                        versionStr.version_string,
                        versionStr.version, versionStr.sub_version, versionStr.sub_sub_version);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_maximintegrated_algorithms_MaximAlgorithms_calculateSQI(JNIEnv *env, jclass clazz,
                                                                 jfloat deep_in_sec,
                                                                 jfloat rem_in_sec,
                                                                 jfloat in_sleep_wake_in_sec,
                                                                 jint number_of_wake_in_sleep) {
    mxm_algosuite_return_code status;
    float sleep_quality_index;

    mxm_algosuite_manager_calculate_SQI(deep_in_sec, rem_in_sec, in_sleep_wake_in_sec,
                                        number_of_wake_in_sleep, &sleep_quality_index, &status);

    return sleep_quality_index;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass outputClass = env->FindClass(
            "com/maximintegrated/algorithms/AlgorithmOutput");

    jclass versionClass = env->FindClass(
            "com/maximintegrated/algorithms/AlgorithmVersion");

    updateHrvOutputMethodId = env->GetMethodID(outputClass, "hrvUpdate",
                                               "(FFFFFFFFFFIZ)V");

    updateRespOutputMethodId = env->GetMethodID(outputClass, "respiratoryUpdate",
                                                "(FFZZZ)V");

    updateStressOutputMethodId = env->GetMethodID(outputClass, "stressUpdate",
                                                  "(ZIF)V");

    updateSleepOutputMethodId = env->GetMethodID(outputClass, "sleepUpdate",
                                                 "(IIIFIIFIIZFFFFIJ)V");

    updateSportsOutputMethodId = env->GetMethodID(outputClass, "sportsUpdate",
                                                 "(IIIIIFFFFFFFIFIIIIZJ)V");

    setVersionMethodId = env->GetMethodID(versionClass, "set",
                                          "([CIII)V");

    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_maximintegrated_algorithms_MaximAlgorithms_getAuthInitials(JNIEnv *env, jclass clazz,
                                                                    jbyteArray auth_inits) {
    jbyte *inits = env->GetByteArrayElements(auth_inits, 0);

    uint8_t out_auth_initials[12];

    mxm_algosuite_manager_getauthinitials((uint8_t *) inits, out_auth_initials);

    jbyteArray out = env->NewByteArray(12);

    env->SetByteArrayRegion(out, 0, 12, (jbyte *) out_auth_initials);

    env->ReleaseByteArrayElements(auth_inits, inits, 0);

    return out;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_maximintegrated_algorithms_MaximAlgorithms_authenticate(JNIEnv *env, jclass clazz,
                                                                 jbyteArray array1,
                                                                 jbyteArray array2) {
    jbyte *arr1 = env->GetByteArrayElements(array1, 0);
    jbyte *arr2 = env->GetByteArrayElements(array2, 0);

    env->ReleaseByteArrayElements(array1, arr1, 0);
    env->ReleaseByteArrayElements(array2, arr2, 0);

    int ret = mxm_algosuite_manager_authenticate((uint8_t *) arr1, (uint8_t *) arr2);

    return ret;
}