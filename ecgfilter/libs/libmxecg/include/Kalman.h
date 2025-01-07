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

#if defined _WIN32 || defined __CYGWIN__
#define ECG_PRIVATE
#else
#define ECG_PRIVATE __attribute__ ((visibility ("hidden")))
#endif

#ifndef KALMAN_FILTER
#define KALMAN_FILTER

#include "matrixOperations.h"

#define K_N 1
#define K_M 2

typedef struct _kalman_instance {
	float dt;  //time interval between measurements;
	float x[K_M];  //state vector
	float y[K_N];  //prediction error
	float z[K_N];  //observation
	float F[K_M*K_M];  //state transition matrix
	float F_tr[K_M*K_M];  //transpose of state transition matrix
	float H[K_N*K_M];  //measurement matrix
	float H_tr[K_N*K_M];  //transpose of measurement matrix
	float P[K_M*K_M];  //uncertainity
	float Q_base[K_M*K_M];  //Base process noise covariance
	float Q[K_M*K_M];  //Dynamic process noise covariance
	float R[K_N*K_N];  //Measurement noise covariance
	float K[K_M*K_N];  //Kalman gain
	float I[K_M*K_M];  //identity matrix
} kalman_instance;

void ECG_PRIVATE kalmanInit(kalman_instance *kalman, float dt);

void ECG_PRIVATE kalmanPredict(kalman_instance *kalman);

void ECG_PRIVATE kalmanUpdate(kalman_instance *kalman, float z[]);

void ECG_PRIVATE scaleDynamicQ(kalman_instance *kalman);

#endif
#ifdef __cplusplus // If this is a C++ compiler, use C linkage
}
#endif
