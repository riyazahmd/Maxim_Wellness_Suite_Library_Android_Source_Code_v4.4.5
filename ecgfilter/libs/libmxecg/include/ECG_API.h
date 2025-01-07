/*******************************************************************************
* Copyright (C) 2017 Maxim Integrated Products, Inc., All rights Reserved.
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
* @file ECG_API.h
* @author Melih Altun
* @date May 2017
* @brief ECG processing module header file
*/

/**
* @defgroup ECG Lib module
* @brief    Includes ECG processing related definitions.
*
* This file contains ECG output data structure and function prototypes
*/

#ifdef __cplusplus // If this is a C++ compiler, use C linkage
extern "C" {
#endif

#ifndef ECG_API_INCLUDE
#define ECG_API_INCLUDE

	// config contains one time settings. Once assigned they cannot be changed during measurement.
	/**
	* @brief Data structure representing ECG processing configuration.
	*
	* Defines the sampling frequency, output gain and CIC compansation filter operation.
	*
	*/
	typedef struct _ecgConfig {
		unsigned int Fs;  //sampling frequency
						  // allowed values: 200, 400, 800, 125, 128, 250, 256, 500, 512 - will be set to default value 400 if entered otherwise

		float algoGain;  //algorithm output gain.
						 // set to 1.0 if no gain factor is required at the output

		bool cicCompensate;  //CIC filter compensation on / off flag
							 // set to true if HW CIC filter is active

		bool inADC;  // Input in ADC count / mV flag
					// true if inputs are in ADC counts false if in mV
	} ecgConfig;


	/**
	* @brief Data structure for ECG processing input.
	*
	* Defines Raw ADC count and filter settings. Filters cut offs can be modified or turned off during measurement
	*
	*/
	typedef struct _ecgInput {
		float ecgRaw; // raw ADC count

		unsigned int notchFreq;  // center frequency for line power notch filter
								 //allowed values:50 (filter 50 Hz), 60 (filter 60 Hz), 5060 (filter both 50 and 60Hz), 0 (notch filter off) - will be set to default value of 0 if entered otherwise.

		unsigned int cutoffFreq;  //cut off frequency for low pass filter
								  //allowed values: 0 (filter off), 20, 30, 40, 50, 60, 70 (filter cut of at given value in Hz) - will be set to default value of 40 if entered otherwise.

		bool adaptiveFilterOn;  // adaptive filter on / off switch

		bool baselineRemoveOn;  // baseline filter on / off switch
	} ecgInput;


	// filtered ECG output, heart rate, heart rate variablility and signal baseline are the algorithm outputs.
	/**
	* @brief Data structure representing color channel data.
	*
	* Defines filtered ECG signal, heart rate, heart rate variablility, ECG baseline and pulse flags (R peak detections).
	*
	*/
	typedef struct _ecgOutput {
		float output;  //processed ECG signal in ADC counts

		float HR;  // Detected heart rate in Hz

		float HRV;  // RMSSD heart rate variability in seconds

		float baseline;  //signal baseline in ADC counts

		bool ecgPulseFlag;  // R peak detection flag
	} ecgOutput;



	/** @brief Allocates dynamic memory and initializes ECG processing algorithm with the given configureation.
	*
	*  @param[in] ecgConfig: sampling Rate, algortihm gain, cic filter compensation on / off flag
	*  @return 0 for SUCCESS, -1 for ERROR
	*/
	int ecgInit(ecgConfig *cfg);

	/** @brief Executes ECG processing with the given raw ADC reading and filter settings.
	*
	*  @param[in] ecgInput: raw ADC value, algortihm gain, cic filter compensation on / off flag
	*  @param[out] ecgOutput: filtered ADC value, heart rate variability, baseline count, R peak detection flag
	*  @return 0 for SUCCESS, -1 for ERROR
	*/
	int ecgProcess(ecgInput *in, ecgOutput *out);

	/** @brief De-allocates dynamic memory used for ECG processing.
	*
	*  @param[in] NONE
	*  @return 0 for SUCCESS, -1 for ERROR
	*/
	int ecgEnd();


	/** @brief Gets algorithm version number
	*
	*  @param[out] char array *buff
	*  @return 0 for SUCCESS, -1 for ERROR
	*/
	int ecgProcessVersion(char *buf);


#endif
#ifdef __cplusplus // If this is a C++ compiler, use C linkage
}
#endif
