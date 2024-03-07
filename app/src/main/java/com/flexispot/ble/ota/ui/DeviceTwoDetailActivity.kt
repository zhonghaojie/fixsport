package com.flexispot.ble.ota.ui

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clj.fastble.data.BleDevice

import com.koylo.ble.core.BluetoothTool
import com.koylo.ble.core.Builder
import com.koylo.ble.core.Callback
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.BuildConfig
import com.flexispot.ble.R
import com.flexispot.ble.data.MediaData
import com.flexispot.ble.gui.device.media.MediaActivity
import com.flexispot.ble.gui.device.media.MediaTwoActivity
import com.flexispot.ble.gui.device.media.MediaTwoViewModel
import com.flexispot.ble.gui.device.media.MediaViewModel
import com.flexispot.ble.gui.device.rack.RackActivity
import com.flexispot.ble.gui.device.rack.RackTwoActivity
import com.flexispot.ble.ota.ble.AdvDevice
import com.flexispot.ble.ota.ble.Command
import com.flexispot.ble.ota.ble.Device
import com.flexispot.ble.ota.ui.file.FileSelectActivity
import com.flexispot.ble.ota.util.TelinkLog
import com.luman.core.LumanHelper
import okhttp3.internal.and

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

/**
 * 设备详情
 * connect --> discoverService --> display
 * Created by Administrator on 2017/2/20.
 */
class DeviceTwoDetailActivity : BaseActivity(), View.OnClickListener {
    var device: Device? = null
        private set
    private var mConnectState = BluetoothGatt.STATE_DISCONNECTED

    private var selectFile: TextView? = null
    private var info: TextView? = null
    private var progress: TextView? = null
    private var startOta: Button? = null
    private var menu_connect_state: TextView? = null
    private var Open: TextView? = null
    private var iv_back: ImageView? = null
    private var mPath: String? = null
    private var activity: Int? = 0;

    private val mInfoHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_PROGRESS) {
                progress!!.text = msg.obj.toString() + "%"
            } else if (msg.what == MSG_INFO) {
                info!!.append("\n" + msg.obj)
                if (msg.obj == "ota complete") {
                    Toast.makeText(
                        this@DeviceTwoDetailActivity,
                        msg.obj.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    showFinishDialog("OTA完成，是否返回主页")
                }else{
                    showFinishDialog("OTA失败，是否返回主页")
                }
            }
        }
    }



    internal var address = ""




    internal var gattOperationCallback: Device.GattOperationCallback =
        object : Device.GattOperationCallback {
            override fun onRead(command: Command, obj: Any) {

            }

            override fun onWrite(command: Command, obj: Any) {

            }

            override fun onNotify(
                data: ByteArray,
                serviceUUID: UUID,
                characteristicUUID: UUID,
                tag: Any
            ) {

            }

            override fun onEnableNotify() {

            }

            override fun onDisableNotify() {

            }
        }

    private var mycharacteristic: BluetoothGattCharacteristic? = null

    private val handler = Handler()
    private val deviceCallback = object : Device.DeviceStateCallback {
        override fun onConnected(device: Device) {
            TelinkLog.w("$TAG # onConnected")
            mConnectState = BluetoothGatt.STATE_CONNECTED
            runOnUiThread {
                showDiscoveringDialog()
                CleanItem()
            }
            /* handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDevice.disconnect();
                }
            }, 5000);*/

        }

        override fun onDisconnected(device: Device) {
            TelinkLog.w("$TAG # onDisconnected")
            mConnectState = BluetoothGatt.STATE_DISCONNECTED
            runOnUiThread {
                //                    mServiceListFragment.clearListData();
                toastMsg("device disconnected")
                CleanItem()
                dismissWaitingDialog()
            }


        }

        override fun onServicesDiscovered(device: Device, services: List<BluetoothGattService>) {
            TelinkLog.w("$TAG # onServicesDiscovered")
            var serviceUUID: UUID? = null
            for (service in services) {
                for (characteristic in service.characteristics) {
                    if (characteristic.uuid == Device.CHARACTERISTIC_UUID_WRITE) {
                        serviceUUID = service.uuid
                        mycharacteristic = characteristic
                        break
                    }
                }
            }

            if (serviceUUID != null) {
                device.SERVICE_UUID = serviceUUID
            }
            runOnUiThread {
                //                    mServiceListFragment.clearListData();
                //                    toastMsg("device onServicesDiscovered");
                dismissWaitingDialog()

                //                    invalidateOptionsMenu();
            }
        }

        override fun onOtaStateChanged(device: Device, state: Int) {
            TelinkLog.w("$TAG # onOtaStateChanged")
            when (state) {
                Device.STATE_PROGRESS -> {
                    TelinkLog.d("ota progress : " + device.otaProgress)
                    mInfoHandler.obtainMessage(MSG_PROGRESS, device.otaProgress).sendToTarget()
                }
                Device.STATE_SUCCESS -> {
                    TelinkLog.d("ota success : ")
                    mInfoHandler.obtainMessage(MSG_INFO, "ota complete").sendToTarget()
                }
                Device.STATE_FAILURE -> {
                    TelinkLog.d("ota failure : ")
                    mInfoHandler.obtainMessage(MSG_INFO, "ota failure").sendToTarget()
                }
            }
        }
    }

    private fun showFinishDialog(s:String ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("提示").setMessage(s)
            .setPositiveButton("确定") { dialog, which ->
                dialog.dismiss()
                setResult(Activity.RESULT_OK)
                finish()
            }
        builder.show()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_detail)
        val intent = intent
        val advDevice: BleDevice?
        if (intent.hasExtra("device")) {
            advDevice = intent.getParcelableExtra("device")
        } else {
            toastMsg("device null !")
            finish()
            return
        }
        activity =  intent.getIntExtra("activity",0)
        initViews()

        device = Device(advDevice!!.device, advDevice.scanRecord, advDevice.rssi)

        device!!.setDeviceStateCallback(deviceCallback)
        device!!.setGattOperationCallback(gattOperationCallback)
        val actionBar = actionBar
        if (actionBar != null) {
            if (advDevice.device != null) {
                actionBar.title = if (advDevice.device.name == null || advDevice.device.name == "")
                    "Unknown device"
                else
                    advDevice.device.name
            }
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }
        address = advDevice.device.address
        iv_back!!.setOnClickListener { finish() }
        connectToggle()
        CleanItem()
        getData()
        isStoragePermissionGranted()
    }


fun getData(){

    if (activity == 1) {
        val ac =
            LumanHelper.aboutActivityManager().findAc(RackTwoActivity::class.java.name)
        ViewModelProviders.of(ac!!).get(MediaTwoViewModel::class.java).getInstance().sendData(MediaData.GetVersion())
        ViewModelProviders.of(ac!!).get(MediaTwoViewModel::class.java).getInstance()
            .openreset.observe(this, Observer {
            if (it){
                Toast.makeText(
                    this@DeviceTwoDetailActivity,
                    "打开OTA成功",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        ViewModelProviders.of(ac!!).get(MediaTwoViewModel::class.java).getInstance()
            .version.observe(this, Observer {
            info!!.text=it
        })
    } else if (activity == 2) {
        val ac =
            LumanHelper.aboutActivityManager().findAc(MediaTwoActivity::class.java.name)
        ViewModelProviders.of(ac!!).get(MediaTwoViewModel::class.java).getInstance().sendData(MediaData.GetVersion())
        ViewModelProviders.of(ac!!).get(MediaTwoViewModel::class.java).getInstance()
            .openreset.observe(this, Observer {
            if (it){
                Toast.makeText(
                    this@DeviceTwoDetailActivity,
                    "打开OTA成功",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        ViewModelProviders.of(ac!!).get(MediaTwoViewModel::class.java).getInstance()
            .version.observe(this, Observer {
            info!!.text=it
        })

    }

}


    //打开开关
    fun getDeviceInfo() {
        if (activity == 1) {
            val ac =
                LumanHelper.aboutActivityManager().findAc(RackTwoActivity::class.java.name)
            ViewModelProviders.of(ac!!).get(MediaTwoViewModel::class.java).getInstance()
                .sendData(MediaData.OpenOTA())
        } else if (activity == 2) {
            val ac =
                LumanHelper.aboutActivityManager().findAc(MediaTwoActivity::class.java.name)
            ViewModelProviders.of(ac!!).get(MediaTwoViewModel::class.java)
                .getInstance().sendData(MediaData.OpenOTA())
        }

//
    }


    private fun initViews() {
        selectFile = findViewById<View>(R.id.selectFile) as TextView
        info = findViewById<View>(R.id.info) as TextView
        progress = findViewById<View>(R.id.progress) as TextView
        startOta = findViewById<View>(R.id.startOta) as Button
        menu_connect_state = findViewById(R.id.menu_connect_state)
        iv_back = findViewById(R.id.iv_back)
        Open = findViewById(R.id.open)
        selectFile!!.setOnClickListener(this)
        startOta!!.setOnClickListener(this)
        menu_connect_state!!.setOnClickListener(this)
        Open!!.setOnClickListener(this)
    }

    private fun connectToggle() {
        TelinkLog.w("$TAG # startConnect")
        if (mConnectState == BluetoothGatt.STATE_CONNECTED) {
            device!!.disconnect()

        } else if (mConnectState == BluetoothGatt.STATE_DISCONNECTED) {
            device!!.connect(this)
            mConnectState = BluetoothGatt.STATE_CONNECTING
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (progress!!.getText() != ""){
            setResult(Activity.RESULT_OK)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (device != null) {
            device!!.setDeviceStateCallback(null)
            if (this.mConnectState == BluetoothGatt.STATE_CONNECTED) {
                device!!.disconnect()
            }
        }




    }

    fun showDiscoveringDialog() {
        showWaitingDialog("Discovering services...")
    }


    private fun CleanItem() {
        if (mConnectState == BluetoothGatt.STATE_CONNECTED) {
            menu_connect_state!!.setText(R.string.state_connected)
        } else if (mConnectState == BluetoothGatt.STATE_DISCONNECTED) {
            menu_connect_state!!.setText(R.string.state_disconnected)
        } else if (mConnectState == BluetoothGatt.STATE_CONNECTING) {
            menu_connect_state!!.setText(R.string.state_connecting)
        }


    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.startOta -> {
                if (mConnectState != BluetoothGatt.STATE_CONNECTED) {
                    Toast.makeText(this, "device disconnected!", Toast.LENGTH_SHORT).show()
                    return
                }
                if (this.mPath == null || this.mPath == "") {
                    Toast.makeText(this, "select firmware!", Toast.LENGTH_SHORT).show()
                    return
                }

                val firmware = readFirmware(this.mPath!!)
                if (firmware == null) {
                    toastMsg("firmware null")
                    return
                }
                info!!.text = "start OTA"
                device!!.startOta(firmware)
            }

            R.id.selectFile ->
                /*if (mConnectState != BluetoothGatt.STATE_CONNECTED){
                    Toast.makeText(this, "device disconnected!", Toast.LENGTH_SHORT).show();
                    return;
                }*/
                startActivityForResult(
                    Intent(this, FileSelectActivity::class.java),
                    REQUEST_CODE_GET_FILE
                )
            R.id.menu_connect_state -> connectToggle()
            R.id.open -> getDeviceInfo()
        }
    }

    private fun readFirmware(fileName: String): ByteArray? {
        try {
            val stream = FileInputStream(fileName)
            val length = stream.available()
            val firmware = ByteArray(length)
            stream.read(firmware)
            stream.close()
            return firmware
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || requestCode != REQUEST_CODE_GET_FILE)
            return

        this.mPath = data!!.getStringExtra("path")
        TelinkLog.d(mPath)
        val f = File(mPath!!)
        selectFile!!.text = f.toString()
    }

    companion object {
        private val REQUEST_CODE_GET_FILE = 1
        private val MSG_PROGRESS = 11
        private val MSG_INFO = 12
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

    fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {

            val readPermissionCheck = ContextCompat.checkSelfPermission(
                this@DeviceTwoDetailActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writePermissionCheck = ContextCompat.checkSelfPermission(
                this@DeviceTwoDetailActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (readPermissionCheck == PackageManager.PERMISSION_GRANTED && writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
                Log.v("juno", "Permission is granted")
                return true
            } else {
                Log.v("juno", "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this@DeviceTwoDetailActivity,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    1
                )
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true
        }
    }


}
