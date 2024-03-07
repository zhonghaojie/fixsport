package com.walking.secretary.confignetwork

import android.bluetooth.BluetoothGatt
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.flexispot.ble.gui.threadMill.TreadMillRepository
import com.koylo.ble.flexispot.flexispotUUID

import com.luman.mvvm.base.LuManViewModel

import okhttp3.internal.and

import java.util.concurrent.locks.ReentrantLock

/**
 * Created by ts on 18-9-26.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class TreadMillModel(val repository: TreadMillRepository) : LuManViewModel() {

    //当前已连接的设备
    private var curBleDevice: BleDevice? = null

    private var LianjieNumber = 0

    private var ss = ""
    private var sss = ""
    private var ssss= ""
    private var mListener: OnOperationListener? = null


    private var mController: TreadMillModel? = null

    private val lock = ReentrantLock()

    @Synchronized
    fun getInstance(): TreadMillModel {
        if (mController == null)
            mController = TreadMillModel(repository)
        return mController as TreadMillModel
    }


    init {
        //初始化
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(0, 500)
            .setConnectOverTime(10000).operateTimeout = 10000
    }


    fun startScan() {
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
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
                isConnect=false

            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                // 连接成功，BleDevice即为所连接的BLE设备（主线程）
                startNotify(bleDevice)
                curBleDevice = bleDevice
                LianjieNumber = 0
                isConnect=true

            }


            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                // 连接断开，isActiveDisConnected是主动断开还是被动断开（主线程）

                if (mListener != null) {
                    mListener!!.onConnectFail()
                }
                isConnect=false


            }
        })
    }

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

    private var notfi = 0;
    private fun startNotify(bleDevice: BleDevice) {

        BleManager.getInstance().notify(
            bleDevice,
            flexispotUUID.SERVICE_UUID, //数据树妖替换
            flexispotUUID.walking_MEASUREMENT_POINT,
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    // 打开通知操作成功
                    notfi = 0;
                    if (mListener != null) {
                        mListener!!.onConnectSuccess()
                    }
                    isConnect=true
                }

                override fun onNotifyFailure(exception: BleException) {
                    // 打开通知操作失败
//                    if (mListener != null) {
//                        mListener!!.onConnectFail()
//                    }
//                    notfi++
//                    if (notfi < 3) {
//                        startNotify(bleDevice)
//                    }else{
                        if (mListener != null) {
                            mListener!!.onConnectFailService()
 //                       }
                    }
                    isConnect=false

                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    // 打开通知后，设备发过来的数据将在这里出现
                    //                        Log.e(TAG, "onCharacteristicChanged " + "==============");
                    //                        Log.e(TAG, "onCharacteristicChanged " + DeviceProtocol.BytetohexString(data));
                    //                        Log.e(TAG, "onCharacteristicChanged " + Arrays.toString(data));

                    try {
                        HandleProtocol(data)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })


    }

    private fun stopNotify(bleDevice: BleDevice) {
        BleManager.getInstance()
            .stopNotify(bleDevice, flexispotUUID.SERVICE_UUID, flexispotUUID.walking_MEASUREMENT_POINT)
    }


    //1：进入手动模式2：进入自动模式 3：进入休眠模式 4：速度加 5：速度减 6：开始/停止7：开启遥控器对码8:开启童锁 9:解除童锁 12：清楚当前数据 15英制 16公制
    fun setkey(key: Int) {
        if (ss == "童锁") {
            setkey2()
            ss = "自动"
            object : Thread() {
                override fun run() {
                    super.run()
                    try {
                        Thread.sleep(2000)//休眠2秒
                        setkey3(key)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }.start()
        } else {
            setkey3(key)
        }


    }


    fun setkey2() {

        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 7.toByte()
            writen[3] = 0xfd.toByte()
            writen[4] = 9.toByte()
            writen[5] = 0x0d.toByte()
            writen[6] = 0x5a.toByte()
            bleWrite(writen)
        } finally {
            lock.unlock()
        }
    }


    fun setkey3(key: Int) {
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 7.toByte()
            writen[3] = 0xfd.toByte()
            if (key == 1) {
                writen[4] = 1.toByte()
                writen[5] = 5.toByte()
            } else if (key == 2) {
                writen[4] = 2.toByte()
                writen[5] = 6.toByte()
            } else if (key == 3) {
                writen[4] = 3.toByte()
                writen[5] = 7.toByte()
            } else if (key == 4) {
                writen[4] = 4.toByte()
                writen[5] = 8.toByte()
            } else if (key == 5) {
                writen[4] = 5.toByte()
                writen[5] = 9.toByte()
            } else if (key == 6) {
                writen[4] = 6.toByte()
                writen[5] = 0x0a.toByte()
            } else if (key == 7) {
                writen[4] = 7.toByte()
                writen[5] = 0x0b.toByte()
            } else if (key == 8) {
                writen[4] = 8.toByte()
                writen[5] = 0x0c.toByte()
            } else if (key == 9) {
                writen[4] = 9.toByte()
                writen[5] = 0x0d.toByte()
            } else if (key == 12) {
                writen[4] = 0x12.toByte()
                writen[5] =
                    ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff)).toByte()
            } else if (key == 15) {
                writen[4] = 0x15.toByte()
                writen[5] =
                    ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff)).toByte()
            } else if (key == 16) {
                writen[4] = 0x16.toByte()
                writen[5] =
                    ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff)).toByte()
            }
            writen[6] = 0x5a.toByte()
            bleWrite(writen)
            //            String str = bytes2HexString(writen);
            //            System.out.println(str+"ppppppppppppp");
            //            System.out.println(key + "ppppppppppppppppppppppkey数字");
        } finally {
            lock.unlock()
        }
    }


    //设置保护距离和时间/   距离/mm  时间/0.5s
    fun setFixed(length: Int, time: Int) {
        val a = length / 256
        val b = length % 256
        val c = time / 256
        val d = time % 256
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 10.toByte()
            writen[3] = 0xf5.toByte()
            writen[4] = a.toByte()
            writen[5] = b.toByte()
            writen[6] = c.toByte()
            writen[7] = d.toByte()
            writen[8] =
                ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff) + (writen[5] and 0xff) + (writen[6] and 0xff) + (writen[7] and 0xff)).toByte()
            writen[9] = 0x5a.toByte()
            bleWrite(writen)

        } finally {
            lock.unlock()
        }
    }


    //设置休眠和锁定时间
    fun setDormancy(dormancy: Int, chip: Int) {
        val a = dormancy / 256
        val b = dormancy % 256
        val c = chip / 256
        val d = chip % 256
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 10.toByte()
            writen[3] = 0xf4.toByte()
            writen[4] = a.toByte()
            writen[5] = b.toByte()
            writen[6] = c.toByte()
            writen[7] = d.toByte()
            writen[8] =
                ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff) + (writen[5] and 0xff) + (writen[6] and 0xff) + (writen[7] and 0xff)).toByte()
            writen[9] = 0x5a.toByte()
            bleWrite(writen)

        } finally {
            lock.unlock()
        }
    }


    //读取运转参数
    fun getFixed() {
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 9.toByte()
            writen[3] = 0xf6.toByte()
            writen[7] =
                ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[7] and 0xff)).toByte()
            writen[8] = 0x5a.toByte()
            bleWrite(writen)

        } finally {
            lock.unlock()
        }
    }


    //获取设备信息
    fun getDeviceInfo() {
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 6.toByte()
            writen[3] = 0xfe.toByte()
            writen[4] = 4.toByte()
            writen[5] = 0x5a.toByte()

            bleWrite(writen)

        } finally {
            lock.unlock()
        }
    }

    //获取运动数据
    fun getMotionRecord(uid: Int) {
        lock.lock()
        val id = uid % 256
        val idd = uid / 256

        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 8.toByte()
            writen[3] = 0xf8.toByte()
            writen[4] = idd.toByte()
            writen[5] = id.toByte()
            writen[6] =
                ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff) + (writen[5] and 0xff)).toByte()
            writen[7] = 0x5a.toByte()

            bleWrite(writen)
            //            String str = bytes2HexString(writen);

        } finally {
            lock.unlock()
        }
    }


    //清除历史运动数据
    fun clean() {
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 9.toByte()
            writen[3] = 0xf7.toByte()
            writen[4] = 1.toByte()
            writen[5] = 0.toByte()
            writen[6] = 0.toByte()
            writen[7] = 1.toByte()
            writen[8] = 0x5a.toByte()

            bleWrite(writen)
            //            String str = bytes2HexString(writen);

        } finally {
            lock.unlock()
        }
    }
private var isConnect=false;

    private fun bleWrite(byteArray: ByteArray) {
        if (!isConnect){
            if (mListener!=null){
                mListener!!.onConnectFail()
            }
            return
        }
        BleManager.getInstance().write(
            curBleDevice,
            flexispotUUID.SERVICE_UUID,
            flexispotUUID.walking_CONTROL_POINT,
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

    @Throws(Exception::class)
    private fun HandleProtocol(data: ByteArray?): Boolean {
        if (data != null) {
                        var str = bytes2HexString(data);
            if (data[3].toInt() == 0x00 && data.size > 0) {
//                println("ppppppppppppp1")
                //A5 FA 14 00 00 22 18 28 00 01 00 11 00 01 00 00 3B 50 14 5A
                //设备回复实时数据
                val speed = (data[6] and 0xff).toDouble()//实时速度
                val length = (data[8].toInt() and 0xff) * 256 + (data[9].toInt() and 0xff)
                val time = (data[10].toInt() and 0xff) * 256 + (data[11].toInt() and 0xff)
                val step = (data[12].toInt() and 0xff) * 256 + (data[13].toInt() and 0xff)
                val stat = (data[4].toInt() and 0xff) * 256 + (data[5].toInt() and 0xff)
                val id = (data[15].toInt() and 0xff) * 256 + (data[16].toInt() and 0xff)
                val maxSpeed = (data[7] and 0xff).toInt()//设置速度


                val isStarting = if (stat shr 4 and 0x01 == 1) true else false//启动运行
                val isRunning = if (stat shr 5 and 0x01 == 1) true else false//正在运行
                val isStop = if (stat shr 6 and 0x01 == 1) true else false//停止运行
                val isshoudong = if (stat shr 1 and 0x01 == 1) true else false//手动
                val isWait = if (stat shr 7 and 0x01 == 1) true else false//休眠
                val iszidong = if (stat shr 2 and 0x01 == 1) true else false//自动
                val istongsuo = if (stat shr 9 and 0x01 == 1) true else false//童锁
                val isYingZhi = stat shr 11 and 0x01 //是否为英制0公制1英制
                if (iszidong) {
                    ss = "自动"
                    ssss= "自动"
                }
                if (isshoudong) {
                    ss = "手动"
                    ssss= "手动"
                }
                if (istongsuo) {
                    ss = "童锁"
                }
                if (isWait) {
                    ss = "休眠"
                }
                if (isStarting || isRunning) {
                    sss = "运行"
                } else {
                    sss = "停止"
                }


                //                System.out.println(stat + "启动" + isStarting + "运行" + isRunning + "停止" + isStop + "手动" + isshoudong + "自动" + iszidong + "童锁" + istongsuo + "pppppppppppppp");
                //                System.out.println(length + "," + time + "," + step + "," + speed + "pppppppppp" + id);
                //                System.out.println(sss+"ppppppppppppppppppppppppp2222");
                if (mListener != null) {
                    mListener!!.onSpeed(speed, length, time, step, ssss, id, sss, maxSpeed, isYingZhi)
                }

            } else if (data[3].toInt() == 0x01) {
                //设备信息
                val a = data[12] and 0xff
                val b = data[14] and 0xff
                val c = data[15] and 0xff
                mListener?.onGetDeviceInfo(a, b, c)

            } else if (data[3].toInt() == 0x09) {
                val length = (data[4].toInt() and 0xff) * 256 + (data[5].toInt() and 0xff)
                val time = ((data[6].toInt() and 0xff) * 256 + (data[7].toInt() and 0xff)) / 2
                if (mListener != null) {
                    mListener!!.onFixed(length, time)
                }
            } else if (data[3].toInt() == 0x06) {//时间

                val time = getTime(
                    data[9] and 0xff,
                    data[8] and 0xff, data[7] and 0xff, data[6] and 0xff, data[5] and 0xff
                )
                if (mListener != null) {
                    mListener!!.onTreadTime(time, 6)
                }


            } else if (data[3].toInt() == 0x05) {//时间
                val time = getTime(
                    data[9] and 0xff,
                    data[8] and 0xff, data[7] and 0xff, data[6] and 0xff, data[5] and 0xff
                )
                if (mListener != null) {
                    mListener!!.onTreadTime(time, 5)
                }


            } else if (data[3].toInt() == 0x07) {
                val length = (data[10].toInt() and 0xff) * 256 + (data[11].toInt() and 0xff)
                val time = (data[8].toInt() and 0xff) * 256 + (data[9].toInt() and 0xff)
                val step = (data[6].toInt() and 0xff) * 256 + (data[7].toInt() and 0xff)
                val a = data[17].toInt() and 0xff
                val recordTime = getTime(
                    a,
                    data[16].toInt() and 0xff,
                    data[15].toInt() and 0xff,
                    data[14].toInt() and 0xff,
                    data[13].toInt() and 0xff
                )

                //                System.out.println(recordTime+"pppppppppp"+length+","+time+","+step+",");

                if (mListener != null) {
                    if (a == 0) {
                        mListener!!.onRecord(recordTime, length, time, step, 0)
                    } else {
                        mListener!!.onRecord(recordTime, length, time, step, 1)
                    }
                }

            } else if (data[3].toInt() == 0x81) {
                //                System.out.println("ppppppppppppppppppppppppppppppppppppp进来了" + str);
            } else if (data[3].toInt() == 0x0b) {
                val length = (data[4].toInt() and 0xff) * 256 + (data[5].toInt() and 0xff)
            } else if (data[3].toInt() == 0x0d) {

            } else if (data[3].toInt() == 0x08) {

                if (mListener != null) {
                    mListener!!.clean(0, 0)
                }

            }
        }
        return false
    }

    private fun getTime(year: Int, month: Int, day: Int, hours: Int, minute: Int): String {
        var a = ""
        var b = ""
        var c = ""
        var d = ""
        var e = ""
        if (year == 0) {
            a = "00"
        } else if (year < 10) {
            a = "0$year"
        } else {
            a = year.toString() + ""
        }
        if (month > 12) {
            b = "12"
        } else {
            b = if (month < 10) "0$month" else month.toString() + ""
        }
        if (day > 31) {
            c = "31"
        } else {
            c = if (day < 10) "0$day" else day.toString() + ""
        }
        if (hours > 24) {
            d = "24"
        } else {
            d = if (hours < 10) "0$hours" else hours.toString() + ""
        }
        if (minute > 60) {
            e = "60"
        } else {
            e = if (minute < 10) "0$minute" else minute.toString() + ""
        }

        return "20$a-$b-$c $d:$e:00"
    }

    //设置最大速度
    fun setMaxSpeed(speed: Int) {
        lock.lock()
        try {

            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 7.toByte()
            writen[3] = 0xfc.toByte()
            writen[4] = speed.toByte()//10-60
            writen[5] =
                ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff)).toByte()
            writen[6] = 0x5a.toByte()
            bleWrite(writen)

        } finally {
            lock.unlock()
        }
    }

    //校正设备时间
    fun setTime(time: String) {
        // 2019-11-27 14:36:52
        var aa = 0
        var bb = 0
        var cc = 0
        var dd = 0
        var ee = 0
        var ff = 0
        val gg = 0
        var week = 0
        val one = time.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (one.size == 2) {
            val time1 = one[0]//"2019-10-27"
            val time2 = one[1]//"14:13:22
            val a = time1.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val b = time2.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var year = a[0]
            val month = a[1]
            val day = a[2]
            val hours = b[0]
            val minute = b[1]
            val second = b[2]
            if (year.length == 4) {
                year = year.substring(2, 4)
            }
            week = week
            //                System.out.println(year+","+month+","+day+","+hours+","+minute+","+second+"pppppppppppppppp"+week);
            aa = Integer.parseInt(year)
            bb = Integer.parseInt(month)
            cc = Integer.parseInt(day)
            dd = Integer.parseInt(hours)
            ee = Integer.parseInt(minute)
            ff = Integer.parseInt(second)
        }
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 13.toByte()
            writen[3] = 0xfa.toByte()
            writen[4] = ff.toByte()
            writen[5] = ee.toByte()
            writen[6] = dd.toByte()
            writen[7] = cc.toByte()
            writen[8] = bb.toByte()
            writen[9] = aa.toByte()
            writen[10] = week.toByte()
            writen[11] =
                ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff) + (writen[5] and 0xff) + (writen[6] and 0xff) + (writen[7] and 0xff) + (writen[8] and 0xff) + (writen[9] and 0xff) + (writen[10] and 0xff)).toByte()
            writen[12] = 0x5a.toByte()
            bleWrite(writen)

        } finally {
            lock.unlock()
        }
    }

    //读取走步机时间
    fun getThreadTime() {

        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 6.toByte()
            writen[3] = 0xf9.toByte()
            writen[4] = ((writen[2] and 0xff) + (writen[3] and 0xff)).toByte()
            writen[5] = 0x5a.toByte()
            bleWrite(writen)

        } finally {
            lock.unlock()
        }
    }


    //设置运行速度
    fun setFunctionSpeed(speed: Int) {
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 7.toByte()
            writen[3] = 0xfb.toByte()
            writen[4] = speed.toByte()
            writen[5] =
                ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff)).toByte()
            writen[6] = 0x5a.toByte()
            bleWrite(writen)

        } finally {
            lock.unlock()
        }
    }


    fun setShield(a: Int, b: Int, c: Int, d: Int) {
        lock.lock()
        try {
            val writen = ByteArray(20)
            writen[0] = 0xa5.toByte()
            writen[1] = 0xfa.toByte()
            writen[2] = 7.toByte()
            writen[3] = 0xf1.toByte()
            writen[4] = (a or (b shl 1) or (c shl 2) or (d shl 3)).toByte()
            writen[5] =
                ((writen[2] and 0xff) + (writen[3] and 0xff) + (writen[4] and 0xff)).toByte()
            writen[6] = 0x5a.toByte()
            bleWrite(writen)


        } finally {
            lock.unlock()
        }
    }


    interface OnOperationListener {
        fun onScanFailure()  //搜索周围蓝牙设备失败

        fun onStartConnect()  //开始连接（主线程）

        fun onConnectSuccess()  //蓝牙设备连接成功

        fun onConnectFail()  //蓝牙设备连接失败

        fun onConnectFailService()  //蓝牙服务设备连接失败


        fun onGetDeviceInfo(maxheight: Int, minheight: Int, unit: Int)  //获取连接的蓝牙设备信息

        fun onGetScanDevice(scanResult: BleDevice) //获取扫描到的蓝牙设备
        fun onSpeed(
            speed: Double,
            length: Int,
            time: Int,
            step: Int,
            stats: String,
            id: Int,
            run: String,
            maxspeed: Int,
            isYingZhi: Int
        )

        fun onMaxSpeed(speed: Double)

        fun onFixed(length: Int, time: Int)

        fun onRecord(recordTime: String, length: Int, time: Int, step: Int, num: Int)

        fun onTreadTime(time: String, number: Int)

        fun process(now: Int, max: Int)

        fun clean(a: Int, b: Int)


    }

    fun setOnOperationListener(mListener: OnOperationListener) {
        this.mListener = mListener
    }


    fun bytes2HexString(b: ByteArray): String {
        var r = ""

        for (i in b.indices) {
            var hex = Integer.toHexString(b[i] and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
            }
            r += hex.toUpperCase()
        }

        return r
    }


}
