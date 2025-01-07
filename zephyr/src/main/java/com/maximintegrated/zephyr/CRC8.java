package com.maximintegrated.zephyr;

public class CRC8 {
    private int _crc8Poly;

    public CRC8(int crc8Poly) {
        this._crc8Poly = crc8Poly;
    }

    public byte calculate(byte[] bytes) {
        int crc = 0;

        for(int index = 0; index < bytes.length; ++index) {
            crc = (crc ^ bytes[index]) & 255;

            for(int loop = 0; loop < 8; ++loop) {
                if ((crc & 1) == 1) {
                    crc = crc >> 1 ^ this._crc8Poly;
                } else {
                    crc >>= 1;
                }
            }

            crc &= 255;
        }

        return (byte)crc;
    }
}
