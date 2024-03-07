package com.flexispot.ble.data

import kotlin.experimental.xor

/**
 * 媒体墙协议
 * @author luman
 */
object MediaData {

    const val Factory_SOURCE: Byte = 0x01

    /**
     * 查询/控制/打开数据回复
     */
    const val GET_INFO = 0xFF
    const val CONTROL = 0xFC
    const val OPEN_DATA = 0xF8


    //获取设备信息
    const val GET_TYPE = 0xFB
    //停止
    const val STOP = 0x00
    //点按上升
    const val UP_CLICK = 0x01
    //长按上升
    const val UP_LONG_CLICK = 0x05
    //点按下降
    const val DOWN_CLICK = 0x02
    //长按下降
    const val DOWN_LONG_CLICK = 0x06
    //去指定高度
    const val TO_HEIGHT = 0x80
    //M+1、M+2
    const val M1 = 0x24
    const val M2 = 0x28
    //进入主动复位状态
    const val M3 = 0x30
    //打开OTA功能
    const val OPEN_SWITCH = 0xFA
    //版本信息
    const val Version = 0xFD
    /**
     * @param type0 查询/控制
     * @param type1 具体类型
     * @param 报文内容
     */
    private fun combineData(type0: Byte, type1: Byte, coreData: ByteArray): ByteArray {
        val data = ByteArray(6 + coreData.size)
        data[0] = 0xA5.toByte()
        data[1] = (data.size - 2).toByte()
        data[2] = type0
        data[3] = type1
        for (tempPos in 0 until coreData.size) {
            data[4 + tempPos] = coreData[tempPos]
        }
        data[data.size - 2] = data[1] xor data[2] xor data[3]
        data[data.size - 1] = 0x5A.toByte()
//        println("${bytes2HexString(data)}ppppppppppp231")
        return data
    }
    fun bytes2HexString(b: ByteArray): String {
        var r = ""

        for (i in b.indices) {
            var hex = Integer.toHexString(b[i].toInt()  and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
            }
            r += hex.toUpperCase()
        }

        return r
    }

    /**
     * 获取设备信息
     */
    fun getDeviceInfo(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(GET_INFO.toByte(), GET_TYPE.toByte(), coreData)
    }

    /**
     * 设备数据返回
     */
    fun openDeviceDataBack(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(OPEN_DATA.toByte(), GET_TYPE.toByte(), coreData)
    }
    /**
     * 打开OTA
     */
    fun OpenOTA(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(OPEN_SWITCH.toByte(), STOP.toByte(), coreData)
    }
    /**
     * 获取版本号
     */
    fun GetVersion (): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(Version.toByte(), STOP.toByte(), coreData)
    }
    /**
     * 单击上升
     */
    fun clickUp(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(CONTROL.toByte(), UP_CLICK.toByte(), coreData)
    }

    /**
     * 单击下降
     */
    fun clickDown(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(CONTROL.toByte(), DOWN_CLICK.toByte(), coreData)
    }

    /**
     * 长按上升
     */
    fun longClickUp(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(CONTROL.toByte(), UP_LONG_CLICK.toByte(), coreData)
    }

    /**
     * 长按下降
     */
    fun longClickDown(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(CONTROL.toByte(), DOWN_LONG_CLICK.toByte(), coreData)
    }

    /**
     * 设置当前高度为m1
     */
    fun setM1(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(CONTROL.toByte(), M1.toByte(), coreData)
    }

    /**
     * 设置当前高度为m1
     */
    fun setM2(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(CONTROL.toByte(), M2.toByte(), coreData)
    }

    /**
     * 进入复位状态
     */
    fun reset(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(CONTROL.toByte(), M3.toByte(), coreData)
    }

    /**
     * 停止上升/下降操作
     */
    fun stop(): ByteArray {
        val coreData = ByteArray(1) {
            0.toByte()
        }
        return combineData(CONTROL.toByte(), STOP.toByte(), coreData)
    }

    /**
     * 去往指定高度
     */
    fun toHeight(height: Int): ByteArray {
        val coreData = ByteArray(2)
        coreData[1] = (height.ushr(8) and (0xFF)).toByte()
        coreData[0] = (height and (0xFF)).toByte()
        return combineData(CONTROL.toByte(), TO_HEIGHT.toByte(), coreData)
    }
}