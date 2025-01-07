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

#ifndef IIR_INCLUDE
#define IIR_INCLUDE

#if defined _WIN32 || defined __CYGWIN__
#define ECG_PRIVATE
#else
#define ECG_PRIVATE __attribute__ ((visibility ("hidden")))
#endif

#define IIR_ORDER 9
#include <stdio.h>

typedef struct iir_inst_ {
	float b[IIR_ORDER];
	float a[IIR_ORDER];
	float input_hist[IIR_ORDER];
	float output_hist[IIR_ORDER];  //history of inputs & outputs
	int order;  // filter order - make sure: filter_order <= IIR_MAX_ORDER
	int groupDelay;   //aprox. group delay in pass band (delay unit sample count)
	int clk;  //filter tap index counter
}iir_inst;

iir_inst ECG_PRIVATE *iir_get_instance(int Fs, int type, int freq);
iir_inst ECG_PRIVATE *iir_get_dc_notch(int type);
void ECG_PRIVATE iir_filter_init(iir_inst *filter_inst, float init_val);
float ECG_PRIVATE iir_filter(float x, iir_inst *filter_inst);
//void iir_filter_exit();
#endif

#ifdef __cplusplus // If this is a C++ compiler, use C linkage
}
#endif

