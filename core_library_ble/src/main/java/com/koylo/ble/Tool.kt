package com.koylo.ble

import kotlin.experimental.and

object Tool {

    /**
     * 字节数组转16进制数
     */
    fun byte2HexString(data: ByteArray): String {
        val str = StringBuilder()
        if (data.size < 0) {
            return ""
        }
        for (i in data.indices) {
            val v = data[i] and 255.toByte()
            val hv = Integer.toHexString(v.toInt())
            str.append(hv)
        }
        return str.toString()
    }

}