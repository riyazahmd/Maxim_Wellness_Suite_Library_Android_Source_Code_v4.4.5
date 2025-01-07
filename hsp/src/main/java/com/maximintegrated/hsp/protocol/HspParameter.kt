package com.maximintegrated.hsp.protocol

data class HspParameter(val key: String?, val value: String) {
    companion object {
        const val SEPARATOR_KEY_VALUE = '='
        const val SEPARATOR_VALUE_LIST = ','

        fun fromText(parameterStr: String): HspParameter {
            val parts = parameterStr.split(SEPARATOR_KEY_VALUE, limit = 2)
            if (parts.isEmpty()) throw IllegalArgumentException("Not a valid parameter")

            return if (parts.size == 1) {
                HspParameter(null, parts[0])
            } else {
                HspParameter(parts[0], parts[1])
            }
        }
    }

    constructor(value: String) : this(null, value)

    val valueAsHex: Int
        get() = value.toIntOrNull(16)
            ?: throw InvalidParameterFormatException("$value is not a HEX string")

    val valueAsInt: Int
        get() = value.toIntOrNull()
            ?: throw InvalidParameterFormatException("$value is not an integer")

    val valueAsByteArray: ByteArray
        get() {
            val array = ByteArray(value.length / 2)
            for (i in array.indices) {
                val index = i * 2
                val j = value.substring(index, index + 2).toIntOrNull(16)
                    ?: throw InvalidParameterFormatException("$value is not a HEX string")
                array[i] = j.toByte()
            }
            return array
        }

    val valueAsList: List<String>
        get() = value.split(SEPARATOR_VALUE_LIST)


    fun toText(): String {
        return if (key == null) {
            value
        } else {
            key + SEPARATOR_KEY_VALUE + value
        }
    }
}


class InvalidParameterFormatException(message: String) : Exception(message)

class MissingParameterException(message: String) : Exception(message)