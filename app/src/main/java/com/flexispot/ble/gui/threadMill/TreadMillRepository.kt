package com.flexispot.ble.gui.threadMill;

import android.bluetooth.BluetoothDevice
import androidx.fragment.app.FragmentActivity
import com.flexispot.ble.BuildConfig
import com.flexispot.ble.data.bean.Device
import com.koylo.ble.core.BluetoothTool
import com.koylo.ble.core.Builder
import com.koylo.ble.core.Callback


/**
 * @author luman
 * @date 19-11-29
 **/
class TreadMillRepository {

    private var mTool: BluetoothTool? = null
    lateinit var mDevice: Device

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

    fun destroy(){
        mTool?.onDestroy()
    }

    fun stopSearch() {
        mTool?.stopSearch()
    }
    fun getDevice(mac: String): BluetoothDevice {
        val remoteDevice = mTool?.getDevice(mac)
        return remoteDevice!!
    }

    /**
     * 信息发送
     */
    fun sendData(byteArray: ByteArray) {
        mTool?.sendData(mDevice.mac, byteArray)
    }

}