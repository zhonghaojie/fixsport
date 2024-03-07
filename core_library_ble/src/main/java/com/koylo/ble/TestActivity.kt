package com.koylo.ble

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.koylo.ble.core.BluetoothTool
import com.koylo.ble.core.Builder
import com.koylo.ble.core.Callback
import com.koylo.ble.flexispot.DeviceType

class TestActivity : FragmentActivity() {

    private lateinit var mTool: BluetoothTool
    private var mDevice: BluetoothDevice? = null
    private var mDevice2: BluetoothDevice? = null
    private var mFlag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_test)

        mTool = Builder().activity(this).hasLog(BuildConfig.DEBUG)
            .callback(
                object : Callback() {
                    override fun deviceBack(
                        device: BluetoothDevice,
                        type: DeviceType,
                        secondType: Int
                    ) {
                        super.deviceBack(device, type, secondType)
                        if (mFlag == 0 && device.address == "BA:03:61:3C:5E:71") {
                            mDevice = device
                            mTool.stopSearch()
                            mTool.connectDevice(device)
                            mTool.sendData(device.address, TestData.getCommonI(TestData.CMD_OPEN))
                        } else if (mFlag == 1 && device.address == "A4:C1:38:D5:12:9C") {
                            mDevice2 = device
                            if (mDevice != null) {
                                mTool.stopSearch()
                            }
                            mTool.connectDevice(mDevice2!!)
                            mTool.sendData(device.address, TestData.getCommonI(TestData.CMD_OPEN))
                        }
                    }

                    override fun dataBack(device: BluetoothDevice, data: ByteArray) {
                        super.dataBack(device, data)
                        if (data.size > 3 && data[3] == TestData.CMD_OPEN) {
                            runOnUiThread {
                                Toast.makeText(this@TestActivity, "开门成功", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }).create()

        findViewById<Button>(R.id.bt_open)
            .setOnClickListener {
                mFlag = 0
                openDevice1()
            }

        findViewById<Button>(R.id.bt_open2)
            .setOnClickListener {
                mFlag = 1
                openDevice2()
            }
    }

    private fun openDevice1() {
        if (mDevice == null) {
            mTool.searchDevices()
        } else {
            mTool.connectDevice(mDevice!!)
            mTool.sendData(mDevice!!.address, TestData.getCommonI(TestData.CMD_OPEN))
        }
    }

    private fun openDevice2() {
        if (mDevice2 == null) {
            mTool.searchDevices()
        } else {
            mTool.connectDevice(mDevice2!!)
            mTool.sendData(mDevice2!!.address, TestData.getCommonI(TestData.CMD_OPEN))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BluetoothTool.OPEN_BLUETOOTH_CODE && resultCode == Activity.RESULT_OK) {
            //蓝牙开启成功
            mTool.checkPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var result = true
        for (code in grantResults) {
            if (code == -1) {
                result = false
            }
        }
        if (result) {
            if (mFlag == 0) {
                openDevice1()
            } else if (mFlag == 2) {
                openDevice2()
            }
        } else {
            //TODO 提示权限获取失败
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTool.onDestroy()
    }

}