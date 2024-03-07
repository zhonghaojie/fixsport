package com.koylo.ble;

public class TestData {
    public static final byte CMD_OPEN = 0x01;

    /**
     * 获取通用协议,适用大部分动作
     *
     * @return cmd 协议中代表动作的字节,{@link #CMD_OPEN}等
     */
    public static byte[] getCommonI(byte cmd) {
        byte[] data = new byte[19];
        //校验位
        data[0] = (byte) 0xf5;
        data[1] = (byte) 0xf5;
        //类型
        data[2] = (byte) 1;
        //操作类型 1开门， 2复位， 3开授权， 4关授权， 5查询列表， 6删除指定权限， 7删除全部权限 8 授权主动返回 9查询电量
        data[3] = cmd;
        //第一张卡
        data[4] = 0x00;
        data[5] = 0x00;
        data[6] = 0x00;
        data[7] = 0x00;
        //第二张卡
        data[8] = 0x00;
        data[9] = 0x00;
        data[10] = 0x00;
        data[11] = 0x00;
        //第三张卡
        data[12] = 0x00;
        data[13] = 0x00;
        data[14] = 0x00;
        data[15] = 0x00;
        //异或结果
        int b = data[0];
        for (int i = 0; i < 15; i++) {
            b = b ^ data[i + 1];
        }
        data[16] = (byte) b;
        //校验位
        data[17] = 0x0D;
        data[18] = 0x0A;

        return data;
    }
}
