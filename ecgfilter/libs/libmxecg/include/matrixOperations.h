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

#ifndef __MATRIX_OPERATIONS_H__
#define __MATRIX_OPERATIONS_H__

#if defined _WIN32 || defined __CYGWIN__
#define ECG_PRIVATE
#else
#define ECG_PRIVATE __attribute__ ((visibility ("hidden")))
#endif

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#if !defined(CPP_BUILD)
#include <stdbool.h>
#endif

#define lin_index(i, j, numCol)  ( ((i)*(numCol))+(j) )   //2D to 1D array

/*multiply NxK by KxM matrices
parameters: (output) NxM matrix  (input) NxK matrix, KxM matrix, row count for input 1,
col count for input 1 which is also row count for input 2, col count for input 2 */
void ECG_PRIVATE multiply_matrices(float output_matrix[], float matrix_1[], float matrix_2[], int N, int K, int M);

/*multiply two NxN matrices
parameters: (output) NxN matrix  (input) NxN input 1, NxN input 2, N */
void ECG_PRIVATE multiply_square_matrices(float output_matrix[], float matrix_1[], float matrix_2[], int N);

/*multiply an NxK matrix with a Kx1 vector
parameters: (output) Nx1 output vector  (input) NxK input matrix, Kx1 input vector, K, N  */
void ECG_PRIVATE multiply_matrix_with_vector(float output_vector[], float input_matrix[], float input_vector[], int N, int K);

/*add two vectors of the same length
parameters: (output) Nx1 output vector (input) Nx1 input vector 1, Nx1 input vector 2, N */
void ECG_PRIVATE add_vectors(float output_vector[], float vector_1[], float vector_2[], int length);

/*subtract a vector from another with the same length
parameters: (output) Nx1 output vector (input) Nx1 input vector 1, Nx1 input vector 2, N */
void ECG_PRIVATE subtract_vector_from_another(float output_vector[], float vector_1[], float vector_2[], int length);

/*add two matrices of the same size
parameters: (output) NxM output vector (input) NxM input matrix,  NxM input matrix, N, M */
void ECG_PRIVATE add_matrices(float output_matrix[], float matrix_1[], float matrix_2[], int numrows, int numcols);

/*subtract a matrix from another with the same size
parameters: (output) NxM output vector (input) NxM input matrix,  NxM input matrix, N, M */
void ECG_PRIVATE subtract_matrix_from_another(float output_matrix[], float matrix_1[], float matrix_2[], int numrows, int numcols);

/* finds matrix transpose of a non square matrix
parameters: (output) MxN matrix transpose (input) NxM matrix, row count, column count */
void ECG_PRIVATE transpose(float transposed[], float input[], int N, int M);

/* Generates a smaller matrix from a matrix whose determinant is to be calculated
paratmeters: (input) matrix, iteration number, (output) smaller matrix, (input) matrix size */
void ECG_PRIVATE form_new_matrix(float mat[], int iter, float  temp_mat[], int size);

/*Finds determinant of a square matrix
parameters: (input) matrix, matrix size
returns determinant value */
float ECG_PRIVATE determinant(float matrix[], int size);

/* Finds cofactor matrix of a given matrix
parameters: (output) cofactor matrix, (input) matrix, square matrix size */
void ECG_PRIVATE cofactor(float cofact_matrix[], float input_matrix[], int size);

/* finds inverse of a square matrix
   parameters: (output) inverse matrix, (input) matrix, matrix size */
void ECG_PRIVATE invert_matrix(float inverse_matrix[], float input_matrix[], int size);

/* outputs an identity matrix of given size */
void ECG_PRIVATE set_to_identity(float matrix[], int size);

/* copies a matrix into another */
void ECG_PRIVATE copy_matrix(float out[], float in[], int N, int M);

/*returns norm of vector*/
float ECG_PRIVATE vector_norm(float in[], int size);

/*diagonalizes a vector*/
void ECG_PRIVATE vector_to_diagonal(float matrix[], float vector[], int size);

/*extract a vector from square matrix diagonal*/
void ECG_PRIVATE diagonal_to_vector(float vector[], float matrix[], int size);

/*scales a vector or matrix by a given coefficient*/
void ECG_PRIVATE scale_elements(float arrayOut[], float arrayIn[], float scaler, int size);

/*inner product of two vectors*/
float ECG_PRIVATE dotProduct(float vector1[], float vector2[], int size);

/*element-wise multiplication of two vectors or matrices of equal size*/
void ECG_PRIVATE multiply_elementwise(float arrayOut[], float arrayIn1[], float arrayIn2[], int size);

/*Checks symmetry of square matrix A of size n*/
bool ECG_PRIVATE is_symmetric(float A[], int n);

/*returns trace of a matrix*/
float ECG_PRIVATE trace_of_matrix(float *A, int n);

/* print a square matrix on screen */
void ECG_PRIVATE print_square_matrix(float matrix[], int size);

/* print a NxM matrix on screen */
void ECG_PRIVATE print_matrix(float matrix[], int row, int col);

#endif

#ifdef __cplusplus // If this is a C++ compiler, use C linkage
}
#endif
