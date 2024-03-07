package com.flexispot.ble.data.db

import com.flexispot.ble.data.bean.Device
import org.litepal.LitePal

/**
 * 设备本地数据操作
 */
class DeviceDao {

    /**
     * 获取设备列表
     */
    fun getDeviceList(): MutableList<Device> = LitePal.findAll(Device::class.java)

    /**
     * 保存设备列表
     */
    fun saveOrUpdateDeviceList(device: Device) {
        device.save()
    }

    /**
     * 设备删除
     */
    fun deleteDevice(device: Device) {
        device.delete()
    }
}