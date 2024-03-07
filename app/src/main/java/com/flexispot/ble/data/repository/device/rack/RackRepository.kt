package com.flexispot.ble.data.repository.device.rack

import android.bluetooth.BluetoothDevice
import androidx.fragment.app.FragmentActivity
import com.koylo.ble.BuildConfig
import com.koylo.ble.core.BluetoothTool
import com.koylo.ble.core.Builder
import com.koylo.ble.core.Callback
import com.flexispot.ble.data.bean.Device
import com.orhanobut.logger.Logger

/**
 * 电视架
 */
class RackRepository {

    private var mTool: BluetoothTool? = null
    lateinit var mDevice: Device

    companion object {
        const val DEFAULT_MAX_HEIGHT: Float = 1230f
        const val DEFAULT_MIN_HEIGHT: Float = 600f
    }

    fun initDevice(device: Device) {
        Logger.d("初始化设备：$device")
        mDevice = device
    }

    /**
     * 设备连接
     */
    fun connect(fragment: FragmentActivity, callback: Callback) {
        if (mTool == null) {
            mTool = Builder()
                .activity(fragment)
                .scanPeriod(20000)
                .hasLog(BuildConfig.DEBUG)
                .callback(callback).create()
        }
        Logger.d("进行链接")
        mTool?.connectDevice(mDevice.mac)
    }

    fun straightConnect(device: BluetoothDevice) {
        mTool?.connectDevice(device.address)
    }

    fun searchEnd() {
        mTool?.stopSearch()
    }

    /**
     * 数据发送
     */
    fun sendMsg(msg: ByteArray) {
        mTool?.sendData(mDevice.mac, msg)
    }

    fun clear() {
        mTool?.onDestroy()
    }

}