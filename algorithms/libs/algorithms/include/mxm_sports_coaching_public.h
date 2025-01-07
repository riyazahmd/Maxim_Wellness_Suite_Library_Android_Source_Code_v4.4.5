/*******************************************************************************
* Copyright (C) Maxim Integrated Products, Inc., All rights Reserved.
*
* This software is protected by copyright laws of the United States and
* of foreign countries. This material may also be protected by patent laws
* and technology transfer regulations of the United States and of foreign
* countries. This software is furnished under a license agreement and/or a
* nondisclosure agreement and may only be used or reproduced in accordance
* with the terms of those agreements. Dissemination of this information to
* any party or parties not specified in the license agreement and/or
* nondisclosure agreement is expressly prohibited.
*
* The above copyright notice and this permission notice shall be included
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL MAXIM INTEGRATED BE LIABLE FOR ANY CLAIM, DAMAGES
* OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
* ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
* OTHER DEALINGS IN THE SOFTWARE.
*
* Except as contained in this notice, the name of Maxim Integrated
* Products, Inc. shall not be used except as stated in the Maxim Integrated
* Products, Inc. Branding Policy.
*
* The mere transfer of this software does not imply any licenses
* of trade secrets, proprietary technology, copyrights, patents,
* trademarks, maskwork rights, or any other form of intellectual
* property whatsoever. Maxim Integrated Products, Inc. retains all
* ownership rights.
*******************************************************************************
*/



#ifdef __cplusplus // If this is a C++ compiler, use C linkage
extern "C" {
#endif


#ifndef __MXM_SPORTS_COACHING_PUBLIC_H__
#define __MXM_SPORTS_COACHING_PUBLIC_H__

#include <stdbool.h>

/**
* @ingroup mxm_sports_coaching
*/
/**@{*/
#define MXM_SC_MAX_HISTORY_LENGTH 32 //  /**< Maximum History Buffer Size */
/**@}*/


/**
* @ingroup mxm_sports_coaching
* @brief   Gender enumaration
*/
typedef enum _mxm_sc_gender {
    MXM_SC_MALE = 0, /**< Male */
    MXM_SC_FEMALE, /**< Female */
} mxm_sc_gender;


/**
* @ingroup mxm_sports_coaching
* @brief   User information structure
*/
typedef struct _mxm_sc_user_info {
    int birth_day;
    int birth_month;
    int birth_year;
    mxm_sc_gender gender;
    int weight;
    int height;
    bool is_metric;
} mxm_sc_user_info;


/**
* @ingroup mxm_sports_coaching
* @brief   Session code enumaration
*/
typedef enum _mxm_sc_session {
    MXM_SC_UNDEFINED = 0,
    MXM_SC_VO2MAX_RELAX, /**< Algorithm will return users VO2MAX_Relax value. The user shall be relax during gathering data */
    MXM_SC_VO2, /**< Algorithm will return users VO2 values*/
    MXM_SC_RECOVERY_TIME, /**< Algorithm will return users recovery time */
    MXM_SC_READINESS, /**< Algorithm will return users readiness for a training */
    MXM_SC_VO2MAX_FROM_HISTORY, /**< Algorithm will return users VO2MAX values using given user history*/
    MXM_SC_EPOC_RECOVERY /**< Algorithm will return users EPOC value and recovery time*/
} mxm_sc_session;

/**
* @ingroup mxm_sports_coaching
* @brief   EPOC Recovery mode related configuration
*/
typedef struct _mxm_sc_epoc_config {
    int exercise_duration_minutes;    /**< Exercise recovery input - only needed in EPOC_RECOVERY mode - Can be any number > 0, Current options in app are 5 (<5), 20 (<20), 60 (<60), 90 (>60) */
    int exercise_intensity;            /**< Exercise recovery input - only needed in EPOC_RECOVERY mode - Curent Scale: 0 (no exercise), 1 (light), 2 (moderate), 3(heavy)*/
    int minutes_after_exercise;       /**< Exercise recovery input - only needed in EPOC_RECOVERY mode - Options in available in app: 0 (<0.5), 1 (<1), 2 (2<), 3(>2)*/
} mxm_sc_epoc_config;

/**
* @ingroup mxm_sports_coaching
* @brief   Recovery time mode related configuration
*/
typedef struct _mxm_sc_recovery_time_config {
    unsigned long long int last_epoc_recovery_timestamp;  /**< Exercise recovery input - only needed in RECOVERY_TIME mode - Epoch timestamp of last EPOC measurement (in miliseconds) */
    int last_recovery_estimate_in_mins;    /**< Exercise recovery input - only needed in RECOVERY_TIME mode - last recovery estimate */
    int last_hr;    /**< Exercise recovery input - only needed in RECOVERY_TIME mode - last HR measured during latest EPOC measurement */
} mxm_sc_recovery_time_config;


/**
* @ingroup mxm_sports_coaching
* @brief   VO2max outputs structure
*/
typedef struct _mxm_sc_vo2max_output {
    float relax;     /**< VO2max relax */
    float VO2;       /**< VO2 score (VO2 during measurement - not max VO2 capacity */
    float fitness_age; /**<  age estimate based on VO2max score */
    float fitness_region_poor_medium; /**< Border between Poor and Medium VO2max levels,
                                   adjusted by user information*/
    float fitness_region_medium_good; /**< Border between Medium and Good VO2max levels,
                                   adjusted by user information*/
    float fitness_region_good_excellent; /**< Border between Good and Excellent VO2max levels,
                                   adjusted by user information*/
} mxm_sc_vo2max_output;


/**
* @ingroup mxm_sports_coaching
* @brief   Recovery outputs structure
*/
typedef struct _mxm_sc_recovery_output {
    int recovery_time_min;
    float epoc; /**< Excess Post-Exercise Oxygen Consumption in ml/kg */
    int hr0; /**< Estimated HR at the end of tarining activity*/
    int last_hr; /**< Last HR value in EPOC estimation window */
    int recovery_percentage; /**< Users recovery state in percentage */
} mxm_sc_recovery_output;


/**
* @ingroup mxm_sports_coaching
* @brief   Readiness outputs structure
*/
typedef struct _mxm_sc_readiness_output {
    float readiness_score; /**< Readiness for next exercise. Between 0-100%*/
} mxm_sc_readiness_output;


/**
* @ingroup mxm_sports_coaching
* @brief   Output structure with combined scores
*/
typedef struct _mxm_sc_estimate_output {
    mxm_sc_readiness_output readiness; /**<Output of readiness calculaation features */
    mxm_sc_vo2max_output vo2_max; /**<Output of the VO2MAX calculation features */
    mxm_sc_recovery_output recovery; /**<The output of the EPOC and recovery features */
} mxm_sc_estimate_output;


/**
* @ingroup mxm_sports_coaching
* @brief   Error code enumaration
*/
typedef enum _mxm_sc_return_code {
    MXM_SC_NO_ERROR = 0, /**< The software executed without error.*/
    MXM_SC_PROCESSING_ERROR, /**< The software had an error during execution */
    MXM_SC_INPUT_ERROR, /**< One or more items in the input prevent the execution*/
    MXM_SC_NULL_PTR_ERROR, /**< One or more arguments are null */
} mxm_sc_return_code;


/**
* @ingroup mxm_sports_coaching
* @brief   Status code enumaration
*/
typedef enum _mxm_sc_status {
    MXM_SC_NO_INPUT = 0, /**<Software hasn't received a valid input yet */
    MXM_SC_PROGRESS, /**< Software is progressing with the given input */
    MXM_SC_SUCCESS, /**< The output has been calculated with success */
    MXM_SC_FAILURE /**< Algorithm could not find a result for the given input */
} mxm_sc_status;

/**
* @ingroup mxm_sports_coaching
* @brief   struct to keep heart rate statistics for history records
*/
typedef struct _mxm_sc_hr_stats {
    int min_hr;
    int max_hr;
    int mean_hr;
} mxm_sc_hr_stats;

/**
* @ingroup mxm_sports_coaching
* @brief   Main output structure with combined scores, HR, HRV and completion
*/
typedef struct _mxm_sc_output {
    int percent_completed; /**< The state of the algorithm between 0% and 100% */
    mxm_sc_session session; /**< Current session of the algorihm*/
    mxm_sc_hr_stats hr_stats; /**< Heart rate statistics calculated during the active session*/
    mxm_sc_estimate_output scores; /**< Users score calculated by the algorithm*/
    mxm_sc_status status; /**< Status of the algorithm */
    bool new_output_ready; /**< Flag to indicate if algorithm reports a new output */
    unsigned long long int timestamp; /**< Epoch timestamp of the output in miliseconds */
} mxm_sc_output;

/**
* @ingroup mxm_sports_coaching
* @brief   User History Item
*/
typedef struct _mxm_sc_user_history_record_item {
    unsigned long long int record_date; /**< Epoch timestamp of the record in miliseconds */
    mxm_sc_estimate_output score; /**< Estimated scores at the output of the algorithm when algorithms and with success */
    mxm_sc_hr_stats heart_rate_stat; /**< Heart rate statistics at the output of the algorithm when algorithms and with success */
    mxm_sc_session session; /**< Session type of the record */
} mxm_sc_user_history_record_item;



/**
* @ingroup mxm_sports_coaching
* @brief   User History for the last month, recorded and read by the wrapper and passed to the algorithm
*/
typedef struct _mxm_sc_user_history {
    int number_of_records; /**< Number of existing history items in the records array */
    mxm_sc_user_history_record_item records[MXM_SC_MAX_HISTORY_LENGTH]; /**<Array to store user history items*/
} mxm_sc_user_history;



/**
* @ingroup mxm_sports_coaching
* @brief   Sports Coaching General Configuration Parameters
*/
typedef struct _mxm_sc_config {
    int sampling_rate;
    mxm_sc_session session_code;  /**< Selected mode for current session */
    mxm_sc_user_info user;
    mxm_sc_user_history history;
    mxm_sc_epoc_config epoc_mode_config; /**< EPOC mode related configuration. Inactive in other modes */
    mxm_sc_recovery_time_config recovery_time_mode_config; /**< Recovery time mode related configuration. Inactive in other modes */
} mxm_sc_config;

#endif // __MXM_SPORTS_COACHING_PUBLIC_H__

#ifdef __cplusplus /* If this is a C++ compiler, use C linkage */
}
#endif