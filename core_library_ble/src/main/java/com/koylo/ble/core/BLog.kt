package com.koylo.ble.core

import android.util.Log


/**
 * 日志控制器
 */
object BLog {
    const val LOG_TAG = "BluetoothTool.LOG_TAG"

    /**
     * 是否展示日志
     */
    val isLogShow = true

    /**
     * Debug级别输出
     */
    fun d(tag: String, msg: String?) {
        if(isLogShow) {
            Log.d(tag, if (msg.isNullOrEmpty()) "未知输出" else msg)
        }

    }

    /**
     * Error级别输出
     */
    fun e(tag: String, msg: String?) {
        if(isLogShow) {
            Log.e(tag, if (msg.isNullOrEmpty()) "未知异常" else msg)
        }
    }

    /**
     * Debug级别输出
     */
    fun d(msg: String?) {
        if(isLogShow) {
            Log.d(LOG_TAG, if (msg.isNullOrEmpty()) "未知输出" else msg)
        }
    }

    /**
     * Error级别输出
     */
    fun e(msg: String?) {
        if(isLogShow) {
            Log.e(LOG_TAG, if (msg.isNullOrEmpty()) "未知异常" else msg)
        }
    }

    /**
     * Error级别错误码输出
     */
    fun e(code: Int) {
        if(isLogShow) {
            Log.e(LOG_TAG, "蓝牙操作异常，错误码$code")
        }
    }

}