package com.koylo.ble.core

/**
 * 错误码集
 */
object ErrorCode {

    /**
     * 上下文对象被销毁
     */
    const val CONTEXT_MISSED = 0
    /**
     * 设备不支持BLE
     */
    const val BLE_UNSUPPORTED = 1
    /**
     * 蓝牙不可用
     */
    const val BLUETOOTH_DISABLE = 2
    /**
     * 蓝牙未开启
     */
    const val BLUETOOTH_NOT_OPEN = 3
    /**
     * 无蓝牙权限
     */
    const val PERMISSION_DENIED = 4
    /**
     * 扫描失败
     */
    const val SCAN_FAILED = 5
    /**
     * uuid未匹配成功
     */
    const val UUID_ERROR = 6
    //未发现可用服务
    const val SERVICE_NOT_FOUND = 7

}