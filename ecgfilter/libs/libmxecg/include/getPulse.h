/*******************************************************************************
* Copyright (C) 2015 Maxim Integrated Products, Inc., All rights Reserved.
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

#ifndef _GET_PULSE_
#define _GET_PULSE_

#if defined _WIN32 || defined __CYGWIN__
#define ECG_PRIVATE
#else
#define ECG_PRIVATE __attribute__ ((visibility ("hidden")))
#endif

#include <stdlib.h>
#include <string.h>
#include "fir_filter.h"
#include "statistics.h"

typedef struct _pulse_instance{
	unsigned long prevPeak;
	unsigned long lastIncrease;
	unsigned long trainingStart;
	float signalLevel;
	float signalThr;
	float noiseLevel;
	float noiseThr;
	unsigned int movingWindowSize;
	unsigned int filterDelay;
	bool pulseFlag;
	bool inADC;
	float edgeFilter[5];
	long trainingPeakLocs[10];
	float *movingAverager;
	float *pulseInterval;
}pulse_instance;

pulse_instance ECG_PRIVATE *get_pulse_instance();

void ECG_PRIVATE pulse_init(int Fs, bool inADC);

float ECG_PRIVATE get_ECG_pulse(float ecgIn, unsigned long clk, int Fs);

void ECG_PRIVATE delete_pulse_instance();

#endif
#ifdef __cplusplus // If this is a C++ compiler, use C linkage
}
#endif
