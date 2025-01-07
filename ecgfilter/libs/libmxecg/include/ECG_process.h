/**
* Copyright (C) 2015 Maxim Integrated Products, Inc., All rights Reserved.
* * This software is protected by copyright laws of the United States and
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

#ifndef ECG_PROCESS_INCLUDE
#define ECG_PROCESS_INCLUDE

#if defined _WIN32 || defined __CYGWIN__
#define ECG_PRIVATE
#else
#define ECG_PRIVATE __attribute__ ((visibility ("hidden")))
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#include "ECG_API.h"
#include "iir_filter.h"
#include "adaptive_filter.h"
#include "fir_filter.h"
#include "hrv.h"
#include "getPulse.h"

#define UNSHARP_FILTER
#define DC_NOTCH

#define KALMAN

#if defined (KALMAN)
#include "Kalman.h"
#endif

typedef struct _ecg {
	unsigned long clk;  //algorithm cycle index
	unsigned long lastPeak;
	float HR_inst;
#if defined (KALMAN)
	float HR_filtered;
	bool kalmanActive;
#endif
	unsigned int bufferSize;
	float *ecgBuffer;
#if defined(UNSHARP_FILTER)
	unsigned int windowSize;
	float *movingWindow;
#endif
}ecgInstance;

ecgInstance ECG_PRIVATE *ecg_get_instance();
#endif

#ifdef __cplusplus // If this is a C++ compiler, use C linkage
}
#endif
