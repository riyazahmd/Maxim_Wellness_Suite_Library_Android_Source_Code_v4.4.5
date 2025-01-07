/*
 * AlgoIntegration.h
 *
 *  Created on: Nov 25, 2019
 *      Author: Yagmur.Gok
 */



#ifndef DRIVERS_ALGOWRAPPER_ALGOWRAPPER_H_
#define DRIVERS_ALGOWRAPPER_ALGOWRAPPER_H_

#ifdef __cplusplus
extern "C" {
#endif

/* Macros for library import/export */
#if defined(WIN32) || defined(_WIN32)
#define COMPILER_INLINED
#ifdef MXM_ALGOSUITE_EXPORTS
#define MXM_ALGOSUITE_API __declspec(dllexport)
#elif defined MXM_RRM_IMPORTS
#define MXM_ALGOSUITE_API __declspec(dllimport)
#else
#define MXM_ALGOSUITE_API
#endif
#define MXM_ALGOSUITE_CALL __cdecl
#else    /* Android */
#define MXM_ALGOSUITE_API __attribute__((__visibility__("default")))
#define MXM_ALGOSUITE_CALL
#define COMPILER_INLINED __attribute__(( always_inline))
#endif

#include <stdbool.h>
#include <stdint.h>


#include "mxm_hrv_public.h"
#include "mxm_respiration_rate_manager.h"
#include "mxm_stress_monitoring.h"
#include "mxm_sleep_manager.h"
#include "mxm_sports_coaching_public.h"


typedef struct {

    uint32_t inp_sample_count	    	;
	uint32_t grn_count					;
	uint32_t grn2Cnt			  		;
	uint32_t irCnt	    		  		;
	uint32_t redCnt				  		;
	int32_t accelx						;
	int32_t accely						;
	int32_t accelz						;
	uint32_t whrm_suite_curr_opmode 	;
	uint32_t hearth_rate_estim      	;
	uint32_t hr_confidence  	  		;
	uint32_t rr_interbeat_interval		;
	uint32_t rr_confidence  	  		;
	uint32_t activity_class				;
	uint32_t r_spo2						;
	uint32_t spo2_confidence  	  		;
	uint32_t spo2_estim					;
	uint32_t spo2_calc_percentage		;
	uint32_t spo2_low_sign_quality_flag	;
	uint32_t spo2_motion_flag       	;
	uint32_t spo2_low_pi_flag	    	;
	uint32_t spo2_unreliable_r_flag 	;
	uint32_t spo2_state			  		;
	uint32_t skin_contact_state	    	;
	uint32_t walk_steps           		;
	uint32_t run_steps            		;
	uint32_t kcal                 		;
	uint32_t cadence              		;
	uint32_t timestampUpper32bit        ;
	uint32_t timestampLower32bit        ;

} mxm_algosuite_input_data;



typedef MxmHrvRet                                 mxm_algosuite_hrv_retcode;
typedef mxm_respiration_rate_manager_return_code  mxm_algosuite_resp_retcode;
typedef mxm_stress_monitoring_return_code         mxm_algosuite_stress_retcode;
typedef mxm_sleep_manager_return                  mxm_algosuite_sleep_retcode;
typedef mxm_sc_return_code                        mxm_algosuite_sc_retcode;


typedef struct {
    char version_string[20]; /**< version in vXX.XX.XX format */
    unsigned short int version;  /**< The first number in vXX.XX.XX format */
    unsigned short int sub_version;  /**< The second number in vXX.XX.XX format */
    unsigned short int sub_sub_version;  /**< The third number in vXX.XX.XX format */
} mxm_algosuite_version_str;



 typedef struct{
	 mxm_algosuite_hrv_retcode    hrv_status;
	 mxm_algosuite_resp_retcode   resp_status;
	 mxm_algosuite_stress_retcode stress_status;
	 mxm_algosuite_sleep_retcode sleep_status;
     mxm_algosuite_sc_retcode sports_coaching_status;
}mxm_algosuite_return_code;


 typedef struct {
 	MxmHrvOutData 						       hrv_out_sample;
 	mxm_sleep_manager_output_dataframe         sleep_out_Sample;
 	mxm_respiration_rate_manager_out_data_str  resp_out_sample;
 	mxm_mxm_stress_monitoring_run_output       stress_out_sample;
    mxm_sc_output                              sc_out_sample;
 }mxm_algosuite_output_data;



#define MXM_ALGOSUITE_ENABLE_HRV    ( 1 << 0)
#define MXM_ALGOSUITE_ENABLE_RESP   ( 1 << 1)
#define MXM_ALGOSUITE_ENABLE_SLEEP  ( 1 << 2)
#define MXM_ALGOSUITE_ENABLE_STRESS ( 1 << 3)
#define MXM_ALGOSUITE_ENABLE_SPORTS ( 1 << 4)


typedef struct {
	unsigned char                           enabledAlgorithms;
	MxmHrvConfig                            hrvConfig;
	mxm_respiration_rate_manager_init_str   respConfig;
	mxm_stress_monitoring_config            stressConfig;
	mxm_sleep_manager_config                sleepConfig;
    mxm_sc_config                           sc_config;
}mxm_algosuite_init_data;






MXM_ALGOSUITE_API void mxm_algosuite_manager_init( const mxm_algosuite_init_data *const init_str,
												   mxm_algosuite_return_code *const status);

MXM_ALGOSUITE_API void mxm_algosuite_manager_run( const mxm_algosuite_input_data *const data_in_str,
												  mxm_algosuite_output_data *const data_out_str,
								                  mxm_algosuite_return_code *const status);

MXM_ALGOSUITE_API void mxm_algosuite_manager_end( const unsigned char tobeDisabledAlgorithms,
												  mxm_algosuite_return_code *const status);

MXM_ALGOSUITE_API void mxm_algosuite_manager_get_versions(mxm_algosuite_version_str *const version_str);


MXM_ALGOSUITE_API void mxm_algosuite_manager_calculate_SQI( float deep_time_in_sec,
                                          	  	  	  	    float rem_time_in_sec,
															float in_sleep_wake_time_in_sec,
															int number_of_wake_in_sleep,
															float *output_sleep_quality_index,
															mxm_algosuite_return_code *const status );

MXM_ALGOSUITE_API void mxm_algosuite_manager_getauthinitials( const uint8_t *const auth_inits,
										    				  uint8_t *out_auth_initials);

MXM_ALGOSUITE_API int mxm_algosuite_manager_authenticate( const uint8_t *const auth_str1,
										                   const uint8_t *const auth_str2);







#ifdef __cplusplus
}
#endif


#endif /* DRIVERS_ALGOWRAPPER_ALGOWRAPPER_H_ */
