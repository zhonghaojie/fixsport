package com.flexispot.ble.data

/**
 * 转就完事了桌协议
 * @author luman
 * @date 19-11-29
 **/
object RotateDeskData {

    //获取设备属性
    const val GET_PARAM = 0XFE
    //获取设备动态数据
    const val GET_STATE = 0XFF
    //控制数据
    const val CONTROL = 0XFD

    //上升
    const val UP = 0X01
    //下降
    const val DOWN = 0X02
    //正翻
    const val FORWARD_ROTATE = 0x55
    //正翻
    const val OPPOSITE_ROTATE = 0xAA

    /**
     * 组合数据
     */
    fun combineData(type0: Int, type1: Int): ByteArray {
        val data = ByteArray(20)
        data[0] = type0.toByte()
        data[1] = type1.toByte()
        for (index in 2..17) {
            data[index] = 0x00
        }
        var temp = 0
        for (index in 0..17) {
            temp += data[index]
        }
        data[18] = temp.and(0xFF).toByte()
        data[19] = (255 - type0).toByte()
        return data
    }

    /**
     * 获取设备参数信息
     */
    fun getDeviceParam(): ByteArray = combineData(GET_PARAM, 0)

    /**
     * 获取设备当前状态
     */
    fun getDeviceState(): ByteArray = combineData(GET_STATE, 0)

    /**
     * 上升
     */
    fun controlUp(): ByteArray = combineData(CONTROL, UP)

    /**
     * 下降
     */
    fun controlDown(): ByteArray = combineData(CONTROL, DOWN)

    /**
     * 正向翻转
     */
    fun controlForwardRotate(): ByteArray = combineData(CONTROL, FORWARD_ROTATE)

    /**
     * 反向翻转
     */
    fun controlOppositeRotate(): ByteArray = combineData(CONTROL, OPPOSITE_ROTATE)

}