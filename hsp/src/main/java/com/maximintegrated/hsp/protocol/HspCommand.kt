package com.maximintegrated.hsp.protocol


sealed class HspCommand(val name: String, val parameters: List<HspParameter> = emptyList()) {
    companion object {
        const val COMMAND_GET_DEVICE_INFO = "get_device_info"
        const val COMMAND_DUMP_REG = "dump_reg"
        const val COMMAND_GET_REG = "get_reg"
        const val COMMAND_SET_REG = "set_reg"
        const val COMMAND_GET_CFG = "get_cfg"
        const val COMMAND_SET_CFG = "set_cfg"
        const val COMMAND_GET_FORMAT = "get_format"
        const val COMMAND_READ = "read"
        const val COMMAND_PAUSE = "pause"
        const val COMMAND_STOP = "stop"

        val SEPARATOR_PARAMETER = "\\s+".toRegex()

        fun fromText(commandStr: String): HspCommand {
            val parts = commandStr.split(SEPARATOR_PARAMETER)
            if (parts.isEmpty()) throw IllegalArgumentException("Not a valid command")

            val command = parts[0]
            val parameters = parts.drop(1).map { HspParameter.fromText(it) }

            return fromCommandAndParameters(command, parameters)
        }

        fun fromCommandAndParameters(command: String, parameters: List<HspParameter>) =
            when (command) {
                COMMAND_GET_DEVICE_INFO -> {
                    checkCommandParameters(command, parameters, 0)
                    GetDeviceInformationCommand()
                }
                COMMAND_DUMP_REG -> {
                    checkCommandParameters(command, parameters, 1)

                    DumpRegistersCommand(sensorName = parameters[0].value)
                }
                COMMAND_GET_REG -> {
                    checkCommandParameters(command, parameters, 2)
                    GetRegisterCommand(
                        sensorName = parameters[0].value,
                        registerAddress = parameters[1].valueAsHex
                    )
                }
                COMMAND_SET_REG -> {
                    checkCommandParameters(command, parameters, 3)
                    SetRegisterCommand(
                        sensorName = parameters[0].value,
                        registerAddress = parameters[1].valueAsHex,
                        registerValue = parameters[2].valueAsHex
                    )
                }
                COMMAND_GET_CFG -> {
                    checkCommandParameters(command, parameters, 1)
                    if (parameters.size > 3) {
                        GetConfigurationCommand(
                            sensorName = parameters[0].value,
                            configuration = parameters[1].value
                        )
                    }else{
                        GetConfigurationCommand(
                            sensorName = null,
                            configuration = parameters[0].value
                        )
                    }
                }
                COMMAND_SET_CFG -> {
                    checkCommandParameters(command, parameters, 2)

                    if (parameters.size == 2) {
                        SetConfigurationCommand(
                            sensorName = parameters[0].value,
                            configuration = parameters[1].value
                        )
                    } else{
                        var v : String? = null
                        if("err" != parameters[2].key){
                            v = parameters[2].value
                        }
                        SetConfigurationCommand(
                            sensorName = parameters[0].value,
                            configuration = parameters[1].value,
                            value = v
                        )
                    }
                }
                COMMAND_GET_FORMAT -> {
                    checkCommandParameters(command, parameters, 2)
                    GetFormatCommand(
                        sensorName = parameters[0].value,
                        mode = parameters[1].valueAsInt
                    )
                }
                COMMAND_READ -> {
                    checkCommandParameters(command, parameters, 2)
                    ReadCommand(
                        sensorName = parameters[0].value,
                        mode = parameters[1].valueAsInt
                    )
                }
                COMMAND_PAUSE -> {
                    checkCommandParameters(command, parameters, 1)

                    // both used as pause and resume command
                    when (parameters[0].valueAsInt) {
                        PauseCommand.VALUE -> PauseCommand()
                        ResumeCommand.VALUE -> ResumeCommand()
                        else -> throw InvalidParameterFormatException("Parameter should either be 0 or 1, given ${parameters[0].value}")
                    }
                }
                COMMAND_STOP -> {
                    checkCommandParameters(command, parameters, 0)
                    StopCommand()
                }
                else -> UnknownCommand(command, parameters)
            }

        private fun checkCommandParameters(
            command: String,
            parameters: List<HspParameter>,
            numberOfParameters: Int
        ) {
            if (parameters.size < numberOfParameters) {
                throw MissingParameterException("$command requires $numberOfParameters parameter(s) but ${parameters.size} parameter(s) given")
            }
        }
    }

    fun toText(): String {
        return buildString {
            append(name)

            for (parameter in parameters) {
                append(' ')
                append(parameter.toText())
            }
        }
    }

    override fun toString() = "${javaClass.simpleName}()"

    override fun equals(other: Any?) = when {
        (this === other) -> true
        (other !is HspCommand) -> false
        (name != other.name) -> false
        (parameters != other.parameters) -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }
}

class GetDeviceInformationCommand : HspCommand(COMMAND_GET_DEVICE_INFO)

data class DumpRegistersCommand(val sensorName: String) :
    HspCommand(COMMAND_DUMP_REG, listOf(HspParameter(sensorName)))

data class GetRegisterCommand(val sensorName: String, val registerAddress: Int) :
    HspCommand(
        COMMAND_GET_REG,
        listOf(
            HspParameter(sensorName),
            HspParameter(Integer.toHexString(registerAddress))
        )
    )

data class SetRegisterCommand(
    val sensorName: String,
    val registerAddress: Int,
    val registerValue: Int
) :
    HspCommand(
        COMMAND_SET_REG,
        listOf(
            HspParameter(sensorName),
            HspParameter(Integer.toHexString(registerAddress)),
            HspParameter(Integer.toHexString(registerValue))
        )
    )

data class GetConfigurationCommand(val sensorName: String? = null, val configuration: String) :
    HspCommand(
        COMMAND_GET_CFG,
        if (sensorName == null) {
            listOf(HspParameter(configuration))
        } else {
            listOf(HspParameter(sensorName), HspParameter(configuration))
        }
    )

data class SetConfigurationCommand(
    val sensorName: String,
    val configuration: String,
    val value: String? = null
) : HspCommand(
    COMMAND_SET_CFG,
    if (value == null) {
        listOf(HspParameter(sensorName), HspParameter(configuration))
    } else {
        listOf(HspParameter(sensorName), HspParameter(configuration), HspParameter(value))
    }
)

data class GetFormatCommand(val sensorName: String, val mode: Int) :
    HspCommand(
        COMMAND_GET_FORMAT,
        listOf(
            HspParameter(sensorName),
            HspParameter(mode.toString())
        )
    )

data class ReadCommand(val sensorName: String, val mode: Int) :
    HspCommand(
        COMMAND_READ,
        listOf(
            HspParameter(sensorName),
            HspParameter(mode.toString())
        )
    )

class PauseCommand : HspCommand(COMMAND_PAUSE, listOf(HspParameter(VALUE.toString()))) {
    companion object {
        const val VALUE = 1
    }
}

class ResumeCommand : HspCommand(COMMAND_PAUSE, listOf(HspParameter(VALUE.toString()))) {
    companion object {
        const val VALUE = 0
    }
}

class StopCommand : HspCommand(COMMAND_STOP)

class UnknownCommand(name: String, parameters: List<HspParameter>) : HspCommand(name, parameters)
