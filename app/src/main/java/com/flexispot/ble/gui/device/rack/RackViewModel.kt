package com.flexispot.ble.gui.device.rack

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.koylo.ble.core.Callback
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.R
import com.flexispot.ble.data.OriginOpe
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.device.rack.RackRepository
import com.flexispot.ble.gui.device.media.MediaViewModel.TAG.MEMORY_HEIGHT_SIT
import com.flexispot.ble.gui.device.media.MediaViewModel.TAG.MEMORY_HEIGHT_STAND
import com.flexispot.ble.gui.view.OnBluethTableListener
import com.luman.mvvm.base.LuManViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

/**
 * @author luman
 * @date 19-11-26
 **/
class RackViewModel(private val repository: RackRepository) : LuManViewModel() {

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
        unit.value = 0
    }

    private val listener: OnBluethTableListener = object :
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
                repository.sendMsg(writen)
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

    private val originOpe = OriginOpe(listener)

    fun deviceInit(device: Device) {
        unit.value = 0
        heightForShow.value = "0"
        repository.initDevice(device)
    }

    /**
     * 设备连接
     */
    fun connect(fragment: FragmentActivity) {
        memoryHeightForSit = PreferenceManager.getDefaultSharedPreferences(fragment)
            .getInt(MEMORY_HEIGHT_SIT + repository.mDevice.mac, -1)
        memoryHeightForStand = PreferenceManager.getDefaultSharedPreferences(fragment)
            .getInt(MEMORY_HEIGHT_STAND + repository.mDevice.mac, -1)
        launch {
            repository.connect(fragment, callback)
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
     * 占用清除
     */
    fun clear() {
        repository.clear()
    }

    private val callback = object : Callback() {
        override fun disconnected(device: BluetoothDevice) {
            super.disconnected(device)
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    connectState.value = false
                }
            }
        }

        override fun serviceBack(device: BluetoothDevice, services: List<BluetoothGattService>) {
            super.serviceBack(device, services)
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    connectState.value = true
                }
            }
        }

        override fun dataBack(device: BluetoothDevice, data: ByteArray) {
            super.dataBack(device, data)
            originOpe.opeData(data)
        }

        override fun deviceBack(device: BluetoothDevice, type: DeviceType, secondType: Int) {
            super.deviceBack(device, type, secondType)
            if (device.address == repository.mDevice.mac) {
                repository.straightConnect(device)
                repository.searchEnd()
            }
        }

        override fun searchEnd() {
            super.searchEnd()
            connectState.value = true
        }
    }

    /**
     * 响应事件
     */
    fun onDeviceButtonChick(buttonId: Int, state: Int, fragment: FragmentActivity) {
        showDialog()
        preStateCheck({
            if (buttonId == 1) {
                val height = PreferenceManager.getDefaultSharedPreferences(fragment).getInt(
                    MEMORY_HEIGHT + repository.mDevice.mac, -1
                )

                if (state == 1) {
                    if (height == 0) {
                        showToast(fragment.getString(R.string.tips_setting_down))
                    } else {
                        val isMM =
                            PreferenceManager.getDefaultSharedPreferences(fragment.applicationContext)
                                .getBoolean(
                                    MEMORY_UNIT + repository.mDevice.mac, true
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
                        putInt(MEMORY_HEIGHT + repository.mDevice.mac, currentHeight)
                    }
                    PreferenceManager.getDefaultSharedPreferences(fragment).edit {
                        putBoolean(MEMORY_UNIT + repository.mDevice.mac, unit.value == 0)
                    }
                    Log.d("currentHeight", "currentHeight=$currentHeight,unit=$unit")
                } else
                    controlKey(0, false)
            }

            if (buttonId == 2) {
                val height = PreferenceManager.getDefaultSharedPreferences(fragment).getInt(
                    MEMORY_HEIGHT + repository.mDevice.mac, -1
                )
                if (state == 1) {
                    if (height == 0) {
                        showToast(fragment.getString(R.string.tips_setting_up))
                    } else {
                        val isMM =
                            PreferenceManager.getDefaultSharedPreferences(fragment.applicationContext)
                                .getBoolean(
                                    MEMORY_UNIT + repository.mDevice.mac, true
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
                        putInt(MEMORY_HEIGHT + repository.mDevice.mac, currentHeight)
                    }
                    PreferenceManager.getDefaultSharedPreferences(fragment).edit {
                        putBoolean(MEMORY_UNIT + repository.mDevice.mac, unit.value == 0)
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
        repository.sendMsg(writen)
    }

    private fun controlKey(key: Int, isLong: Boolean) {
        val writen = ByteArray(20)
        writen[0] = 0xfd.toByte()
        writen[1] = key.toByte()
        writen[2] = if (isLong) 1.toByte() else 0.toByte()
        writen[19] = 0x02.toByte()
        repository.sendMsg(writen)
    }

    /**
     * 控制前的连接状态检查
     */
    private fun preStateCheck(block: () -> Unit, fragment: FragmentActivity) {
        if (connectState.value != null && connectState.value!!) {
            launch {
                block()
            }
        } else {
            dismissDialog()
            showToast(fragment.getString(R.string.device_offilne))
        }
    }

    private fun loadingHeight(height: Int) {
        var tempHeight = height
        if (unit.value == 0)
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    heightForShow.value = tempHeight.toString()
                }
            }
        else {
            val temp = tempHeight.toFloat() / 10.0f
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    heightForShow.value = decimalFormat.format(temp)
                }
            }
        }
    }


}