package com.flexispot.ble.gui.devices

import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.devices.DevicesRepository
import com.luman.mvvm.base.LuManViewModel
import com.luman.mvvm.base.SingleEvent

/**
 * @author luman
 * @date 2019/11/26
 * 设备列表
 */
class DevicesViewModel(val repository: DevicesRepository) : LuManViewModel() {

    //设备列表刷新标志位
    private val notifyDevices = SingleEvent<Boolean>()
    //标签列表刷新标志位
    private val notifyLabels = SingleEvent<Boolean>()
    //被选中的设备数量
    private val devicesAmount = SingleEvent<Int>()

    fun devicesNeedRefresh() = notifyDevices
    fun labelNeedRefresh() = notifyLabels
    fun selectedAmount() = devicesAmount

    fun selectedDevices() = repository.selectDevices()
    fun devices() = repository.devicesForShown
    fun labels() = repository.labels
    fun labelIndex() = repository.typeIndex

    /**
     * 初始化设备列表
     */
    fun initData() {
        repository.queryAll(devicesNeedRefresh(), labelNeedRefresh())
    }

    /**
     * 根据标签类型获取设备
     */
    fun getByType(type: Int) {
        repository.getByType(type)
        notifyLabels.value = true
        notifyDevices.value = true
    }

    /**
     * 设备添加
     */
    fun addDevices(device: Device) {
        launch {
            repository.addDevice(device, labelNeedRefresh(), devicesNeedRefresh())
        }
    }

    /**
     * 更新设备名
     */
    fun modifyName(name: String) {
        launch {
            repository.modifyDevice(name, devicesNeedRefresh())
        }
    }

    /**
     * 删除设备
     */
    fun deleteDevice() {
        launch {
            repository.deleteDevice(devicesNeedRefresh(), labelNeedRefresh(), selectedAmount())
        }
    }

    /**
     * 选中+1
     */
    fun selectOne(device: Device) {
        repository.addSelectDevices(device)
        devicesAmount.value = repository.selectDevices().size
    }

    /**
     * 选中-1
     */
    fun reduceOne(device: Device) {
        repository.reduceSelectDevices(device)
        devicesAmount.value = repository.selectDevices().size
    }

    /**
     * 清楚所有设备的选中状态
     */
    fun clearSelectState() {
        repository.clearSelected()
        selectedAmount().value = 0
    }

    /**
     * 全选
     */
    fun selectAll() {
        repository.selectAll()
        selectedAmount().value = repository.selectDevices().size
    }

}