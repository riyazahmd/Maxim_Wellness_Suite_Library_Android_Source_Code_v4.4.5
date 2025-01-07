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

#ifndef __SORT__
#define __SORT__

#if defined _WIN32 || defined __CYGWIN__
#define ECG_PRIVATE
#else
#define ECG_PRIVATE __attribute__ ((visibility ("hidden")))

#endif
#include<string.h>  //for memcpy
#include<stdbool.h>

//void bSort(float out[], float in[], int length, bool option); //buble sort
//void bSort(float inOut[], int length, bool option);  //this sorts in place

//void csSort(float out[], float in[], int length, bool option); //coctail shaker sort
//void csSort(float inOut[], int length, bool option); //coctail shaker sorting in place

//void mSort(float out[], float in[], unsigned int length, bool option); //merge sort
//void mSort(float A[], unsigned int length, bool option); // in place merge sort

//void qSort(float out[], float in[], unsigned int length, bool option); // quick sort
//void qSort(float inOut[], unsigned int length, bool option); //in place quick sort

//void cSort(float out[], float in[], unsigned int length, bool option); // comb sort
void ECG_PRIVATE cSort(float inOut[], unsigned int length, bool option); // in place comb sort

//void iSort(float out[], float in[], unsigned int length, bool option); // insertion sort
//void iSort(float inOut[], unsigned int length, bool option); // in place insertion sort

//void sSort(float out[], float in[], unsigned int length, bool option); // selection sort
//void sSort(float inOut[], unsigned int length, bool option); // in place selection sort

//void hSort(float out[], float in[], unsigned int length, bool option); // heap sort - work to be done

#endif

#ifdef __cplusplus // If this is a C++ compiler, use C linkage
}
#endif
