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
* @file mxm_hrv_public.h
* @date November 2019
* @brief Maxim HRV Module public API header file
*/

/**
* @defgroup mxm_hrv_public Maxim HRV Public Interface
* @brief    Public interface of the heart rate variability module
*/

#ifndef __MXM_HRV_PUBLIC_H__
#define __MXM_HRV_PUBLIC_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <stdbool.h>
#include <stdint.h>

/**
* @ingroup mxm_hrv_public
* @brief   HRV top level module return codes
*/
typedef enum _MxmHrvRet {
    MXM_HRV_SUCCESS,
    MXM_HRV_NULL_PTR_ERROR,
    MXM_HRV_INVALID_CONFIG_ERROR,
    MXM_HRV_NON_POSITIVE_SAMPLING_PERIOD_ERROR,
    MXM_HRV_IBI_PREP_ERROR,
    MXM_HRV_METRIC_CALC_ERROR,
    MXM_HRV_ALREADY_INITIALIZED_ERR,
    MXM_HRV_NOT_INITIALIZED_ERR
} MxmHrvRet;

/**
* @ingroup mxm_hrv_public
* @brief   HRV Module configuration package
*/
typedef struct _MxmHrvConfig {
    float samplingPeriod;          /**< Sampling (clock) period in ms         */
    uint16_t windowSizeInSec;      /**< HRV metric calculation window in sec  */
    uint16_t windowShiftSizeInSec; /**< HRV metric calculation window shift size
                                       in sec                                 */
} MxmHrvConfig;

/**
* @ingroup mxm_hrv_public
* @brief   Input data structure for running the HRV Module
*/

/**
* @ingroup mxm_hrv_public
* @brief   Time domain HRV metrics structure
*/
typedef struct _TimeDomainHrvMetrics {
    float avnn;    /**< Average of NN intervals in ms */
    float sdnn;    /**< Standard deviation of NN intervals in ms */
    float rmssd;    /**< RMS value of successive differences in ms */
    float pnn50;    /**< Percentage of successive differences greater than 50 ms */
} TimeDomainHrvMetrics;

/**
* @ingroup mxm_hrv_public
* @brief   Frequency domain HRV metrics structure
*/
typedef struct _FreqDomainHrvMetrics {
    float ulf;    /**< Power in ULF band (ms^2) */
    float vlf;    /**< Power in VLF band (ms^2) */
    float lf;    /**< Power in LF band (ms^2) */
    float hf;    /**< Power in HF band (ms^2) */
    float lfOverHf;    /**< LF/HF ratio */
    float totPwr;    /**< Total power (ms^2) */
} FreqDomainHrvMetrics;

/**
* @ingroup mxm_hrv_public
* @brief   HRV metric calculator output data structure
*/
typedef struct _MxmHrvOutData {
    TimeDomainHrvMetrics timeDomainMetrics;    /**< Time domain HRV metrics */
    FreqDomainHrvMetrics freqDomainMetrics;    /**< Frequency domain HRV metrics */
    int percentCompleted;    /**< 0 to 100 Progress indicator  */
    bool isHrvCalculated;    /**< Flag that indicates if the content of the output is valid */
} MxmHrvOutData;



#ifdef __cplusplus    /* If this is a C++ compiler, use C linkage */
}
#endif

#endif    /* __MXM_HRV_PUBLIC_H__ */

