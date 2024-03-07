package com.flexispot.ble.gui.device.media

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import com.koylo.ble.JavaTool
import com.koylo.ble.core.Callback
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.R
import com.flexispot.ble.data.MediaData
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.device.media.MediaRepository
import com.flexispot.ble.gui.threadMill.PreferencesUtility
import com.flexispot.ble.ota.ble.AdvDevice
import com.luman.mvvm.base.LuManViewModel
import com.luman.mvvm.base.SingleEvent
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import java.text.DecimalFormat

/**
 * @author luman
 * @date 19-11-26
 **/
class MediaViewModel(val repository: MediaRepository) : LuManViewModel() {

    companion object TAG {
        const val MEMORY_HEIGHT_SIT =
            "com.flexispot.loctecble.ui.device_detail.control_type2.ControlViewModel.MEMORY_HEIGHT_SIT"
        const val MEMORY_HEIGHT_STAND =
            "com.flexispot.loctecble.ui.device_detail.control_type2.ControlViewModel.MEMORY_HEIGHT_STAND"
        const val NO_LONG =
            "com.flexispot.loctecble.ui.devices.devicesdctivity.nolong"
    }

    private var decimalFormat = DecimalFormat(".0")
    val scope = CoroutineScope(Dispatchers.IO)
    //版本信息
    var version = SingleEvent<String>()
    //复位回复
    var reset = SingleEvent<Boolean>()
    //关闭ota回复
    var openreset = SingleEvent<Boolean>()
    //进度条的值
    var progress = SingleEvent<Int>()
    //连接状态
    var connectState = SingleEvent<Boolean>()
    //显示的高度值
    var heightForShow = SingleEvent<String>()
    //错误码字符id
    var errorMsgId: Int = 0
    //单位 公制：0，英制：1
    var unit = SingleEvent<Int>()
    //当前高度
    var currentHeight = 0
    //记忆坐姿高度
    var memoryHeightForSit = -1
    //记忆站姿高度
    var memoryHeightForStand = -1
    //重置操作
    var resetJob: Job? = null
    //复位状态下的停止操作
//    var reset : Boolean = false
//    private var mStopJob : Job? = null

    fun init(device: Device) {
        repository.initDevice(device)
        unit.value = 0

    }

    /**
     * 连接设备
     */
    fun connectDevice(fragment: FragmentActivity) {
        memoryHeightForSit = PreferenceManager.getDefaultSharedPreferences(fragment)
            .getInt(MEMORY_HEIGHT_SIT + repository.mDevice.mac, -1)
        memoryHeightForStand = PreferenceManager.getDefaultSharedPreferences(fragment)
            .getInt(MEMORY_HEIGHT_STAND + repository.mDevice.mac, -1)

        scope.launch {
            withContext(Dispatchers.IO) {
                repository.connectDevice(fragment, callback)
            }
        }


    }
    /**
     * 设置记忆坐姿高度
     */
    fun modifyMemoryHeightSit(value: Int, context: Context) {
        memoryHeightForSit = value
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putInt(MEMORY_HEIGHT_SIT + repository.mDevice.mac, value)
        }
    }

    /**
     * 设置记忆站姿高度
     */
    fun modifyMemoryHeightStand(value: Int, context: Context) {
        memoryHeightForStand = value
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putInt(MEMORY_HEIGHT_STAND + repository.mDevice.mac, value)
        }
    }

    /**
     * 数据发送
     */
    fun sendData(byteArray: ByteArray) {
        if (connectState.value != null && connectState.value!!) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    repository.sendData(byteArray)
                }
            }
        } else {
            scope.launch {
                withContext(Dispatchers.Main) {
                    connectState.value = false
                }
            }
        }
    }
    fun get(mac: String,activity:Int): AdvDevice {
        val remoteDevice =    repository.getDevice(mac)
        val writen = ByteArray(7)
        var a=AdvDevice(remoteDevice,activity,writen)
        return a
    }

    /**
     * 进行复位
     */
    fun reset() {
        if (resetJob != null) {
            resetJob?.cancel()
            resetJob = null
        }
        resetJob = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    while (resetJob != null && !resetJob!!.isCancelled) {
                        sendData(MediaData.clickDown())
                        delay(50)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                resetJob?.cancel()
                resetJob = null
            }
        }
    }

    /**
     * 进行上升
     */
    fun resetUP() {
        if (resetJob != null) {
            resetJob?.cancel()
            resetJob = null
        }
        resetJob = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    while (resetJob != null && !resetJob!!.isCancelled) {
                        sendData(MediaData.clickUp())
                        delay(50)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                resetJob?.cancel()
                resetJob = null
            }
        }
    }



    /**
     * 取消复位操作
     */
    fun cancelReset() {
        resetJob?.cancel()
        resetJob = null
        repository.clearMsgs()
    }

    /**
     * 蓝牙状态、数据回调
     */
    private val callback = object : Callback() {
        override fun disconnected(device: BluetoothDevice) {
            super.disconnected(device)
            scope.launch {
                withContext(Dispatchers.Main) {
                    connectState.value = false
                }
            }
        }

        override fun dataBack(device: BluetoothDevice, data: ByteArray) {
            super.dataBack(device, data)
            opeData(data)
        }

        override fun serviceBack(device: BluetoothDevice, services: List<BluetoothGattService>) {
            super.serviceBack(device, services)
            println("ppppppppppptrue")
            scope.launch {
                withContext(Dispatchers.Main) {
                    connectState.value = true
                    repository.sendData(MediaData.getDeviceInfo())
                }
            }
        }

        override fun deviceBack(
            device: BluetoothDevice,
            type: DeviceType,
            secondType: Int
        ) {
            super.deviceBack(device, type, secondType)
            println("pppppppppppe")
            if (device.address == repository.mDevice.mac) {
                repository.stopSearch()
                scope.async {
                    delay(50)
                    repository.connectDevice(device)
                }
            }
        }

        override fun error(code: Int) {
            println("ppppppppppperror")
            scope.launch {
                withContext(Dispatchers.Main) {
                    connectState.value = false
                }
            }
            super.error(code)
        }
    }
    fun bytes2HexString(b: ByteArray): String {
        var r = ""

        for (i in b.indices) {
            var hex = Integer.toHexString(b[i].toInt()  and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
            }
            r += hex.toUpperCase()
        }
        return r
    }

    /**
     * 处理返回数据
     */
    private fun opeData(tempData: ByteArray) {
//        println("ppp连接")
        if (tempData.size < 7 || tempData[0] != 0xA5.toByte() || tempData[tempData.size - 1] != 0x5A.toByte()) {
           //A5 04 FC 30 38 5A 收到复位
//            bytes2HexString(tempData)
            if(tempData.size == 6 ){
                if (tempData[2] == 0x30.toByte()){//收到复位成功
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            reset.value = true
                        }
                    }
                }else if(tempData[2] == 0xFA.toByte()){
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            openreset.value = true
                        }
                    }
                }
            }else {
                Logger.d("收到错误长度数据：${JavaTool.bytesToHex(tempData)}")
            }

        } else {
            if (tempData[2] == 0xFF.toByte()) {
                //设备信息查询
                if (tempData[3] == 0xFB.toByte()) {
                    repository.maxHeight =
                        tempData[4].toInt().shl(8).and(0xFF).xor(tempData[5].toInt().and(0xFF))
                            .toFloat()
                    repository.minHeight =
                        tempData[6].toInt().shl(8).and(0xFF).xor(tempData[7].toInt().and(0xFF))
                            .toFloat()
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            unit.value = tempData[8].toInt()
                        }
                    }
                } else if (tempData[3] == 0xFD.toByte()) {
                    //实时高度返回
                    if (tempData[7].toInt() == 0 && tempData[8].toInt() == 0) {
                        val tempHeight =
                            tempData[4].toInt().and(0xFF).shl(8).xor(tempData[5].toInt().and(0xFF))
                                .toFloat()
                        currentHeight = tempHeight.toInt()
                        loadingHeight(currentHeight)
                    } else {
                        var errorCode: String = "RST"
                        if (tempData[7].toInt() != -1 || tempData[8].toInt() != -1) {
                            errorCode = "E${tempData[7].toInt()}${tempData[8].toInt()}"
                        }
                        when (errorCode) {
                            "E01" -> {
                                errorMsgId = R.string.e01
                            }
                            "E02" -> {
                                errorMsgId = R.string.e02
                            }
                            "E03" -> {
                                errorMsgId = R.string.e03
                            }
                            "E04" -> {
                                errorMsgId = R.string.e04
                            }
                            "E07" -> {
                                errorMsgId = R.string.e07
                            }
                            "E08" -> {
                                errorMsgId = R.string.e08
                            }
                            "E09" -> {
                                errorMsgId = R.string.e09
                            }
                            "E20" -> {
                                errorMsgId = R.string.e20
                            }
                            "E21" -> {
                                errorMsgId = R.string.e21
                            }
                            "E22" -> {
                                errorMsgId = R.string.e22
                            }
                            "E23" -> {
                                errorMsgId = R.string.e23
                            }
                            "E30" -> {
                                errorMsgId = R.string.e30
                            }
                            else -> {
//                                if(mStopJob == null){
//                                    mStopJob = GlobalScope.launch {
//                                        if(reset) {
//                                            sendData(MediaData.stop())
//                                            delay(50)
//                                        }
//                                    }
//                                }
                                errorMsgId = R.string.ex
                            }
                        }
                        scope.launch {
                            withContext(Dispatchers.Main) {
                                heightForShow.value = errorCode
                            }
                        }
                    }
                }
            }
            else if (tempData[2] == 0xFD.toByte()) {
                var a=tempData[4].toInt()
                var b= tempData[5].toInt()
                var c=tempData[6].toInt()
                var d=""+a+"."+b+"."+c;
                scope.launch {
                    withContext(Dispatchers.Main) {
                        version.value = d
                    }
                }
            }
        }
    }

    /**
     * 计算应显示高度数值
     */
    private fun loadingHeight(height: Int) {
        var tempHeight = height.toFloat()
        if (unit.value == 0)
            scope.launch {
                withContext(Dispatchers.Main) {
                    heightForShow.value = tempHeight.toString()
                }
            }
        else {
            val temp = tempHeight / 10
            scope.launch {
                withContext(Dispatchers.Main) {
                    heightForShow.value = decimalFormat.format(temp)
                }
            }
        }
    }

    /**
     * 缓存辅助方法
     */
    private fun SharedPreferences.edit(action: SharedPreferences.Editor.() -> Unit) {
        val editor = edit()
        action(editor)
        editor.apply()
    }

    fun destroy(){
        repository.destroy()
    }

    fun cleanLianJie(adress:String){
        repository.cleanLianJie(adress)
    }


    fun clearOperation(){
        repository.stopSearch()
    }
}