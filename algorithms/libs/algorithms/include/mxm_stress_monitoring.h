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
********************************************************************************
*/

#ifndef __MXM_STRESS_MONITORING_H__
#define __MXM_STRESS_MONITORING_H__

#ifdef __cplusplus /* If this is a C++ compiler, use C linkage */
extern "C" {
#endif

/**
  * @file mxm_stress_monitoring.h
  * @public
  * @copyright Copyright/licensing notice (see Legal and Copyright Notices)
  */

/**
  * @defgroup mxm_stress_monitoring Maxim Stress Monitoring Module
  * @brief    Stress Monitoring Module
  *
  * This module presents the Maxim Stress Monitoring Module, API related
  * function definitions and declarations.
  */

#include <stdbool.h>
#include <stdint.h>

typedef bool boolean_t;     /**< Boolean quantity        */
typedef float float32_t;    /**< Signed 32 bits quantity */

/**
  * @public
  * @ingroup mxm_stress_monitoring
  * @brief Maxim Stress Monitoring Module return codes
  */
typedef enum _mxm_stress_monitoring_return_code {

    MXM_STRESS_MONITORING_SUCCESS = 0,        /**< SUCCESS code               */
    MXM_STRESS_MONITORING_NULL_PTR_ERR,       /**< NULL pointer error         */
    MXM_STRESS_MONITORING_INVALID_CONFIG_ERR, /**< Configuration error        */
    MXM_STRESS_MONITORING_RUN_ERR,            /**< RUN error                  */

} mxm_stress_monitoring_return_code;

/**
  * @public
  * @ingroup mxm_stress_monitoring
  * @brief Maxim Stress Monitoring Module configuration structure
  */
typedef struct _mxm_stress_monitoring_config {

    uint8_t dummy_config_for_compilation;
} mxm_stress_monitoring_config;


/**
  * @public
  * @ingroup mxm_stress_monitoring
  * @brief   Specifies the output structure for ::mxm_stress_monitoring_run
  *          function
  */
typedef struct _mxm_stress_monitoring_run_output {

    boolean_t stress_class; /**< Binary stress/non-stress output */
    uint8_t stress_score;   /**< Integer stress score output in interval [0 18]:
                                 <br>
                                 Scores [0 8]: Represent stressful scores from
                                 highest to lowest levels where parasympathetic
                                 system is dominant                         <br>
                                 Scores [9 18]: Represent non-stressful scores
                                 from highest to lowest levels where sympathetic
                                 system is dominant                           */
    float32_t stress_score_prc;   /**< Stress score output as percentage      */

} mxm_mxm_stress_monitoring_run_output;



#ifdef __cplusplus /* If this is a C++ compiler, use C linkage */
}
#endif

#endif    /* #define __MXM_STRESS_MONITORING_H__ */
