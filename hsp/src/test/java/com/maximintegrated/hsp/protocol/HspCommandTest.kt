package com.maximintegrated.hsp.protocol

import org.junit.Test
import org.junit.Assert


class HspCommandTest {
    @Test
    fun fromText() {
        val command = HspCommand.fromText("command key1=value1 value2")

        Assert.assertEquals("command", command.name)
        Assert.assertEquals(2, command.parameters.size)
        Assert.assertEquals(HspParameter("key1", "value1"), command.parameters[0])
        Assert.assertEquals(HspParameter("value2"), command.parameters[1])
    }

    @Test
    fun fromText_getDeviceInformationCommand() {
        val command = HspCommand.fromText("get_device_info")

        Assert.assertTrue(command is GetDeviceInformationCommand)
        Assert.assertTrue(command.parameters.isEmpty())
    }

    @Test
    fun fromText_dumpRegistersCommand() {
        val command = HspCommand.fromText("dump_reg ppg")

        Assert.assertTrue(command is DumpRegistersCommand)
        Assert.assertEquals(1, command.parameters.size)

        if (command is DumpRegistersCommand) {
            Assert.assertEquals("ppg", command.sensorName)
        }
    }

    @Test
    fun fromText_dumpRegistersCommand_missingParameter() {
        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("dump_reg")
        }
    }

    @Test
    fun fromText_getRegisterCommand() {
        val command = HspCommand.fromText("get_reg ppg 09")

        Assert.assertTrue(command is GetRegisterCommand)
        Assert.assertEquals(2, command.parameters.size)

        if (command is GetRegisterCommand) {
            Assert.assertEquals("ppg", command.sensorName)
            Assert.assertEquals(0x09, command.registerAddress)
        }
    }

    @Test
    fun fromText_getRegisterCommand_missingParameter() {
        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("get_reg")
        }

        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("get_reg ppg")
        }
    }

    @Test
    fun fromText_getRegisterCommand_invalidParameterFormat() {
        Assert.assertThrows(InvalidParameterFormatException::class.java) {
            HspCommand.fromText("get_reg ppg XY")
        }
    }

    @Test
    fun fromText_setRegisterCommand() {
        val command = HspCommand.fromText("set_reg ppg 09 7F")

        Assert.assertTrue(command is SetRegisterCommand)
        Assert.assertEquals(3, command.parameters.size)

        if (command is SetRegisterCommand) {
            Assert.assertEquals("ppg", command.sensorName)
            Assert.assertEquals(0x09, command.registerAddress)
            Assert.assertEquals(0x7F, command.registerValue)
        }
    }

    @Test
    fun fromText_setRegisterCommand_missingParameter() {
        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("set_reg")
        }

        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("set_reg ppg")
        }

        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("set_reg ppg 09")
        }
    }

    @Test
    fun fromText_setRegisterCommand_invalidParameterFormat() {
        Assert.assertThrows(InvalidParameterFormatException::class.java) {
            HspCommand.fromText("set_reg ppg XY 7F")
        }

        Assert.assertThrows(InvalidParameterFormatException::class.java) {
            HspCommand.fromText("set_reg ppg 09 --")
        }
    }

    @Test
    fun fromText_setConfigurationCommand() {
        val command = HspCommand.fromText("set_cfg flash log 1")

        Assert.assertTrue(command is SetConfigurationCommand)
        Assert.assertEquals(3, command.parameters.size)

        if (command is SetConfigurationCommand) {
            Assert.assertEquals("flash", command.sensorName)
            Assert.assertEquals("log", command.configuration)
            Assert.assertEquals("1", command.value)
        }
    }

    @Test
    fun fromText_setConfigurationCommand_missingParameter() {
        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("set_cfg")
        }

        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("set_cfg flash")
        }
    }

    @Test
    fun fromText_getFormatCommand() {
        val command = HspCommand.fromText("get_format ppg 0")

        Assert.assertTrue(command is GetFormatCommand)
        Assert.assertEquals(2, command.parameters.size)

        if (command is GetFormatCommand) {
            Assert.assertEquals("ppg", command.sensorName)
            Assert.assertEquals(0, command.mode)
        }
    }

    @Test
    fun fromText_getFormatCommand_missingParameter() {
        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("get_format")
        }

        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("get_format ppg")
        }
    }

    @Test
    fun fromText_getFormatCommand_invalidParameterFormat() {
        Assert.assertThrows(InvalidParameterFormatException::class.java) {
            HspCommand.fromText("get_format ppg one")
        }
    }

    @Test
    fun fromText_readCommand() {
        val command = HspCommand.fromText("read ecg 2")

        Assert.assertTrue(command is ReadCommand)
        Assert.assertEquals(2, command.parameters.size)

        if (command is ReadCommand) {
            Assert.assertEquals("ecg", command.sensorName)
            Assert.assertEquals(2, command.mode)
        }
    }

    @Test
    fun fromText_readCommand_missingParameter() {
        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("read")
        }

        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("read ecg")
        }
    }

    @Test
    fun fromText_readCommand_invalidParameterFormat() {
        Assert.assertThrows(InvalidParameterFormatException::class.java) {
            HspCommand.fromText("read ppg zero")
        }
    }

    @Test
    fun fromText_pauseCommand() {
        val command = HspCommand.fromText("pause 1")

        Assert.assertTrue(command is PauseCommand)
        Assert.assertEquals(1, command.parameters.size)
    }

    @Test
    fun fromText_resumeCommand() {
        val command = HspCommand.fromText("pause 0")

        Assert.assertTrue(command is ResumeCommand)
        Assert.assertEquals(1, command.parameters.size)
    }

    @Test
    fun fromText_pauseResumeCommand_missingParameter() {
        Assert.assertThrows(MissingParameterException::class.java) {
            HspCommand.fromText("pause")
        }
    }

    @Test
    fun fromText_pauseResumeCommand_invalidParameterFormat() {
        Assert.assertThrows(InvalidParameterFormatException::class.java) {
            HspCommand.fromText("pause one")
        }
    }

    @Test
    fun fromText_stopCommand() {
        val command = HspCommand.fromText("stop")

        Assert.assertTrue(command is StopCommand)
        Assert.assertTrue(command.parameters.isEmpty())
    }

    @Test
    fun fromText_unknownCommand() {
        val command = HspCommand.fromText("an_unknown_command")

        Assert.assertTrue(command is UnknownCommand)
    }
}