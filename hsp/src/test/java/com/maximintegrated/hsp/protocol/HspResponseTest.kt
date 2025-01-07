package com.maximintegrated.hsp.protocol

import org.junit.Assert
import org.junit.Test

class HspResponseTest {
    @Test
    fun fromText_getRegisterResponse() {
        val response = HspResponse.fromText("get_reg ppg 09 reg_val=4E err=0")

        Assert.assertTrue(response is GetRegisterResponse)
        Assert.assertEquals(1, response.parameters.size)    // reg_val
        Assert.assertEquals(Status.SUCCESS, response.status)

        if (response is GetRegisterResponse) {
            Assert.assertEquals(GetRegisterCommand("ppg", 0x09), response.command)
            Assert.assertEquals(0x4E, response.registerValue)
        }
    }

    @Test
    fun fromText_setRegisterResponse() {
        val response = HspResponse.fromText("set_reg ppg 09 7F err=0")

        Assert.assertTrue(response is SetRegisterResponse)
        Assert.assertTrue(response.parameters.isEmpty())
        Assert.assertEquals(Status.SUCCESS, response.status)

        if (response is SetRegisterResponse) {
            Assert.assertEquals(SetRegisterCommand("ppg", 0x09, 0x7F), response.command)
        }
    }

    @Test
    fun fromText_setConfigurationResponse() {
        val response = HspResponse.fromText("set_cfg flash log 1 err=0")

        Assert.assertTrue(response is SetConfigurationResponse)
        Assert.assertTrue(response.parameters.isEmpty())
        Assert.assertEquals(Status.SUCCESS, response.status)

        if (response is SetConfigurationResponse) {
            Assert.assertEquals(SetConfigurationCommand("flash", "log", "1"), response.command)
        }
    }

    @Test
    fun fromText_readResponse() {
        val response = HspResponse.fromText("read ppg 0 err=0\n")

        Assert.assertTrue(response is ReadResponse)
        Assert.assertTrue(response.parameters.isEmpty())
        Assert.assertEquals(Status.SUCCESS, response.status)

        if (response is ReadResponse) {
            Assert.assertEquals(ReadCommand("ppg", 0), response.command)
        }
    }

    @Test
    fun fromText_pauseResponse() {
        val response = HspResponse.fromText("pause 1 err=0")

        Assert.assertTrue(response is PauseResponse)
        Assert.assertTrue(response.parameters.isEmpty())
        Assert.assertEquals(Status.SUCCESS, response.status)
    }

    @Test
    fun fromText_resumeResponse() {
        val response = HspResponse.fromText("pause 0 err=0")

        Assert.assertTrue(response is ResumeResponse)
        Assert.assertTrue(response.parameters.isEmpty())
        Assert.assertEquals(Status.SUCCESS, response.status)
    }

    @Test
    fun fromText_stopResponse() {
        val response = HspResponse.fromText("stop err=0")

        Assert.assertTrue(response is StopResponse)
        Assert.assertTrue(response.parameters.isEmpty())
        Assert.assertEquals(Status.SUCCESS, response.status)
    }
}