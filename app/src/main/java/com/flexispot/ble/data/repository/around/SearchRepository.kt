package com.flexispot.ble.data.repository.around

import android.bluetooth.BluetoothDevice
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.koylo.ble.BuildConfig
import com.koylo.ble.core.BluetoothTool
import com.koylo.ble.core.Builder
import com.koylo.ble.core.Callback
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.data.bean.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author luman
 * @date 19-11-26
 **/
class SearchRepository {

    var mTool: BluetoothTool? = null
    val localDevices: HashMap<String, Device> = HashMap()
    val devicesForShow: ArrayList<Device> = ArrayList()
    var deviceType: DeviceType? = null

    fun initData(localData: ArrayList<Device>?, type: DeviceType?) {
        deviceType = type
        if (localData != null) {
            for (tempData in localData) {
                localDevices[tempData.mac] = tempData
            }
        }
    }

    /**
     * 搜索设备
     */
    fun searchDevice(
        fragment: FragmentActivity,
        addFlag: MutableLiveData<Boolean>,
        endFlag: MutableLiveData<Boolean>, devName: String?
    ) {
        if (mTool == null) {
            mTool = Builder().activity(fragment).scanPeriod(15000).hasLog(BuildConfig.DEBUG)
                .setName(devName)
                .callback(
                    object : Callback() {
                        override fun deviceBack(
                            device: BluetoothDevice,
                            type: DeviceType,
                            secondType: Int
                        ) {
                            super.deviceBack(device, type, secondType)
//                            println("${device.name}" +"${device.address}"+ "${type.type}ppppppppp")
                            if (localDevices.containsKey(device.address)) {
                                return
                            }
                            if (deviceType != null && type.type != deviceType!!.type) {
                                return
                            }
                            if (devName != null && device.name != devName) {
                                return
                            }

                            val tempDevice = Device(device.name, device.address, type.type,device.name)
                            tempDevice.secondType = secondType
                            devicesForShow.add(tempDevice)
                            localDevices[tempDevice.mac] = tempDevice
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    addFlag.value = true
                                }
                            }


                        }

                        override fun searchEnd() {
                            super.searchEnd()
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    endFlag.value = true
                                }
                            }
                        }
                    }).create()
        }
        mTool?.searchDevices()
    }
    /**
     * 重启搜索
     */
    fun searchAgain() {
        mTool?.searchDevices()
    }

    /**
     * 停止搜索
     */
    fun stopSearch() {
        mTool?.stopSearch()
    }

    /**
     * 清理内存占用
     */
    fun clear() {
        mTool?.onDestroy()
        mTool = null
    }

}