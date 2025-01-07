package com.maximintegrated.hsp

class RegisterField(val address: Int, val bitWidth: Int, val bitShift: Int) {
    fun extractValue(registerValue: Int): Int {
        val mask = (1 shl bitWidth) - 1

        return (registerValue ushr bitShift) and mask
    }
}