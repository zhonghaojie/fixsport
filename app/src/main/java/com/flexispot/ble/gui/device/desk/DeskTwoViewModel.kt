package com.flexispot.ble.gui.device.desk

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
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
import com.flexispot.ble.data.OriginOpe
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.device.media.MediaRepository
import com.flexispot.ble.data.repository.device.rack.RackRepository
import com.flexispot.ble.gui.device.media.MediaViewModel.TAG.MEMORY_HEIGHT_SIT
import com.flexispot.ble.gui.device.media.MediaViewModel.TAG.MEMORY_HEIGHT_STAND
import com.flexispot.ble.gui.device.rack.RackViewModel
import com.flexispot.ble.gui.view.OnBluethTableListener
import com.luman.mvvm.base.LuManViewModel
import com.luman.mvvm.base.SingleEvent
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import java.text.DecimalFormat

/**
 * @author luman
 * @date 19-11-26
 **/
class DeskTwoViewModel(val repository: RackRepository) : LuManViewModel() {

    companion object {
        const val MEMORY_HEIGHT = "com.flexispot.ble.gui.device.rack.RackViewModel.MEMORY_HEIGHT"
        const val MEMORY_UNIT = "com.flexispot.ble.gui.device.rack.RackViewModel.MEMORY_UNIT"
    }

    var heightForShow = MutableLiveData<String>()
    var unit = MutableLiveData<Int>()
    var connectState = MutableLiveData<Boolean>()
    var getDeviceInfo: Boolean = false
    var currentHeight: Int = 0
    //记忆坐姿高度
    var memoryHeightForSit = -1
    //记忆站姿高度
    var memoryHeightForStand = -1
    internal var decimalFormat = DecimalFormat(".0")

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
    var mController: DeskTwoViewModel? = null
    @Synchronized
    public fun getInstance(): DeskTwoViewModel {
        if (mController == null)
            mController = DeskTwoViewModel(repository)
        return mController as DeskTwoViewModel
    }

    fun startScan(fragment: FragmentActivity) {
        memoryHeightForSit = PreferenceManager.getDefaultSharedPreferences(fragment)
            .getInt(MEMORY_HEIGHT_SIT + repository.mDevice.mac, -1)
        memoryHeightForStand = PreferenceManager.getDefaultSharedPreferences(fragment)
            .getInt(MEMORY_HEIGHT_STAND + repository.mDevice.mac, -1)

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
                        originOpe.opeData(data)
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


    private val mm: OnBluethTableListener = object :
        OnBluethTableListener {
        override fun ConnectionState(connection: Boolean) {

        }

        override fun EquipmentState(
            height: Int,
            alert: Int,
            remindmin: Int,
            sit: Int,
            station: Int
        ) {
            currentHeight = height
            loadingHeight(height)
            if (!getDeviceInfo) {
                val writen = ByteArray(20)
                writen[0] = 0xfe.toByte()
                writen[19] = 0x01.toByte()
                sendData(writen)
            }
        }

        override fun GetDeviceInfo(maxheight: Int, minheight: Int, tempUnit: Int) {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    unit.value = tempUnit
                }
            }
            Log.e("GetDeviceInfo", "maxheight=$maxheight,minheight=$minheight,unit=$unit")
            getDeviceInfo = true

        }

        override fun RemindParam(
            sitmin: Int,
            standmin: Int,
            sitremindopen: Int,
            standremindopen: Int
        ) {

        }

        override fun ServiceNotFound() {

        }
    }
    private val originOpe = OriginOpe(mm)

    fun deviceInit(device: Device) {
        unit.value = 0
        heightForShow.value = "0"
        repository.initDevice(device)
    }

    /**
     * 占用清除
     */
    fun clear() {
        repository.clear()
    }

    /**
     * 响应事件
     */
    fun onDeviceButtonChick(buttonId: Int, state: Int, fragment: FragmentActivity) {
        showDialog()
        preStateCheck({
            if (buttonId == 1) {
                val height = PreferenceManager.getDefaultSharedPreferences(fragment).getInt(
                    RackViewModel.MEMORY_HEIGHT + repository.mDevice.mac, -1
                )

                if (state == 1) {
                    if (height == 0) {
                        showToast(fragment.getString(R.string.tips_setting_down))
                    } else {
                        val isMM =
                            PreferenceManager.getDefaultSharedPreferences(fragment.applicationContext)
                                .getBoolean(
                                    RackViewModel.MEMORY_UNIT + repository.mDevice.mac, true
                                )
                        if (isMM) {
                            controlHight(height)
                        } else {
                            val temp = height.toFloat() / 10.0f
                            val tempHeight = (temp / 0.039370).toInt()
                            Log.d("currentHeight", "height=$height,tempHeight=$tempHeight")
                            controlHight(tempHeight)
                        }

                    }
                } else if (state == 2) {
                    showToast(fragment.getString(R.string.setting_down_success))
                    PreferenceManager.getDefaultSharedPreferences(fragment).edit {
                        putInt(RackViewModel.MEMORY_HEIGHT + repository.mDevice.mac, currentHeight)
                    }
                    PreferenceManager.getDefaultSharedPreferences(fragment).edit {
                        putBoolean(RackViewModel.MEMORY_UNIT + repository.mDevice.mac, unit.value == 0)
                    }
                    Log.d("currentHeight", "currentHeight=$currentHeight,unit=$unit")
                } else
                    controlKey(0, false)
            }

            if (buttonId == 2) {
                val height = PreferenceManager.getDefaultSharedPreferences(fragment).getInt(
                    RackViewModel.MEMORY_HEIGHT + repository.mDevice.mac, -1
                )
                if (state == 1) {
                    if (height == 0) {
                        showToast(fragment.getString(R.string.tips_setting_up))
                    } else {
                        val isMM =
                            PreferenceManager.getDefaultSharedPreferences(fragment.applicationContext)
                                .getBoolean(
                                    RackViewModel.MEMORY_UNIT + repository.mDevice.mac, true
                                )
                        if (isMM) {
                            controlHight(height)
                        } else {
                            val temp = height.toFloat() / 10.0f
                            val tempHeight = (temp / 0.039370).toInt()
                            controlHight(tempHeight)
                        }
                    }
                } else if (state == 2) {
                    showToast(fragment.getString(R.string.setting_up_success))
                    PreferenceManager.getDefaultSharedPreferences(fragment).edit {
                        putInt(RackViewModel.MEMORY_HEIGHT + repository.mDevice.mac, currentHeight)
                    }
                    PreferenceManager.getDefaultSharedPreferences(fragment).edit {
                        putBoolean(RackViewModel.MEMORY_UNIT + repository.mDevice.mac, unit.value == 0)
                    }
                } else
                    controlKey(0, false)
            }

            if (buttonId == 3) {
                if (state == 1)
                    controlKey(1, false)
                else if (state == 2)
                    controlKey(1, true)
                else
                    controlKey(0, false)
            }
            if (buttonId == 4) {
                if (state == 1)
                    controlKey(2, false)
                else if (state == 2)
                    controlKey(2, true)
                else
                    controlKey(0, false)
            }
        }, fragment)
    }

    /**
     * 高度控制
     */
    fun controlHight(height: Int) {
        val writen = ByteArray(20)
        writen[0] = 0xfd.toByte()
        writen[1] = 0x80.toByte()
        writen[2] = 0x00
        writen[3] = (height shr 8 and 0xff).toByte()
        writen[4] = (height shr 0 and 0xff).toByte()
        writen[19] = 0x02.toByte()
        sendData(writen)
    }

    private fun controlKey(key: Int, isLong: Boolean) {
        val writen = ByteArray(20)
        writen[0] = 0xfd.toByte()
        writen[1] = key.toByte()
        writen[2] = if (isLong) 1.toByte() else 0.toByte()
        writen[19] = 0x02.toByte()
        sendData(writen)
    }

    /**
     * 控制前的连接状态检查
     */
    private fun preStateCheck(block: () -> Unit, fragment: FragmentActivity) {


        if (Connted) {
            launch {
                block()
            }
        } else {
            dismissDialog()
            showToast(fragment.getString(R.string.device_offilne))
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
    val scope = CoroutineScope(Dispatchers.IO)
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