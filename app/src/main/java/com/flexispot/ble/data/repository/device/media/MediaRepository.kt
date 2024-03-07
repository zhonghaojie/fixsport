package com.flexispot.ble.data.repository.device.media

import android.bluetooth.BluetoothDevice
import androidx.fragment.app.FragmentActivity
import com.koylo.ble.core.BluetoothTool
import com.koylo.ble.core.Builder
import com.koylo.ble.core.Callback
import com.flexispot.ble.BuildConfig
import com.flexispot.ble.data.bean.Device

class MediaRepository {

    companion object {
        const val DEFAULT_MAX_HEIGHT: Float = 1230f
        const val DEFAULT_MIN_HEIGHT: Float = 600f
    }

    private var mTool: BluetoothTool? = null
    lateinit var mDevice: Device
    //最高/最低高度
    var maxHeight: Float =
        DEFAULT_MAX_HEIGHT
    var minHeight: Float =
        DEFAULT_MIN_HEIGHT

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

    fun getDevice(mac: String): BluetoothDevice {
        val remoteDevice = mTool?.getDevice(mac)
        return remoteDevice!!
    }


    fun connectDevice(device: BluetoothDevice) {
        mTool?.connectDevice(device)
    }

    fun stopSearch() {
        mTool?.stopSearch()
    }

    fun destroy(){
        mTool?.onDestroy()
    }

    fun cleanLianJie(adress:String){
        mTool?.cleanLianJie()
    }



    fun sendData(byteArray: ByteArray) {
        mTool?.sendData(mDevice.mac, byteArray)
    }

    fun clearMsgs() {
        mTool?.msgClear(mDevice.mac)
    }
}