package com.koylo.ble.core

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.koylo.ble.JavaTool
import com.koylo.ble.core.ErrorCode.SERVICE_NOT_FOUND
import com.koylo.ble.flexispot.DeviceType
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.bluetooth.BluetoothGatt
import kotlinx.coroutines.Job
import java.lang.Exception





class BluetoothTool constructor(/*参数对象*/val builder: Builder) {

    /**
     * 静态参数
     */
    companion object {
        //开启蓝牙请求码
        const val OPEN_BLUETOOTH_CODE = 134
        //申请权限请求码
        const val REQUEST_PERMISSION_CODE = 135
        var times = 0

        //读写通道缓存，value值固定长度2 前读后写
        private val bleCharacteristicList: HashMap<String, CharacteristicDto> = HashMap()
    }

    //各连接设备的写操作关联集，非单次读写时开启
    private val writeJobMap: HashMap<String, Job> by lazy { HashMap<String, Job>() }
    //写数据的控制
    private val writeState: HashMap<String, Boolean> by lazy { HashMap<String, Boolean>() }
    //待发送数据队列集
    private val dataMap: HashMap<String, LinkedBlockingDeque<ByteArray>> by lazy { HashMap<String, LinkedBlockingDeque<ByteArray>>() }
    //ble适配器，用于判断蓝牙状态，获取扫描对象
    private var bleAdapter: BluetoothAdapter? = null
    //ble扫描器，用于开启和关闭扫描
    private var bleScanner: BluetoothLeScanner? = null
    //设备地址和连接通道的关联集，避免重新获取连接参数浪费资源，onDestroy()之前都缓存着
    private val bleGattList: HashMap<String, BluetoothGatt> by lazy { HashMap<String, BluetoothGatt>() }
//    private var bleGatt : BluetoothGatt? = null
    //协程工作列表
    private val jobList by lazy {
        ArrayList<Job>()
    }
    //已创建Lopper的线程名集
    private val lopperList by lazy {
        ArrayList<String>()
    }
    //停止扫描线程
    private var cancelScanRunnable: Runnable? = null
    private val handler by lazy {
        Handler()
    }
    val scope = CoroutineScope(Dispatchers.IO)

    //声明周期绑定，随着绑定组建的销毁而自我销毁
    init {
//        if (builder.fragment() == null) {
//                builder.context()?.lifecycle?.addObserver(LifeCycleListener())
//        } else {
//                builder.fragment()?.lifecycle?.addObserver(LifeCycleListener())
//        }
    }

    /**
     * 检查蓝牙是否可用
     */
    fun checkBleEnable(): Boolean {
        val manager =
            builder.context()!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = manager.adapter

        if (bleAdapter == null) {
            builder.callback()?.error(ErrorCode.BLUETOOTH_DISABLE)
            BLog.e("蓝牙不可用")
            return false
        } else if (!builder.context()!!.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            builder.callback()?.error(ErrorCode.BLE_UNSUPPORTED)
            BLog.e("当前设备不支持BLE")
            return false
        } else if (!bleAdapter!!.isEnabled) {
            builder.callback()?.error(ErrorCode.BLUETOOTH_NOT_OPEN)

            return false
        }
        return true
    }

    /**
     * 开启蓝牙
     */
    fun openBle() {
        builder.context()!!.startActivityForResult(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            OPEN_BLUETOOTH_CODE
        )
    }

    /**
     * 检查权限
     */
    fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            val state = ContextCompat.checkSelfPermission(
                builder.context()!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (state != PackageManager.PERMISSION_GRANTED) {
                BLog.e("无蓝牙权限")
                builder.callback()?.error(ErrorCode.PERMISSION_DENIED)
                if (builder.autoDealBluetoothPermission()) {
                    BLog.d("尝试蓝牙权限获取")
                    ActivityCompat.requestPermissions(
                        builder.context()!!,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_PERMISSION_CODE
                    )
                }
                return false
            }
        }
        return true
    }

    /**
     * 搜索周边设备
     */
    fun searchDevices() {
        if (cancelScanRunnable != null) {
            BLog.d("扫描进行中")
            return
        }
        preCheck {
            bleScanner = bleAdapter!!.bluetoothLeScanner
            if (builder.filterName() != null) {
                val filer = ScanFilter.Builder().setDeviceName(builder.filterName()!!).build()
                val filters = ArrayList<ScanFilter>()
                filters.add(filer)
                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
                bleScanner?.startScan(
                    filters, scanSettings,
                    scanCallback
                )
            } else {
                bleScanner?.startScan(scanCallback)
            }
            Log.d("开始", "搜索")
            cancelScanRunnable = Runnable {
                bleScanner?.stopScan(scanCallback)
                builder.callback()?.searchEnd()
                if (cancelScanRunnable != null) {
                    handler.removeCallbacks(cancelScanRunnable)
                    cancelScanRunnable = null
                }
            }
            preHandler {
                handler.postDelayed(cancelScanRunnable, builder.scanPeriod())
            }
        }
    }

    /**
     * 连接设备
     */
    fun connectDevice(device: BluetoothDevice) {
        preCheck {
//            if (bleGatt != null && bleGatt!!.device.address == device.address) {
                if (bleGattList.containsKey(device.address)) {
                    bleGattList[device.address]?.connect()
                    BLog.d("已有该设备，直接尝试连接")
//                bleGatt?.connect()
                } else {
                    BLog.d("开始构建蓝牙连接")
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (times % 2 == 0) {
                            device.connectGatt(
                                builder.context()!!,
                                true,
                                bleCallback
                                , BluetoothDevice.TRANSPORT_LE
                            )
                        } else {
                            device.connectGatt(
                                builder.context()!!,
                                true,
                                bleCallback
                            )
                        }
                        times++
                    } else {
                        device.connectGatt(
                            builder.context()!!,
                            true,
                            bleCallback
                        )
                    }
                }
            }

    }

    /**
     * 通过mac地址连接设备
     */
    fun connectDevice(mac: String) {
        preCheck {
            val remoteDevice = bleAdapter!!.getRemoteDevice(mac)
            if (remoteDevice == null || remoteDevice.name.isNullOrBlank()) {
                BLog.d("无设备，进行搜索")
                searchDevices()
            } else {
                BLog.d("有设备，尝试连接")
                connectDevice(remoteDevice)
            }
        }
    }
    fun getDevice(mac: String): BluetoothDevice {
        val remoteDevice = bleAdapter!!.getRemoteDevice(mac)
        return remoteDevice
    }



    /**
     * 数据发送
     */
    fun sendData(address: String, data: ByteArray) {
        if (dataMap[address] == null) {
            dataMap[address] = LinkedBlockingDeque()
        }
        dataMap[address]!!.offer(data)

    }

    /**
     * 根据uuid筛选可用数据通道
     */
    private fun filterService(gatt: BluetoothGatt) {
        preCheck {
            bleCharacteristicList[gatt.device.address] = CharacteristicDto()
            val tempService = gatt.getService(UUID.fromString(builder.uuids().serviceUuid))
            if (tempService != null) {
                val tempRead =
                    tempService.getCharacteristic(UUID.fromString(builder.uuids().readUuid))
                if (tempRead != null) {
                    bleCharacteristicList[gatt.device.address]!!.read = tempRead
                }
                val tempWrite =
                    tempService.getCharacteristic(UUID.fromString(builder.uuids().writeUuid))
                if (tempWrite != null) {
                    bleCharacteristicList[gatt.device.address]!!.write = tempWrite
                }
            }
            if (tempService == null || (bleCharacteristicList[gatt.device.address]!!.read == null
                        || bleCharacteristicList[gatt.device.address]!!.write == null)
            ) {
                BLog.d("服务读写通道有一项匹配失败，将主动断开")
                builder.callback()?.error(ErrorCode.UUID_ERROR)
                BLog.d("服务读写通道有一项匹配失败，将主动断开2")
                bleGattList[gatt.device.address]?.disconnect()
                bleGattList[gatt.device.address]?.close()
                println(bleCharacteristicList[gatt.device.address]!!.read == null)
                println( bleCharacteristicList[gatt.device.address]!!.write == null)
//                bleGatt?.disconnect()
//                bleGatt?.close()


            } else {
                BLog.d("进入服务")
                gatt.setCharacteristicNotification(
                    bleCharacteristicList[gatt.device.address]!!.read,
                    true
                )
                beginSend(gatt.device.address)
            }
        }
    }

    /**
     * 清空指定mac地址的消息队列
     */
    fun msgClear(address: String) {
        if (!dataMap.containsKey(address)) {
            return
        }
        Log.d("我", "已经清楚消息队列了")
        dataMap[address]?.clear()
    }

    /**
     * 开启写数据操作
     * @param address 设备mac地址 格式AA:BB:CC:DD:EE:FF
     */
    private fun beginSend(address: String) {
        if (!bleCharacteristicList.containsKey(address) || bleCharacteristicList[address]!!.write == null) {
            BLog.e("尝试对无写通道的设备进行写操作")
            return
        }
        preCheck {
            if (writeJobMap.containsKey(address)) {
                writeJobMap[address]?.cancel()
            }
            if (!dataMap.containsKey(address)) {
                dataMap[address] = LinkedBlockingDeque()
            }
            if (!writeState.containsKey(address) || writeState[address] == false) {
                writeState[address] = true
            }
            writeJobMap[address] = GlobalScope.launch {
                while (writeState[address]!!) {
                    BLog.d("该消息队列长度：${dataMap[address]!!.size}")
                    val value = dataMap[address]!!.take()
                    if (value.size == 1 && value[0] == 0xFF.toByte()) {
                        BLog.d("收到结束符，结束消息队列")
                        writeJobMap[address]?.cancel()
                        writeJobMap.remove(address)
                        break
                    }
                    bleCharacteristicList[address]!!.write!!.value = value
//                    bleGatt?.writeCharacteristic(bleCharacteristicList[address]!!.write)
                    bleGattList[address]!!.writeCharacteristic(bleCharacteristicList[address]!!.write)
                    BLog.d("向设备${address}投递了数据：${JavaTool.bytesToHex(value)}")
                    //下次数据发送间隔50ms
                    delay(
                        50
                    )
                }
            }
        }
    }

    /**
     * 任何设备操作前做预检查
     */
    private fun preCheck(code: () -> Unit) {
        if (builder.context() == null && builder.fragment() == null) {
            //上下文对象缺失
            BLog.e("上下文对象缺失")
            builder.callback()?.error(ErrorCode.CONTEXT_MISSED)
            onDestroy()
            return
        }
        if (!checkBleEnable()) {
            BLog.e("蓝牙未开启")
            return
        }
        if (!checkPermission()) {
            BLog.e("权限未开启")
            return
        }
        jobList.add(scope.launch {
            code.invoke()
        })
    }

    /**
     * 运行Handler前的准备工作
     */
    private fun preHandler(code: () -> Unit) {
        if (!lopperList.contains(Thread.currentThread().name)) {
            Looper.prepare()
            code.invoke()
            Looper.loop()
            lopperList.add(Thread.currentThread().name)
        }
    }

    /**
     * 与设备断开连接
     */
    fun disconnectDevice(serial: String) {
        preCheck {
            if (bleGattList.containsKey(serial)) {
                bleGattList[serial]?.disconnect()
                bleGattList[serial]?.close()
//            if(bleGatt != null && bleGatt!!.device.address == serial) {
//                bleGatt?.disconnect()
//                bleGatt?.close()
            }
        }

    }

    /**
     * 停止搜索
     */
    fun stopSearch() {
        preCheck {
            bleScanner?.stopScan(scanCallback)
            if (cancelScanRunnable != null) {
                handler.removeCallbacks(cancelScanRunnable)
                cancelScanRunnable = null
            }
        }
    }

    /**
     * 清除占用
     */
    fun onDestroy() {
        for ((_, job) in writeJobMap) {
            job.cancel()
        }
        writeJobMap.clear()
        //清理所有协程
        for (job in jobList) {
            job.cancel()
        }
        jobList.clear()
        //关闭停止搜索线程
        if (cancelScanRunnable != null) {
            handler.removeCallbacks(cancelScanRunnable)
            cancelScanRunnable = null
        }
        //停止搜索
        if (bleScanner != null) {
            if (bleAdapter != null && bleAdapter!!.isEnabled) {
                bleScanner!!.stopScan(scanCallback)
            }
            bleScanner = null
        }
        //关闭所有写数据任务
        for ((address, state) in writeState) {
            if (state) {
                writeState[address] = false
                dataMap[address]?.clear()
                dataMap[address]?.offer(ByteArray(1) {
                    0xFF.toByte()
                })
            }
        }
        writeState.clear()
        //清理所有待发送数据
        dataMap.clear()
        //关闭和清理所有连接
        if (bleGattList.size > 0) {
            if (bleAdapter != null && bleAdapter!!.isEnabled) {
                for ((_, gatt) in bleGattList) {
                    gatt.disconnect()
                    gatt.close()
                }
            }
            bleGattList.clear()
        }
//        bleGatt?.disconnect()
//        bleGatt?.close()
        builder.clearContext()
    }

    fun cleanLianJie(){
        if (bleGattList.size > 0) {
            if (bleAdapter != null && bleAdapter!!.isEnabled) {
                for ((_, gatt) in bleGattList) {
                    gatt.disconnect()
                    gatt.close()
                }
            }
            bleGattList.clear()
        }
    }

    /**
     * 生命周期监听回调
     */
    private inner class LifeCycleListener : LifecycleObserver {
        /**
         * 绑定的activity/fragment生命周期结束
         */
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun componentOnDestroy() {
            onDestroy()
        }
    }
private  var isRunOnServicesDiscovered:Boolean=false
    private  var num:Int=0
    private    var state=false;

    //连接回调，避免重复创建回调对象,onDestroy()之前都缓存着
    private val bleCallback: BluetoothGattCallback by lazy {
        object : BluetoothGattCallback() {

            //连接状态变更
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (gatt != null && gatt.device != null) {
                    if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
                        BLog.d("设备${gatt.device.name}，已断开")
                        if(status == 133 && bleGattList.containsKey(gatt.device.address)){
                            bleGattList[gatt.device.address]?.close()
                            bleGattList.remove(gatt.device.address)
                        }
//                        if(status == 133 && bleGatt != null && bleGatt!!.device.address == gatt.device.address){
////                            bleGattList.remove(gatt.device.address)
//                            bleGatt?.close()
//                            bleGatt = null
//                        }
                        builder.callback()?.disconnected(gatt.device)
                        if (writeJobMap.containsKey(gatt.device.address)) {
                            writeState[gatt.device.address] = false
                            dataMap[gatt.device.address]?.clear()
                            dataMap[gatt.device.address]?.offer(ByteArray(1) {
                                0xFF.toByte()
                            })
                        }
                    } else if (newState == BluetoothAdapter.STATE_CONNECTED) {
                        BLog.d("设备${gatt.device.name}，已连接")
                        if(builder.context() == null && builder.fragment() == null){
                            //页面都关掉了
                            gatt.disconnect()
                            gatt.close()
                            return
                        }
                        bleGattList[gatt.device.address] = gatt

                        val state = gatt.discoverServices()
                        if (state) {
                            builder.callback()?.connected(gatt.device)
                        } else {
                            builder.callback()?.error(SERVICE_NOT_FOUND)
                            builder.callback()?.disconnected(gatt.device)
                        }

//                        bleGatt = gatt
//                        val state = gatt.discoverServices()

//                        while (!isRunOnServicesDiscovered&&num<10) {
//                            try {
//                                Thread.sleep(500)
//                                num++;
//                            } catch (e: InterruptedException) {
//                                e.printStackTrace()
//                            }
//                            if (!isRunOnServicesDiscovered){
//                                state=gatt.discoverServices()
//                            }
//                            if (num==10){
//                                builder.callback()?.error(SERVICE_NOT_FOUND)
//                                builder.callback()?.disconnected(gatt.device)
//                            }
//
//                        }

                    }
                }
            }

            //服务发现
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)

//                isRunOnServicesDiscovered=true
//                if (state){
//                    builder.callback()?.connected(gatt!!.device)
//                }

                if (gatt != null && gatt.device != null) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        BLog.d("设备${gatt.device.name}，服务获取成功")
                        builder.callback()?.serviceBack(gatt.device, gatt.services)
                        //已存储读写通道特性
                        if (bleCharacteristicList.containsKey(gatt.device.address)) {
                            val characteristicList = bleCharacteristicList[gatt.device.address]!!
                            if(characteristicList.read == null && characteristicList.write == null){
                                filterService(gatt)
                            }else {
                                if (characteristicList.read != null) {
                                    gatt.setCharacteristicNotification(
                                        characteristicList.read,
                                        true
                                    )
                                }
                                //可直接发送数据
                                if (characteristicList.write != null) {
                                    BLog.d("尝试直接进行数据发送")
                                    beginSend(gatt.device.address)
                                }
                            }
                        } else {
                            //服务比对，找到读写通道特性
                            filterService(gatt)
                        }
                    } else {
                        BLog.d("设备${gatt.device.name}，服务获取失败，将主动断开")
                        if (bleGattList.containsKey(gatt.device.address)) {
                            bleGattList[gatt.device.address]?.disconnect()
                            bleGattList[gatt.device.address]?.close()
                        }
//                        bleGatt?.disconnect()
//                        bleGatt?.close()
                    }
                }
            }

            //数据发送状态
            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                if (gatt != null && gatt.device != null) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        BLog.d("\"设备${gatt.device.name}，数据投递成功")
                        builder.callback()?.sendSuccess(gatt.device)
                    } else {
                        BLog.e("设备${gatt.device.name}，数据投递失败")
                        builder.callback()?.sendFailed(gatt.device)
                    }
                }
            }

            //数据接收
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                if (gatt != null && gatt.device != null && characteristic != null) {
                    BLog.d(
                        "读取到设备${gatt.device.name}发来的消息：${JavaTool.bytesToHex(
                            characteristic.value
                        )}"
                    )
                    builder.callback()?.dataBack(gatt.device, characteristic.value)
                    if (builder.isOpeOnce() && bleGattList.containsKey(gatt.device.address)) {
                        bleGattList[gatt.device.address]?.disconnect()
                        bleGattList[gatt.device.address]?.close()
                    }
//                    bleGatt?.disconnect()
//                    bleGatt?.close()
                }
            }
        }
    }

    //设备扫描回调,避免重复创建回调对象,onDestroy()之前都缓存着
    private val scanCallback: ScanCallback by lazy {
        object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                builder.callback()?.searchEnd()
                BLog.e("扫描失败")
                if (cancelScanRunnable != null) {
                    handler.removeCallbacks(cancelScanRunnable)
                    cancelScanRunnable = null
                }
                builder.callback()?.error(ErrorCode.SCAN_FAILED)
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                val device = result?.device
                BLog.d("搜到设备,设备地址：${device?.address}；设备名：${device?.name}")
                if (device != null && !device.name.isNullOrBlank()) {
                    when {
                        device.name.startsWith("MTS") -> {
                            builder.callback()?.deviceBack(device, DeviceType.RACK, 0)
                        }
                        device.name.startsWith("EMW") -> {
                            builder.callback()?.deviceBack(device, DeviceType.MEDIA, 0)
                        }
                        device.name.startsWith("BTD") || device.name.startsWith("BDT") -> {
                            builder.callback()?.deviceBack(device, DeviceType.DESK, 0)
                        }
                        device.name.startsWith("FT01") -> {
                            builder.callback()?.deviceBack(device, DeviceType.THREAD, 0)
                        }
                    }
                }
            }
        }
    }

    private fun checkHead(str: String, head: String): Boolean {
        return str.startsWith(head)
    }

}