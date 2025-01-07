package com.maximintegrated.hsp.protocol

import timber.log.Timber


sealed class HspResponse<out T : HspCommand>(
    val command: T,
    val parameters: List<HspParameter> = emptyList(),
    val status: Status = Status.SUCCESS
) {

    companion object {
        const val KEY_STATUS = "err"

        fun fromText(responseStr: String): HspResponse<HspCommand> {
            val parts = responseStr.split(HspCommand.SEPARATOR_PARAMETER)
            if (parts.isEmpty()) throw IllegalArgumentException("Not a valid response")

            val commandStr = parts[0]
            val allParameters = parts.drop(1).map { HspParameter.fromText(it) }

            val command = HspCommand.fromCommandAndParameters(commandStr, allParameters)
            val responseParameters = allParameters.drop(command.parameters.size)

            return fromCommandAndParameters(command, responseParameters)
        }

        private fun fromCommandAndParameters(
            command: HspCommand,
            parameters: List<HspParameter>
        ): HspResponse<*> {
            val responseParameters: List<HspParameter>
            val status: Status

            if (parameters.isNotEmpty() && parameters.last().key == KEY_STATUS) {
                responseParameters = parameters.dropLast(1)
                status = Status.fromCode(parameters.last().value.toInt())
            } else {
                responseParameters = parameters
                status = Status.UNKNOWN_COMMAND_ERROR
            }

            return when (command) {
                is GetDeviceInformationCommand -> {
                    val hubVersion = responseParameters.firstOrNull {
                        it.key == GetDeviceInformationResponse.HUB_VERSION
                    }

                    val firmwareVersion = responseParameters.firstOrNull {
                        it.key == GetDeviceInformationResponse.FIRMWARE_VERSION
                    }

                    GetDeviceInformationResponse(
                        command,
                        firmwareVersion?.value.toString(), hubVersion?.value.toString(), status
                    )
                }
                is DumpRegistersCommand -> DumpRegistersResponse(command, status)
                is GetRegisterCommand -> {
                    val registerValueParameter = responseParameters.firstOrNull {
                        it.key == GetRegisterResponse.KEY_REGISTER_VALUE
                    }

                    if (registerValueParameter == null) {
                        GetRegisterResponse(command, status = status)
                    } else {
                        GetRegisterResponse(command, registerValueParameter.valueAsHex, status)
                    }
                }
                is SetRegisterCommand -> SetRegisterResponse(command, status)
                is SetConfigurationCommand -> SetConfigurationResponse(command, status)
                is GetFormatCommand -> GetFormatResponse(command, status)
                is ReadCommand -> ReadResponse(command, status)
                is PauseCommand -> PauseResponse(command, status)
                is ResumeCommand -> ResumeResponse(command, status)
                is StopCommand -> StopResponse(command, status)
                is UnknownCommand -> UnknownResponse(command, status)
                is GetConfigurationCommand -> {
                    val cfgValue = responseParameters.firstOrNull {
                        it.key == GetConfigurationResponse.KEY_CFG_VALUE
                    }
                    GetConfigurationResponse(
                        command,
                        cfgValue?.value,
                        status
                    )
                }
            }
        }
    }

    fun toText(): String {
        return buildString {
            append(command.toText())

            for (parameter in parameters) {
                append(' ')
                append(parameter.toText())
            }

            append(' ')
            append(KEY_STATUS)
            append(HspParameter.SEPARATOR_KEY_VALUE)
            append(status.code)
        }
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(command=$command, status=$status)"
    }

    override fun equals(other: Any?) = when {
        (this === other) -> true
        (other !is HspResponse<*>) -> false
        (command != other.command) -> false
        (parameters != other.parameters) -> false
        (status != other.status) -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + parameters.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }
}

data class Status(val code: Int, val message: String) {
    companion object {
        val SUCCESS = Status(0, "Success")
        val UNSPECIFIED_ERROR = Status(-1, "Unspecified error")
        val FILE_ACCESS_ERROR = Status(-2, "File access error")
        val I2C_ERROR = Status(-3, "I2C error")
        val NO_DRIVER_ERROR = Status(-4, "No driver error")
        val NO_DEVICE_ERROR = Status(-5, "No device error")
        val NO_LIBRARY_OR_ALGORITHM_ERROR = Status(-6, "No library or algorithm error")
        val NO_MEMORY_ERROR = Status(-7, "No memory error")
        val GENERAL_DRIVER_ERROR = Status(-8, "General driver error")
        val INVALID_OR_MISSING_PARAMETER_ERROR =
            Status(-254, "Invalid or missing parameter error")
        val UNKNOWN_COMMAND_ERROR = Status(-255, "Unknown command error")

        fun fromCode(statusCode: Int) = when (statusCode) {
            SUCCESS.code -> SUCCESS
            UNSPECIFIED_ERROR.code -> UNSPECIFIED_ERROR
            FILE_ACCESS_ERROR.code -> FILE_ACCESS_ERROR
            I2C_ERROR.code -> I2C_ERROR
            NO_DRIVER_ERROR.code -> NO_DRIVER_ERROR
            NO_DEVICE_ERROR.code -> NO_DEVICE_ERROR
            NO_LIBRARY_OR_ALGORITHM_ERROR.code -> NO_LIBRARY_OR_ALGORITHM_ERROR
            NO_MEMORY_ERROR.code -> NO_MEMORY_ERROR
            GENERAL_DRIVER_ERROR.code -> GENERAL_DRIVER_ERROR
            INVALID_OR_MISSING_PARAMETER_ERROR.code -> INVALID_OR_MISSING_PARAMETER_ERROR
            UNKNOWN_COMMAND_ERROR.code -> UNKNOWN_COMMAND_ERROR
            else -> Status(statusCode, "Unknown status code")
        }
    }

    val isSuccess get() = (code == SUCCESS.code)

    val isError get() = !isSuccess
}

// TODO: implement response parsing
class GetDeviceInformationResponse(
    command: GetDeviceInformationCommand,
    val firmwareVersion: String,
    val hubVersion: String,
    status: Status
) :
    HspResponse<GetDeviceInformationCommand>(
        command, listOf(
            HspParameter(FIRMWARE_VERSION, firmwareVersion ?: ""), HspParameter(
                HUB_VERSION, hubVersion ?: ""
            )
        ), status = status
    ) {
    companion object {
        const val HUB_VERSION = "hub_firm_ver"
        const val FIRMWARE_VERSION = "firmware_ver"
    }
}

class GetRegisterResponse(
    command: GetRegisterCommand,
    val registerValue: Int? = null,
    status: Status
) :
    HspResponse<GetRegisterCommand>(
        command, listOf(HspParameter(KEY_REGISTER_VALUE, registerValue?.toString() ?: "")), status
    ) {

    companion object {
        const val KEY_REGISTER_VALUE = "reg_val"
    }

}

class SetRegisterResponse(command: SetRegisterCommand, status: Status) :
    HspResponse<SetRegisterCommand>(command, status = status)

// TODO: implement response parsing
class DumpRegistersResponse(command: DumpRegistersCommand, status: Status) :
    HspResponse<DumpRegistersCommand>(command, status = status)

class GetConfigurationResponse(
    command: GetConfigurationCommand,
    val value: String? = null,
    status: Status
) :
    HspResponse<GetConfigurationCommand>(
        command, listOf(HspParameter(KEY_CFG_VALUE, value ?: "")), status
    ) {

    companion object {
        const val KEY_CFG_VALUE = "value"
    }

}

class SetConfigurationResponse(command: SetConfigurationCommand, status: Status) :
    HspResponse<SetConfigurationCommand>(command, status = status)

// TODO: implement response parsing
class GetFormatResponse(command: GetFormatCommand, status: Status) :
    HspResponse<GetFormatCommand>(command, status = status)

class ReadResponse(command: ReadCommand, status: Status) :
    HspResponse<ReadCommand>(command, status = status)

class PauseResponse(command: PauseCommand, status: Status) :
    HspResponse<PauseCommand>(command, status = status)

class ResumeResponse(command: ResumeCommand, status: Status) :
    HspResponse<ResumeCommand>(command, status = status)

class StopResponse(command: StopCommand, status: Status) :
    HspResponse<StopCommand>(command, status = status)

class UnknownResponse(command: UnknownCommand, status: Status) :
    HspResponse<UnknownCommand>(command, status = status)