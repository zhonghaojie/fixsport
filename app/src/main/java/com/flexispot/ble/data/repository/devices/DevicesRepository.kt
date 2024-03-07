package com.flexispot.ble.data.repository.devices

import android.util.SparseArray
import androidx.core.util.set
import androidx.lifecycle.MutableLiveData
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.db.flexispotDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author luman
 * @date 19-11-26
 **/
class DevicesRepository {

    /**
     * 设备类型标签集
     */
    val labels = ArrayList<Int>()

    /**
     * 用于展示的设备列表
     */
    val devicesForShown = ArrayList<Device>()
    /**
     * 设备总列表
     */
    val devices = ArrayList<Device>()
    /**
     * mac和设备的对应
     */
    val devicesMap = HashMap<String, Device>()
    /**
     * 设备类型和设备的对应列表
     */
    val typeDevicesMap = SparseArray<ArrayList<Device>>()

    /**
     * 当前显示的设备类型标识位
     */
    var typeIndex = 0

    /**
     * 选中的设备集
     */
    val multiSelectedDevices = ArrayList<Device>()

    /**
     * 获取全部选中设备
     */
    fun selectDevices() = multiSelectedDevices

    /**
     * 选中设备+1
     */
    fun addSelectDevices(device: Device) {
        if (!multiSelectedDevices.contains(device)) {
            device.selected = true
            multiSelectedDevices.add(device)
        }
    }

    /**
     * 选中设备-1
     */
    fun reduceSelectDevices(device: Device) {
        if (multiSelectedDevices.contains(device)) {
            device.selected = false
            multiSelectedDevices.remove(device)
        }
    }

    /**
     * 清空选中 设备
     */
    fun clearSelected() {
        for (device in multiSelectedDevices) {
            device.selected = false
        }
        multiSelectedDevices.clear()
    }

    /**
     * 全部选中
     */
    fun selectAll() {
        for (device in devicesForShown) {
            if (!device.selected) {
                device.selected = true
                multiSelectedDevices.add(device)
            }
        }
    }

    /**
     * 查询所有设备
     */
    fun queryAll(
        deviceNotify: MutableLiveData<Boolean>,
        labelNotify: MutableLiveData<Boolean>
    ) {
        typeDevicesMap.clear()
        typeDevicesMap[DeviceType.DESK.type] = ArrayList()
        typeDevicesMap[DeviceType.RACK.type] = ArrayList()
        typeDevicesMap[DeviceType.MEDIA.type] = ArrayList()
        typeDevicesMap[DeviceType.THREAD.type] = ArrayList()
        devices.clear()
        labels.clear()
        devicesForShown.clear()
        typeIndex = 0
        devices.addAll(flexispotDatabase.getDeviceDao().getDeviceList())

//        devices.add(Device("1", "1", 1))
//        devices.add(Device("2", "2", 1))
//        devices.add(Device("3", "3", 1))
//        devices.add(Device("4", "4", 1))
//        devices.add(Device("5", "5", 1))
//        devices.add(Device("46", "6", 1))

        typeDevicesMap[DeviceType.ALL.type] = devices
        devicesForShown.addAll(devices)
        for (tempDevice in devices) {
            devicesMap[tempDevice.mac] = tempDevice
            typeDevicesMap[tempDevice.type]?.add(tempDevice)
        }
        for (tempIndex in typeDevicesMap.size() - 1 downTo 0) {
            if (typeDevicesMap[tempIndex].size != 0) {
                labels.add(typeDevicesMap.keyAt(tempIndex))
            }
        }
        deviceNotify.value = true
        labelNotify.value = true
    }

    /**
     * 更新设备名
     */
    suspend fun modifyDevice(
        newName: String,
        deviceNotify: MutableLiveData<Boolean>
    ) {
        if (multiSelectedDevices.size == 1) {
            multiSelectedDevices[0].nickname = newName
            flexispotDatabase.getDeviceDao().saveOrUpdateDeviceList(multiSelectedDevices[0])
            withContext(Dispatchers.Main) {
                deviceNotify.value = true
            }
        }
    }

    /**
     * 删除设备
     */
    suspend fun deleteDevice(
        deviceNotify: MutableLiveData<Boolean>,
        labelNotify: MutableLiveData<Boolean>,
        amount: MutableLiveData<Int>
    ) {
        if (selectDevices().size == 0) {
            return
        }
        for (device in selectDevices()) {
            flexispotDatabase.getDeviceDao().deleteDevice(device)
            devices.remove(device)
            devicesMap.remove(device.mac)
            typeDevicesMap[device.type]?.remove(device)
            devicesForShown.remove(device)
            if (typeDevicesMap[device.type].isEmpty()) {
                labels.remove(device.type)
                if (devicesForShown.isEmpty()) {
                    typeIndex = 0
                    devicesForShown.addAll(typeDevicesMap[DeviceType.ALL.type]!!)
                }
            }
        }
        multiSelectedDevices.clear()
        withContext(Dispatchers.Main) {
            labelNotify.value = true
            deviceNotify.value = true
            amount.value = 0
        }
    }

    /**
     * 添加设备
     */
    suspend fun addDevice(
        device: Device,
        labelNotify: MutableLiveData<Boolean>,
        deviceNotify: MutableLiveData<Boolean>
    ) {
        if (devicesMap.containsKey(device.mac)) {
            return
        }
        val tempDevice = Device(device.name, device.mac, device.type,device.nickname)
        tempDevice.secondType = device.secondType
        flexispotDatabase.getDeviceDao().saveOrUpdateDeviceList(tempDevice)
        devices.add(tempDevice)
        devicesMap[device.mac] = tempDevice
        typeDevicesMap[device.type]?.add(tempDevice)
        if (labels.size == 0) {
            labels.add(DeviceType.ALL.type)
        }
        if (labels.contains(device.type)) {
            if (labels[typeIndex] == device.type || typeIndex == 0) {
                devicesForShown.add(tempDevice)
            }
        } else {
            labels.add(device.type)
            typeIndex = labels.size - 1
            devicesForShown.clear()
            devicesForShown.add(tempDevice)
            withContext(Dispatchers.Main) {
                labelNotify.value = true
            }
        }
        withContext(Dispatchers.Main) {
            deviceNotify.value = true
        }
    }

    /**
     * 根据类型获取设备列表
     */
    fun getByType(type: Int) {
        typeIndex = labels.indexOf(type)
        devicesForShown.clear()
        devicesForShown.addAll(typeDevicesMap[type]!!)
    }
}