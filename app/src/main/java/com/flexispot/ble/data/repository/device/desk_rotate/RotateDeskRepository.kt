package com.flexispot.ble.data.repository.device.desk_rotate

import android.bluetooth.BluetoothDevice
import androidx.fragment.app.FragmentActivity
import com.koylo.ble.core.BluetoothTool
import com.koylo.ble.core.Builder
import com.koylo.ble.core.Callback
import com.flexispot.ble.BuildConfig
import com.flexispot.ble.data.bean.Device

/**
 * @author luman
 * @date 19-11-29
 **/
class RotateDeskRepository {

    private var mTool: BluetoothTool? = null
    lateinit var mDevice: Device
    //最高/最低高度
    var maxHeight: Float =
        0f
    var minHeight: Float =
        0f

    fun initDevice(device: Device) {
        mDevice = device
    }

    /**
     * 搜索设备
     */
    fun connectDevice(fragment: FragmentActivity, callback: Callback) {
        if (mTool == null) {
            val builder = Builder()
                .activity(fragment)
                .scanPeriod(20000)
                .hasLog(BuildConfig.DEBUG)
            mTool = builder.callback(callback).create()
        }
        mTool?.connectDevice(mDevice.mac)
    }

    fun connectDevice(device: BluetoothDevice) {
        mTool?.connectDevice(device)
    }

    fun stopSearch() {
        mTool?.stopSearch()
    }

    /**
     * 信息发送
     */
    fun sendData(byteArray: ByteArray) {
        mTool?.sendData(mDevice.mac, byteArray)
    }

    fun destroy(){
        mTool?.onDestroy()

    }

}