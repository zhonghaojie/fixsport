package com.koylo.ble.core

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import com.koylo.ble.flexispot.DeviceType

/**
 * 蓝牙回调
 */
open class Callback {

    /**
     * 设备发现
     */
    open fun deviceBack(device: BluetoothDevice, type: DeviceType, secondType: Int) {}

    /**
     * 设备搜索结束
     */
    open fun searchEnd() {}

    /**
     * 错误状态返回
     * @param code 错误码 参照{@Link ErrorCode}
     */
    open fun error(code: Int) {}

    /**
     * 信息发送成功
     */
    open fun sendSuccess(device: BluetoothDevice) {}

    /**
     * 信息发送失败
     */
    open fun sendFailed(device: BluetoothDevice) {}

    /**
     * 设备已连接
     */
    open fun connected(device: BluetoothDevice) {}

    /**
     * 设备失去连接
     */
    open fun disconnected(device: BluetoothDevice) {}

    /**
     * 数据返回
     */
    open fun dataBack(device: BluetoothDevice, data: ByteArray) {}

    /**
     * 服务列表返回
     */
    open fun serviceBack(device: BluetoothDevice, services: List<BluetoothGattService>) {}
}