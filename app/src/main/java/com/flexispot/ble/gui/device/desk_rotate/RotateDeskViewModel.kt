package com.flexispot.ble.gui.device.desk_rotate

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import androidx.fragment.app.FragmentActivity
import com.koylo.ble.core.Callback
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.data.RotateDeskData
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.device.desk_rotate.RotateDeskRepository
import com.luman.mvvm.base.LuManViewModel
import com.luman.mvvm.base.SingleEvent
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.and
import java.text.DecimalFormat

/**
 * @author luman
 * @date 19-11-29
 **/
class RotateDeskViewModel(val repository: RotateDeskRepository) : LuManViewModel() {

    private var decimalFormat = DecimalFormat(".0")


    //连接状态
    var connectState = SingleEvent<Boolean>()
    //显示的高度值
    var heightForShow = SingleEvent<String>()
    var currentHeight: Float = 0f
    //显示的角度值
    var angleForShow = SingleEvent<String>()
    //单位 公制：0，英制：1
    var unit = SingleEvent<Int>()

    /**
     * 设备初始化
     */
    fun init(device: Device) {
        repository.initDevice(device)
        unit.value = 0
    }

    /**
     * 连接设备
     */
    fun connectDevice(fragment: FragmentActivity) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                repository.connectDevice(fragment, callback)
            }
        }
    }

    /**
     * 数据发送
     */
    fun sendData(byteArray: ByteArray) {
        if (connectState.value != null && connectState.value!!) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    repository.sendData(byteArray)
                }
            }
        } else {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    Logger.d("发数据时还没连上，主动断开")
                    connectState.value = false
                }
            }
        }
    }

    /**
     * 蓝牙状态、数据回调
     */
    private val callback = object : Callback() {
        override fun disconnected(device: BluetoothDevice) {
            super.disconnected(device)
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    Logger.d("设备链接断开了")
                    connectState.value = false
                }
            }
        }

        override fun serviceBack(device: BluetoothDevice, services: List<BluetoothGattService>) {
            super.serviceBack(device, services)
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    connectState.value = true
                    sendData(RotateDeskData.getDeviceParam())
                }
            }
        }

        override fun dataBack(device: BluetoothDevice, data: ByteArray) {
            super.dataBack(device, data)
            opeData(data)
        }

        override fun deviceBack(
            device: BluetoothDevice,
            type: DeviceType,
            secondType: Int
        ) {
            super.deviceBack(device, type, secondType)
            if (device.address == repository.mDevice.mac) {
                repository.stopSearch()
                repository.connectDevice(device)
            }
        }
    }

    /**
     * 数据预处理
     */
    private fun opeData(data: ByteArray) {
        if (data.size != 20 || (data[0] + data[19]).and(0xFF) != 255) {
            Logger.d("错误数据，长度：${data.size}")
            return
        }

        var temp = 0
        for (index in 0..17) {
            temp += data[index]
        }
        val checkTag = temp.and(0xFF).toByte()
        if (checkTag != data[18]) {
            Logger.d("校验结果错误，校验和：${checkTag}")
            return
        }
        when (data[19] and 0xFF) {
            0xFE -> {
                //设备参数
                repository.maxHeight =
                    data[1].toInt().shl(8).and(0xFF).xor(data[2].toInt().and(0xFF))
                        .toFloat()
                repository.minHeight =
                    data[3].toInt().shl(8).and(0xFF).xor(data[4].toInt().and(0xFF))
                        .toFloat()

                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        unit.value = data[5].toInt()
                        Logger.d("收到单位信息：${unit.value}")
                    }
                }
            }
            0xFF -> {
                //设备状态
                val tempHeight =
                    data[1].toInt().and(0xFF).shl(8).xor(data[2].toInt().and(0xFF))
                        .toFloat()
                loadingHeight(tempHeight.toInt())
            }
            else -> {
                Logger.d("无效数据，类型：${data[19] and 0xFF}")
            }
        }
    }

    /**
     * 计算应显示高度数值
     */
    private fun loadingHeight(height: Int) {
        val tempHeight = height.toFloat()
        currentHeight = tempHeight
        if (unit.value == 0)
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    heightForShow.value = tempHeight.toString()
                }
            }
        else {
            val temp = tempHeight / 10
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    heightForShow.value = decimalFormat.format(temp)
                }
            }
        }
    }

    fun destroy(){
        repository.destroy()
    }



}