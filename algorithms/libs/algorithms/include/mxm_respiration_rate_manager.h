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

/**
* @file mxm_respiration_rate_manager.h
* @date JUN 2019
* @brief Maxim Respiration Rate Measurement public API header file
*/

/**
* @defgroup respiration_rate_manager Respiration Rate Measurement API Module
* @brief    Defines Respiration Rate Measurement Public API.
*
* This module is in charge of handling Respiration Rate Measurement API
* related tasks and definitions.
*/

#ifndef _MXM_RESPIRATION_RATE_MANAGER_H_
#define _MXM_RESPIRATION_RATE_MANAGER_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
#include <stdbool.h>

    typedef bool boolean_t;     /**< Boolean quantity        */

    /**
    * @public
    * @ingroup respiration_rate_manager
    * @brief   Respiration rate return options
    */
    typedef enum _mxm_repiration_rate_manager_return_code {
        MXM_RESPIRATION_RATE_MANAGER_SUCCESS,    /**< Success return code */
        MXM_RESPIRATION_RATE_MANAGER_INIT_NULL_PTR_ERROR,    /**< NULL pointer error (during initialization) return code */
        MXM_RESPIRATION_RATE_MANAGER_INIT_ERROR,    /**< error (during initialization) return code */
        MXM_RESPIRATION_RATE_MANAGER_RUN_NULL_PTR_ERROR,    /**< NULL pointer error (during execution) return code */
        MXM_RESPIRATION_RATE_MANAGER_RUN_ERROR,
        MXM_RESPIRATION_RATE_MANAGER_END_NULL_PTR_ERROR,    /**< NULL pointer error (during termination) return code */
        MXM_RESPIRATION_RATE_MANAGER_END_ERROR,
        MXM_RESPIRATION_RATE_MANAGER_GET_VERSION_NULL_PTR_ERROR,    /**< NULL pointer error (during termination) return code */
    } mxm_respiration_rate_manager_return_code;

    /**
    * @public
    * @ingroup respiration_rate_manager
    * @brief   Respiration rate LED Codes
    *
    * This enumeration type is useful for identifying correct array indices for multiple LED channels.
    */
    typedef enum _mxm_respiration_rate_manager_led_codes {
        MXM_RESPIRATION_RATE_MANAGER_GREEN_LED,    /**< GREEN channel */
        MXM_RESPIRATION_RATE_MANAGER_IR_LED,       /**< IR channel */
        MXM_RESPIRATION_RATE_MANAGER_RED_LED,      /**< RED channel */
    } mxm_respiration_rate_manager_led_codes;

    /**
    * @public
    * @ingroup respiration_rate_manager
    * @brief   Respiration rate ppg source options
    */
    typedef enum _mxm_respiration_rate_manager_ppg_source_options {
        MXM_RESPIRATION_RATE_MANAGER_PPG_SOURCE_WRIST, /**< The algorithm will work with the PPG gathered from wrist. */
        MXM_RESPIRATION_RATE_MANAGER_PPG_SOURCE_FINGER /**< The algorithm will work with the PPG gathered from finger. */
    } mxm_respiration_rate_manager_ppg_source_options;

    /**
    * @public
    * @ingroup respiration_rate_manager
    * @brief   Respiration rate available sampling rate options
    */
    typedef enum _mxm_respiration_rate_manager_sampling_rate_option {
        MXM_RESPIRATION_RATE_MANAGER_SAMPLING_RATE_25_HZ = 25,   /**< 25 Hz sampling rate */
        MXM_RESPIRATION_RATE_MANAGER_SAMPLING_RATE_100_HZ = 100   /**< 100 Hz sampling rate */
    } mxm_respiration_rate_manager_sampling_rate_option;

    /**
    * @public
    * @ingroup respiration_rate_manager
    * @brief   Respiration rate software initialization structure
    */
    typedef struct _mxm_respiration_rate_manager_init_str {
        mxm_respiration_rate_manager_ppg_source_options signal_source_option; /**< The location of PPG signal source*/
        mxm_respiration_rate_manager_led_codes led_code; /**< The color/wavelength of PPG signal source*/
        mxm_respiration_rate_manager_sampling_rate_option sampling_rate; /**< Input PPG sampling rate*/
        uint32_t motion_magnitude_limit; /**< The motion magnitude limit in milig*/
    } mxm_respiration_rate_manager_init_str;


    /**
    * @public
    * @ingroup respiration_rate_manager
    * @brief   Output data structure for running respiration rate estimator
    */
    typedef struct _mxm_respiration_rate_manager_out_data_str {
        float respiration_rate; /**< Output respiration rate with unit 'Breaths/Minute'*/
        float confidence_level; /**< Confidence level of the output in range of 0-100*/
        boolean_t motion_flag; /**< True if motion detected*/
        boolean_t ibi_low_quality_flag; /**< True if received IBI values are recieved with low quailty */
        boolean_t ppg_low_quality_flag; /**< True if input PPG signal is noisy (DC Jumps, spikes)*/
    } mxm_respiration_rate_manager_out_data_str;




#ifdef __cplusplus
}
#endif

#endif /* _MXM_RESPIRATION_RATE_MANAGER_H_ */
