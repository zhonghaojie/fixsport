package com.flexispot.ble.gui.device.rack

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.lifecycle.Observer
import com.flexispot.ble.R
import com.flexispot.ble.data.MediaData
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.device.media.MediaViewModelFactory
import com.flexispot.ble.databinding.FraDeviceRackBinding
import com.flexispot.ble.gui.WelcomeActivity
import com.flexispot.ble.gui.dialog.DisconnectedDialog
import com.flexispot.ble.gui.dialog.ResetDialog
import com.flexispot.ble.gui.device.media.MediaViewModel
import com.flexispot.ble.gui.dialog.OpeLaterDialog
import com.flexispot.ble.gui.outkill.AppStatus
import com.flexispot.ble.gui.outkill.AppStatusManager

import com.flexispot.ble.gui.view.MoreLongHandleImageView
import com.luman.core.LumanHelper
import com.luman.mvvm.base.LuManActivity
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fra_device_media.*
import kotlinx.android.synthetic.main.fra_device_rack.*
import kotlinx.android.synthetic.main.fra_device_rack.gp_normal
import kotlinx.android.synthetic.main.fra_device_rack.gp_reset
import kotlinx.android.synthetic.main.fra_device_rack.iv_back
import kotlinx.android.synthetic.main.fra_device_rack.iv_loading
import kotlinx.android.synthetic.main.fra_device_rack.tv_height
import kotlinx.android.synthetic.main.fra_device_rack.tv_restore
import kotlinx.android.synthetic.main.fra_device_rack.tv_title
import kotlinx.android.synthetic.main.fra_device_rack.tv_unit
import java.io.Serializable
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.name
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.Message
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.clj.fastble.data.BleDevice
import com.koylo.ble.core.BluetoothTool
import com.flexispot.ble.gui.device.media.MediaTwoViewModel
import com.flexispot.ble.gui.view.AlertDialog
import com.flexispot.ble.gui.view.MoreLongRackHandleImageView
import com.flexispot.ble.gui.view.NewBluetooth
import com.flexispot.ble.ota.ui.DeviceDetailActivity
import com.flexispot.ble.ota.ui.DeviceTwoDetailActivity
import java.util.ArrayList


/**
 * @author luman
 * @date 19-11-26
 * 电视升降架
 **/
class RackTwoActivity : LuManActivity<FraDeviceRackBinding, MediaTwoViewModel>() {

    private val handler = Handler()

    private var progressDialog: ProgressDialog? = null
    private var stopAnimRunnable: Runnable? = null
    private var dismissRunnable: Runnable? = null
    private var mSureResetDialog: ResetDialog? = null
    private var opeLaterDialog: OpeLaterDialog? = null
    private var name: String? = ""
    private var mac: String? = ""
    override fun layoutId() = R.layout.fra_device_rack
    override fun vmFactory() = MediaViewModelFactory()
    override fun barColor() = R.color.detailTop
    override fun ifDark() = false
    var cdt = MyCountDownTimer(10000, 200)
    override fun viewOpe() {
        super.viewOpe()

        //判断app状态
        if (AppStatusManager.getInstance().getAppStatus() == AppStatus.STATUS_RECYCLE) {
            //被回收，跳转到启动页面
            var intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }


        if (intent.extras == null || intent.extras!!["params"] == null) {
            finish()
        }
        showProgressDialog()
        name = (intent.extras!!["params"] as Device).name
        mac = (intent.extras!!["params"] as Device).mac
        if (!isLocationEnable(this)) {
            setLocationService()
        }
        checkPermission()
        val b = NewBluetooth()
        if (b.isBluetoothSupported() && !b.isBluetoothEnabled()) {
            setBlueOpen()
        } else {
            viewModel.getInstance().startScan();
        }

        viewModel.getInstance().setOnOperationListener(bleListener)
        iv_back.setOnClickListener {
            finish()
        }
        tv_title.setText(R.string.rack)

        //重置
        tv_restore.setOnClickListener {
            if (mSureResetDialog == null) {
                mSureResetDialog = ResetDialog()
                mSureResetDialog!!.setCallback(object :
                    ResetDialog.Callback {
                    override fun sure() {
                        val animation =
                            AnimationUtils.loadAnimation(
                                this@RackTwoActivity,
                                R.anim.progress_rotate
                            )
                        animation.interpolator = LinearInterpolator()
                        tv_restore.visibility = View.GONE
                        iv_loading.visibility = View.VISIBLE
                        iv_loading.startAnimation(animation)
                        stopAnimRunnable = Runnable {
                            iv_loading.clearAnimation()
                            tv_restore.visibility = View.VISIBLE
                            iv_loading.visibility = View.GONE
                            viewModel.getInstance().showToast(getString(R.string.reset_failed))
                        }
                        handler.postDelayed(stopAnimRunnable!!, 10000)

                        if (cdt == null) {
                            cdt = MyCountDownTimer(10000, 200)
                        }
                        cdt.start()
                    }

                    override fun cancel() {
                        mSureResetDialog?.dismiss()
                    }
                })
            }
            mSureResetDialog?.show(supportFragmentManager, "reset")

        }

        tv_title.setOnClickListener(View.OnClickListener {
            val intent: Intent  = Intent(this, DeviceTwoDetailActivity::class.java)
            intent.putExtra("device",scanNowResult)
            intent.putExtra("activity",1)
            intent.putExtras(intent)
            startActivityForResult(intent,10001)
        })

        tv_up.setHandleButtonOnClickListener(handleViewListener)
        tv_down.setHandleButtonOnClickListener(handleViewListener)


    }

    public inner class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {

        }

        override fun onTick(millisUntilFinished: Long) {
            viewModel.getInstance().sendData(MediaData.reset())
        }
    }

    private val REQUEST_CODE_LOCATION_SETTINGS = 2
    private val REQUEST_CODE_BULETOOTH_SETTINGS = 4

    private fun setLocationService() {
        val locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS)
    }

    private fun checkPermission() {
        //请求蓝牙权限与定位权限
        val perms = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(this, perms, 5)

    }

    private fun setBlueOpen() {
        val BlIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        this.startActivityForResult(BlIntent, REQUEST_CODE_BULETOOTH_SETTINGS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10001 && resultCode == Activity.RESULT_OK) {
//            finish()
        } else if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            if (isLocationEnable(this)) {
                //定位已打开的处理
                viewModel.getInstance().startScan()
            } else {
                //定位依然没有打开的处理
                //                showToast(getString(R.string.location_permissions_not_authorized))
            }
        } else if (requestCode == REQUEST_CODE_BULETOOTH_SETTINGS) {
            viewModel.getInstance().startScan();

        }
    }

    private var downSet: Boolean = false
    private var upSet: Boolean = false


    //上下两键处理器
    private val handleViewListener =
        object : MoreLongRackHandleImageView.HandleButtonOnClickListener {
            override fun onShortPress(view: View) {
                when (view.tag) {
                    "1" -> {
                        viewModel.getInstance().sendData(MediaData.clickUp())
                    }
                    "2" -> {
                        viewModel.getInstance().sendData(MediaData.clickDown())

                    }
                }
            }

            override fun onLongPress(view: View) {
                if (NewHeight == "RST" && view.tag == "2") {
//                viewModel.reset = false
                    viewModel.getInstance().reset()
                    downSet = true
                } else {
                    if (viewModel.getInstance().stopJob != null) {
                        viewModel.getInstance().cancelStop()
                    }
                    when (view.tag) {
                        "1" -> {
                            upSet = true
                            if (downSet) {
                                viewModel.getInstance().cancelReset()
                                viewModel.getInstance().stopP()
                            } else {
                                viewModel.getInstance().resetUP()
                            }
                        }
                        "2" -> {
                            downSet = true
                            if (upSet) {
                                viewModel.getInstance().cancelReset()
                                viewModel.getInstance().stopP()

                            } else {
                                viewModel.getInstance().reset()
                            }

                        }
                    }
                }
            }

            override fun toMax(view: View) {
//            when (view.tag) {
//                "1" -> {
//                    viewModel.sendData(MediaData.longClickUp())
//                }
//                "2" -> {
//                    viewModel.sendData(MediaData.longClickDown())
//                }
//            }

            }

            override fun onUpspring(view: View) {
////            Logger.d("松开手指")
//            if (viewModel.resetJob == null) {
//                viewModel.getInstance().sendData(MediaData.stop())
//            }
            }

            override fun onDown(view: View) {
//            onToMaxCheck { }
            }

            override fun onUp(view: View) {
                when (view.tag) {
                    "1" -> {
                        upSet = false
                    }
                    "2" -> {
                        downSet = false
                    }
                }
                if (upSet || downSet) {
                    if (upSet) {
                        viewModel.getInstance().resetUP()
                    } else {
                        viewModel.getInstance().reset()

                    }
                } else {
                    viewModel.getInstance().stopP()
                    if (viewModel.getInstance().resetJob != null) {
//                viewModel.reset = true
                        viewModel.getInstance().cancelReset()
                    }

                }

            }
        }


    private fun isLocationEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return networkProvider || gpsProvider
    }


    override fun observeVM() {
        super.observeVM()


    }

    /**
     * 信息发送前的预处理，先发清空指令
     */
    private fun onToMaxCheck(block: () -> Unit) {
        viewModel.getInstance().sendData(MediaData.stop())
        block()
    }


    /**
     * 加载连接加载框
     */
    private fun showProgressDialog() {
        progressDialog = ProgressDialog(this)
        if (progressDialog != null) {
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel)
            ) { dialog, which ->
                finish()
            }
            progressDialog!!.setCancelable(false)//点击屏幕和按返回键都不能取消加载框
            progressDialog!!.setMessage(getText(R.string.connect_device))    //设置内容
            progressDialog!!.show()
            //设置超时自动消失
            dismissRunnable = Runnable {
                //取消加载框
                if (dismissProgressDialog()!!) {
                    //超时处理
                    showdialog()
                    //关闭蓝牙连接
                    viewModel.getInstance().disconnect()
                }
            }
            handler.postDelayed(dismissRunnable, 8000)
        }
    }


    var tanchukaung: AlertDialog? = null
    private fun showdialog() {
        if (tanchukaung == null) {
            tanchukaung = AlertDialog(
                this,
                R.style.AlertDialogStyle,
                "",
                AlertDialog.OnCloseListener { dialog, confirm ->
                    finish()
                })

        }
        tanchukaung!!.setCancelable(false)
        tanchukaung!!.show()
    }


    /**
     * 取消连接加载框
     */
    private fun dismissProgressDialog(): Boolean? {
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
                return true//取消成功
            }
        }
        return false//已经取消过了，不需要取消
    }


    override fun onDestroy() {
        super.onDestroy()
        dismissProgressDialog()
        if (stopAnimRunnable != null) {
            handler.removeCallbacks(stopAnimRunnable)
            stopAnimRunnable = null
        }
        if (dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable)
            dismissRunnable = null
        }
        //关闭蓝牙连接
        viewModel.getInstance().disconnect()
        if (tanchukaung != null) {
            tanchukaung!!.dismiss()
            tanchukaung = null
        }
    }

    private var NewHeight = ""
    private var scanNowResult: BleDevice? = null
    private val bleListener = object : MediaTwoViewModel.OnOperationListener {
        override fun ReplyOpen() {

        }

        override fun ReplyReset() {

            var message = Message()
            message.what = 3;
            handler1.sendMessage(message);
        }

        override fun onScanFailure() {


        }

        override fun onStartConnect() {
        }

        override fun onConnectSuccess() {
            dismissProgressDialog()
            if (tanchukaung != null) {
                tanchukaung!!.dismiss()
            }
        }

        override fun onConnectFail() {
            var message = Message()
            message.what = 2;
            handler1.sendMessage(message);
        }

        override fun onGetDeviceInfo(maxheight: Int, minheight: Int, unit: Int) {

        }

        override fun onGetScanDevice(scanResult: BleDevice) {
            if (scanResult != null) {
                if (TextUtils.equals(name, scanResult.name)) {
//                                        if (TextUtils.equals(intent.getStringExtra("address"),scanResult.mac)){
//                    isConnecting = true
                    scanNowResult = scanResult
                    viewModel.getInstance().startConnect(scanResult.mac)
                    viewModel.getInstance().stopScan()
                }
            }
        }


        override fun height(height: String) {
            NewHeight = height
            var message = Message()
            message.what = 1;
            handler1.sendMessage(message);
        }

    }
    private val handler1 = object : Handler() {

        override fun handleMessage(msg: Message) {
//            Log.e("aaa", "{${msg.what}}")
            when (msg.what) {
                1 -> {
                    if (NewHeight.startsWith("E") || NewHeight.startsWith("RST")) {
//                viewModel.reset = true
                        if (stopAnimRunnable != null) {
                            handler.removeCallbacks(stopAnimRunnable)
                            stopAnimRunnable = null
                            iv_loading.clearAnimation()
                            iv_loading.visibility = View.GONE
                        }
//                tv_unit.visibility = View.GONE
//                tv_height.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
//                tv_height.text = "$it : ${getString(viewModel.errorMsgId)}"
                        tv_restore.visibility = View.GONE
                        tv_error_rack.text = NewHeight

                        if (NewHeight.startsWith("E01") || NewHeight.startsWith("E02")) {
                            tv_down.visibility = View.GONE
                            tv_up.visibility = View.GONE
                        }

                        when {
                            (NewHeight == "E01") -> {
                                //运行时间过长
//                          showOpeTooLongDialog(R.string.ope_later)
                                gp_normal.visibility = View.INVISIBLE
                                gp_reset.visibility = View.VISIBLE
                                iv_label.visibility = View.INVISIBLE
                                tv_label.visibility = View.INVISIBLE
                                tv_to_lowest_rack.text = getString(R.string.ope_later)
                            }
                            (NewHeight == "E02") -> {
                                //过温
//                        showOpeTooLongDialog(R.string.ope_later_temp)
                                gp_normal.visibility = View.INVISIBLE
                                gp_reset.visibility = View.VISIBLE
                                iv_label.visibility = View.INVISIBLE
                                tv_label.visibility = View.INVISIBLE
                                tv_to_lowest_rack.text = getString(R.string.ope_later_temp)

                            }
//                            (NewHeight == "E03" || NewHeight == "E04") -> {
//                                //过流
////                                LumanHelper.aboutToast().showLongToast(R.string.auto_fixed)
//
//
//
//                            }
                            else -> {
                                //复位去
                                gp_normal.visibility = View.INVISIBLE
                                gp_reset.visibility = View.VISIBLE
                                iv_label.visibility = View.INVISIBLE
                                tv_label.visibility = View.INVISIBLE
                                tv_to_lowest_rack.text = getString(R.string.to_lowest)

                            }
                        }

                    } else {
                        if (tv_restore.visibility == View.GONE && stopAnimRunnable == null) {
//                    viewModel.reset = false
                            tv_restore.visibility = View.VISIBLE
                        }
                        if (gp_normal.visibility == View.INVISIBLE) {
                            gp_normal.visibility = View.VISIBLE
                            gp_reset.visibility = View.INVISIBLE
                            iv_label.visibility = View.VISIBLE
                            tv_label.visibility = View.VISIBLE
                        }
                        if (tv_down.visibility === View.GONE) {
                            tv_down.visibility = View.VISIBLE
                            tv_up.visibility = View.VISIBLE
                        }

                        tv_height.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60f)
                        tv_height.text = NewHeight
                    }

                }
                2 -> {
                    dismissProgressDialog()
                    showdialog()
                }
                3 -> {
                    cdt.cancel()
                }
                else -> {

                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        viewModel.getInstance().cancelReset()

    }
}