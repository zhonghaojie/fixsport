package com.flexispot.ble.gui.device.media

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.koylo.ble.JavaTool
import com.koylo.ble.core.BluetoothTool
import com.koylo.ble.core.Callback
import com.koylo.ble.flexispot.DeviceType
import com.koylo.ble.flexispot.flexispotUUID
import com.flexispot.ble.R
import com.flexispot.ble.data.MediaData
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.device.media.MediaRepository
import com.luman.mvvm.base.LuManViewModel
import com.luman.mvvm.base.SingleEvent
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import java.text.DecimalFormat

/**
 * @author luman
 * @date 19-11-26
 **/
class MediaTwoViewModel(val repository: MediaRepository) : LuManViewModel() {

//    companion object TAG {
//        const val MEMORY_HEIGHT_SIT =
//            "com.flexispot.loctecble.ui.device_detail.control_type2.ControlViewModel.MEMORY_HEIGHT_SIT"
//        const val MEMORY_HEIGHT_STAND =
//            "com.flexispot.loctecble.ui.device_detail.control_type2.ControlViewModel.MEMORY_HEIGHT_STAND"
//        const val NO_LONG =
//            "com.flexispot.loctecble.ui.devices.devicesdctivity.nolong"
//    }

    private var decimalFormat = DecimalFormat(".0")
    val scope = CoroutineScope(Dispatchers.IO)
    //版本信息
    var version = SingleEvent<String>()

    //连接状态
    var connectState = SingleEvent<Boolean>()
    //关闭ota回复
    var openreset = SingleEvent<Boolean>()
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
    //停止操作
    var stopJob: Job? = null
    //复位状态下的停止操作
//    var reset : Boolean = false
//    private var mStopJob : Job? = null

    init {
        //初始化
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(0, 500)
            .setConnectOverTime(10000).operateTimeout = 10000
        unit.value = 0
//        repository.initDevice(device)
    }


    //当前已连接的设备
    private var curBleDevice: BleDevice? = null
    var mController: MediaTwoViewModel? = null
    @Synchronized
    public fun getInstance(): MediaTwoViewModel {
        if (mController == null)
            mController = MediaTwoViewModel(repository)
        return mController as MediaTwoViewModel
    }

    fun startScan() {
//        memoryHeightForSit = PreferenceManager.getDefaultSharedPreferences(fragment)
//            .getInt(MEMORY_HEIGHT_SIT + repository.mDevice.mac, -1)
//        memoryHeightForStand = PreferenceManager.getDefaultSharedPreferences(fragment)
//            .getInt(MEMORY_HEIGHT_STAND + repository.mDevice.mac, -1)
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setScanTimeOut(8000)              // 扫描超时时间，可选，默认10秒
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanFinished(scanResultList: List<BleDevice>) {
                mListener!!.onScanFailure()

            }

            override fun onScanStarted(success: Boolean) {

            }

            override fun onScanning(bleDevice: BleDevice) {
                mListener!!.onGetScanDevice(bleDevice)

            }
        })
    }

    fun startConnect(info: String) {
        BleManager.getInstance().connect(info, object : BleGattCallback() {
            override fun onStartConnect() {
                // 开始连接（主线程）
                if (mListener != null) {
                    mListener!!.onStartConnect()
                }
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                // 连接失败（主线程）
                if (mListener != null) {
                    mListener!!.onConnectFail()
                }
                Connted = false

            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                // 连接成功，BleDevice即为所连接的BLE设备（主线程）
                startNotify(bleDevice)
                curBleDevice = bleDevice

                if (mListener != null) {
                    mListener!!.onConnectSuccess()
                }
                Connted = true

            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                // 连接断开，isActiveDisConnected是主动断开还是被动断开（主线程）
                Connted = false
                //                System.out.println(isActiveDisConnected + "连接断开ppppppppppppp" + device.getKey() + "," + device.getName() + "," + status + "," + device.getMac());

                if (LianjieNumber < 1) {
                    startConnect(info)

                } else {
                    if (mListener != null) {
                        mListener!!.onConnectFail()
                    }
                    LianjieNumber++
                }


            }
        })
    }

    private var LianjieNumber = 0
    fun stopScan() {
        BleManager.getInstance().cancelScan()
    }

    // 断开某个设备
    fun disconnect() {
        if (BleManager.getInstance().isConnected(curBleDevice)) {
            BleManager.getInstance().disconnect(curBleDevice)
            destroy()
        }
        destroy()
    }

    // 退出使用，清理资源
    private fun destroy() {
        BleManager.getInstance().destroy()
    }

    private var Connted: Boolean = false
    private fun startNotify(bleDevice: BleDevice) {
        BleManager.getInstance().notify(
            bleDevice,
            flexispotUUID.SERVICE_UUID, //数据树妖替换
            flexispotUUID.flexispot_MEASUREMENT_POINT,
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    // 打开通知操作成功
                    Connted = true

                }

                override fun onNotifyFailure(exception: BleException) {
                    // 打开通知操作失败
                    if (mListener != null) {
                        mListener!!.onConnectFail()
                    }
                    Connted = false

                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    // 打开通知后，设备发过来的数据将在这里出现
                    //                        Log.e(TAG, "onCharacteristicChanged " + "==============");
                    //                        Log.e(TAG, "onCharacteristicChanged " + DeviceProtocol.BytetohexString(data));
                    //                        Log.e(TAG, "onCharacteristicChanged " + Arrays.toString(data));
                    try {
                        opeData(data)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })


    }

    private var mListener: OnOperationListener? = null

    fun setOnOperationListener(mListener: OnOperationListener) {
        this.mListener = mListener
    }

    interface OnOperationListener {
        fun onScanFailure()  //搜索周围蓝牙设备失败

        fun onStartConnect()  //开始连接（主线程）

        fun onConnectSuccess()  //蓝牙设备连接成功

        fun onConnectFail()  //蓝牙设备连接失败

        fun onGetDeviceInfo(maxheight: Int, minheight: Int, unit: Int)  //获取连接的蓝牙设备信息

        fun onGetScanDevice(scanResult: BleDevice) //获取扫描到的蓝牙设备

        fun height(height: String)

        fun ReplyOpen()
        fun ReplyReset()

    }
//
//    /**
//     * 设置记忆坐姿高度
//     */
//    fun modifyMemoryHeightSit(value: Int, context: Context) {
//        memoryHeightForSit = value
//        PreferenceManager.getDefaultSharedPreferences(context).edit {
//            putInt(MEMORY_HEIGHT_SIT + repository.mDevice.mac, value)
//        }
//    }
//
//    /**
//     * 设置记忆站姿高度
//     */
//    fun modifyMemoryHeightStand(value: Int, context: Context) {
//        memoryHeightForStand = value
//        PreferenceManager.getDefaultSharedPreferences(context).edit {
//            putInt(MEMORY_HEIGHT_STAND + repository.mDevice.mac, value)
//        }
//    }
//

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


private var number:Int=0;
    /**
     * 进行上升
     */
    fun stopP() {
        if (stopJob != null) {
            stopJob?.cancel()
            stopJob = null
            number=0;
        }
        stopJob = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    while (stopJob != null && !stopJob!!.isCancelled && number<5) {
                        number++
                        sendData(MediaData.stop())
                        delay(50)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopJob?.cancel()
                stopJob = null
                number=0;
            }
        }
    }
    /**
     * 取消停止操作
     */
    fun cancelStop() {
        stopJob?.cancel()
        stopJob = null
        number=0;
//        repository.clearMsgs()
    }



    /**
     * 取消复位操作
     */
    fun cancelReset() {
        resetJob?.cancel()
        resetJob = null
//        repository.clearMsgs()
    }


    fun bytes2HexString(b: ByteArray): String {
        var r = ""

        for (i in b.indices) {
            var hex = Integer.toHexString(b[i].toInt() and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
            }
            r += hex.toUpperCase()
        }
//        println("${r}pppppppp")
        return r
    }

    /**
     * 处理返回数据
     */
    private fun opeData(tempData: ByteArray) {

        if (tempData.size < 7 || tempData[0] != 0xA5.toByte() || tempData[tempData.size - 1] != 0x5A.toByte()) {
            //A5 04 FC 30 38 5A 收到复位
//            var s = bytes2HexString(tempData)
            if (tempData.size == 6) {
                if (tempData[3] == 0x30.toByte()) {//收到复位成功
                    if (mListener != null) {
                        mListener!!.ReplyReset()
                    }
                } else if (tempData[2] == 0xFA.toByte()) {
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            openreset.value = true
                        }
                    }
                    if (mListener != null) {
                        mListener!!.ReplyOpen()
                    }

                }
            } else {
                Logger.d("收到错误长度数据：${JavaTool.bytesToHex(tempData)}")
            }

        } else {
            if (tempData[2] == 0xFF.toByte()) {
                //设备信息查询
//                if (tempData[3] == 0xFB.toByte()) {
//                    repository.maxHeight =
//                        tempData[4].toInt().shl(8).and(0xFF).xor(tempData[5].toInt().and(0xFF))
//                            .toFloat()
//                    repository.minHeight =
//                        tempData[6].toInt().shl(8).and(0xFF).xor(tempData[7].toInt().and(0xFF))
//                            .toFloat()
//                    scope.launch {
//                        withContext(Dispatchers.Main) {
//                            unit.value = tempData[8].toInt()
//                        }
//                    }
//                } else
                if (tempData[3] == 0xFD.toByte()) {

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

                                errorMsgId = R.string.ex
                            }
                        }
                        if (mListener != null) {
                            mListener!!.height(errorCode)
                        }

                    }
                }
            } else if (tempData[2] == 0xFD.toByte()) {
                var a = tempData[4].toInt()
                var b = tempData[5].toInt()
                var c = tempData[6].toInt()
                var d = "" + a + "." + b + "." + c;
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
        var tempHeight = height.toInt()
        if (unit.value == 0) {
            if (mListener != null) {
                mListener!!.height(tempHeight.toString())
            }
        } else {
            val temp = tempHeight / 10
            if (mListener != null) {
                mListener!!.height(temp.toString())
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

    /**
     * 数据发送
     */
    fun sendData(byteArray: ByteArray) {
        if (Connted) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    sendData2(byteArray)
                }
            }
        } else {
            if (mListener != null) {
                mListener!!.onConnectFail()
            }
        }
    }

    public fun sendData2(byteArray: ByteArray) {
        BleManager.getInstance().write(
            curBleDevice,
            flexispotUUID.SERVICE_UUID,
            flexispotUUID.flexispot_CONTROL_POINT,
            byteArray,
            object : BleWriteCallback() {

                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                    Log.e("=========", "发送成功 ")
                }

                override fun onWriteFailure(exception: BleException) {
                    Log.e("=========", "发送失败 ")
                }
            })
    }

}