/*******************************************************************************
* Copyright (C) 2019 Maxim Integrated Products, Inc., All Rights Reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a
* copy of this software and associated documentation files (the "Software"),
* to deal in the Software without restriction, including without limitation
* the rights to use, copy, modify, merge, publish, distribute, sublicense,
* and/or sell copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following conditions:
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
********************************************************************************
*/

/**
* @file mxm_sleep_manager.h
* @date January 2019
* @brief Maxim Sleep Quality Assessment Manager public API header file
*/

/**
* @defgroup sleepQAManager Maxim Sleep Quality Assessment Manager Module
* @brief    Includes Maxim Sleep Quality Assessment Manager API related
*           definitions.
*
* This module is in charge of handling Maxim Sleep Quality Assessment Manager API
* related tasks and definitions.
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef _SLEEP_MANAGER_H_
#define _SLEEP_MANAGER_H_

#include <stdint.h>
#include <stdbool.h>



    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Sleep/Wake Detection Duration Config parameter
    *
    * This enumaration helps to decide Sleep/Wake detection duration config parameter to
    * determine sleep or wake regarding to last N minutes.
    */
    //TODO:Config selection guide
    typedef enum _mxm_sleep_manager_minimum_detectable_sleep_duration {
        MXM_SLEEP_MANAGER_MINIMUM_SLEEP_30_MIN = 30, /**<Detect sleeps more than 30 min.
                                                     Initial Latency 30 min.*/

        MXM_SLEEP_MANAGER_MINIMUM_SLEEP_40_MIN = 40, /**<<Detect sleeps more than 40 min.
                                                 Initial Latency 40 min. */

        MXM_SLEEP_MANAGER_MINIMUM_SLEEP_50_MIN = 50, /**<Detect sleeps more than 50 min.
                                                 Initial Latency 50 min. */

        MXM_SLEEP_MANAGER_MINIMUM_SLEEP_60_MIN = 60,/**<Detect sleeps more than 60 min.
                                               Initial Latency 60 min.  */
    }mxm_sleep_manager_minimum_detectable_sleep_duration;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Subject's Gender option
    *
    * This enumaration helps to define subject's gender
    */
    typedef enum _mxm_sleep_manager_gender {
        MXM_MALE,
        MXM_FEMALE
    } mxm_sleep_manager_gender;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager user info
    *
    */
    typedef struct _mxm_sleep_user_info {

        uint16_t age;       /**< Age of the user to set (in years)*/
        uint16_t weight;    /**< Weight of the user to set (in kg) */
        mxm_sleep_manager_gender gender;    /**< Gender of the user to set, 0:Male 1:Female */
        float sleep_resting_hr; /**< Resting HR of the user. Needs to be set if is_resting_hr_available is true*/

    } mxm_sleep_user_info;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Config strucutre
    */
    typedef struct _mxm_sleep_manager_config
    {

        mxm_sleep_manager_minimum_detectable_sleep_duration mxm_sleep_detection_duration; /**<
                                                                                  Minimum sleep duration allowed*/
        mxm_sleep_user_info user_info; /**< User info including age,gender,weight*/

        bool is_resting_hr_available;/**< Availability of the resting HR of the subject*/

        bool is_confidence_level_available_hr; /**< Availability of the confidence level for the HR measurement*/
        bool is_confidence_level_available_ibi; /**< Availability of the confidence level for the IBI measruement*/
        bool is_activity_available;/**< Availability of the activity identifier*/
    } mxm_sleep_manager_config;

   /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Sleep Phase Output Status
    *
    * This enumaration indicates the sleep phase output's readiness information
    */
    typedef enum _mxm_sleep_manager_sleep_phase_output_status {
        MXM_SLEEP_MANAGER_PHASE_NOT_CALCULATED = 0, /**< If the Sleep Phase Decision is not ready */
        MXM_SLEEP_MANAGER_PHASE_READY = 1,          /**< If the Sleep Phase Decision is ready */
    } mxm_sleep_manager_sleep_phase_output_status;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager return types
    */
    typedef enum _mxm_sleep_manager_return {
        MXM_SLEEP_MANAGER_SUCCESS,                  /**< Success return code */
        MXM_SLEEP_MANAGER_INIT_NULL_PTR_ERROR,      /**< NULL pointer error (during initialization) return code */
        MXM_SLEEP_MANAGER_INIT_INVALID_INPUT_ERROR, /**< Invalid Input error return code */
        MXM_SLEEP_MANAGER_ALGO_INIT_ERROR,          /**< Algorithm module initialization error return code */
        MXM_SLEEP_MANAGER_ALGO_END_ERROR,           /**< Algorithm module termination error return code */
        MXM_SLEEP_MANAGER_RUN_NULL_PTR_ERROR,       /**< NULL pointer error (during execution) return code */
        MXM_SLEEP_MANAGER_RUN_ALGO_ERROR,           /**< Algorithm execution error return code */
        MXM_SLEEP_MANAGER_RUN_ALGO_AGE_OR_GENDER_NOT_DEFINED, /**< Age or gender is not defined return code */
    } mxm_sleep_manager_return;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Sleep Phase Output while subject is in sleep
    *
    * This enumaration indicates the subject's sleep phase output while the subject is in sleep
    */
    typedef enum _mxm_sleep_manager_sleep_phase_output {
        MXM_SLEEP_MANAGER_SP_UNDEFINED = -1,  /**< If Sleep Phases Output is not ready*/
		MXM_SLEEP_MANAGER_SP_WAKE 	   = 0,
        MXM_SLEEP_MANAGER_REM 		   = 2,            /**< Subject sleeps in in REM stage */
        MXM_SLEEP_MANAGER_LIGHT 	   = 3,              /**< Subject sleeps in in LIGHT stage */
        MXM_SLEEP_MANAGER_DEEP 		   = 4               /**< Subject sleeps in in DEEP stage */
    } mxm_sleep_manager_sleep_phase_output;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Sleep/Wake Indicator
    *
    * This enumaration is a flag indicating whether the sleep-wake decision is updated or not. Moreover
    * the sleep-wake-output status is set to NOT_CALCULATED during the first N minutes (initialization period)
    * defined by @ref _mxm_sleep_manager_detection_duration_config
    */
    typedef enum _mxm_sleep_manager_sleep_wake_output_status {
        MXM_SLEEP_MANAGER_NOT_CALCULATED = 0,  /**< If the Sleep/Wake Decision is not ready */
        MXM_SLEEP_MANAGER_CALCULATED = 1,      /**< If the Sleep/Wake Decision is made */
    } mxm_sleep_manager_sleep_wake_output_status;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Sleep Wake Output
    *
    * This enumaration indicates the subject's sleep status of the user.
    */
    typedef enum _mxm_sleep_manager_sleep_wake_output {
        MXM_SLEEP_MANAGER_WAKE = 0,   /**< While subject is in sleep, short-term decision for wake */
        MXM_SLEEP_MANAGER_RESTLESS,   /**< While subject is in sleep, short-term decision for restless.
                                      The restless state is corresponds inconveient portins during sleep rooting
                                      from small and non-periodic wiggles.*/
        MXM_SLEEP_MANAGER_SLEEP       /**< While subject is in sleep, short-term decision for sleep */
    } mxm_sleep_manager_sleep_wake_output;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Sleep Algorithm Encoded Output
    *
    * This structure keeps encoded output to keep sleep output with minimal memory usage.
    * The user can get a summary of subjects sleep by keeping only the encoded outputs with needs_storage flag is true
    */
    typedef struct _mxm_sleep_manager_sleep_encoded_output {
        mxm_sleep_manager_sleep_phase_output sleep_phase; /**< Sleep phase */
        int duration;  /**< Duration of the given sleep phase*/
        bool needs_storage; /**< The given phase and duration needs to be stored in memory*/
    } mxm_sleep_manager_sleep_encoded_output;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Output Data Structure
    */
    typedef struct _mxm_sleep_manager_output_data_str {

        // Sleep/Wake Classification related outputs
        mxm_sleep_manager_sleep_wake_output_status sleep_wake_decision_status; /**< Update flag of the sleep_wake_decision*/
        mxm_sleep_manager_sleep_wake_output sleep_wake_decision; /**< Sleep/Wake Classification output */
        short int sleep_wake_detection_latency; /** in minutes */
        float sleep_wake_output_conf_level;    /**< Sleep/Wake Classification output confidence level*/

        // Sleep Phases Classification related outputs
        mxm_sleep_manager_sleep_phase_output_status sleep_phase_output_status; /**< Sleep Phases Output status */
        mxm_sleep_manager_sleep_phase_output sleep_phase_output; /**< Sleep Phases Classification output */
        float sleep_phase_output_conf_level;    /**< Sleep Phases Classification output confidence level*/

        mxm_sleep_manager_sleep_encoded_output encoded_output; /**< The encoded output of SLeep Algorithm*/

        float hr;                               /**< HR value processed by SleepQA Algorithm */
        float acc_mag;                          /**< Accelerometer Magnitude value processed by SleepQA Algorithm */
        float interbeat_interval;               /**< Inter-beat Interval value processed by SleepQA Algorithm */
        float sleep_resting_hr;                     /**< Resting HR calculated at the end of the nigth. Required as an input for next initialization*/

    } mxm_sleep_manager_output_data_str;

    /**
    * @public
    * @ingroup sleepQAManager
    * @brief   Sleep Quality Assessment Manager Output DataFrame
    */
    typedef struct _mxm_sleep_manager_output_dataframe {
        mxm_sleep_manager_output_data_str * output_data_arr; /**< Array of outputs corresponding to the input array of observations*/

        unsigned int output_data_arr_length; /**< Length of the outputs*/

        uint64_t date_info;                                 /**< Date Info is the time for the initial
                                                                    mxm_sleep_manager_input_data_str unix timestamp*/
    }mxm_sleep_manager_output_dataframe;




#endif /* SLEEP_MANAGER_H_ */

#ifdef __cplusplus // If this is a C++ compiler, use C linkage
}
#endif
