package com.maximintegrated.nonin

import android.bluetooth.BluetoothGattCharacteristic
import java.util.*
import kotlin.math.pow


const val BASE_BLUETOOTH_UUID_POSTFIX = "0000-1000-8000-00805F9B34FB"

fun uuidFromShortCode16(shortCode16: String) =
    UUID.fromString("0000$shortCode16-$BASE_BLUETOOTH_UUID_POSTFIX")

fun uuidFromShortCode32(shortCode32: String) =
    UUID.fromString("$shortCode32-$BASE_BLUETOOTH_UUID_POSTFIX")

fun BluetoothGattCharacteristic?.hasProperty(property: Int): Boolean {
    return ((this != null) && (properties and property) > 0)
}

fun Short.toSFloat(): Float {
    val mantissa: Int = if (this.toInt() and 0x0800 != 0) {
        (this.toInt() and 0x0FFF) or 0xF000
    } else {
        this.toInt() and 0x0FFF
    }
    val exponent: Int = if (this < 0) {
        (((this.toInt() shr 12) and 0x0F) or 0xF0).toByte().toInt()
    } else {
        (this.toInt() shr 12) and 0x0F
    }
    return when (this.toInt()) {
        0x07FF, 0x0800, 0x0801 -> Float.NaN
        0x07FE -> Float.POSITIVE_INFINITY
        0x0802 -> Float.NEGATIVE_INFINITY
        else -> mantissa * 10.0.pow(exponent.toDouble()).toFloat()
    }
}

val BluetoothGattCharacteristic?.hasReadProperty
    get() = hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

val BluetoothGattCharacteristic?.hasWriteProperty
    get() = hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

val BluetoothGattCharacteristic?.hasNotifyProperty
    get() = hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

val BluetoothGattCharacteristic?.hasIndicateProperty
    get() = hasProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)