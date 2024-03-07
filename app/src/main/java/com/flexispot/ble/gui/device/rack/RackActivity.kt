package com.flexispot.ble.gui.device.rack

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
import com.flexispot.ble.gui.view.MoreLongRackHandleImageView
import com.flexispot.ble.ota.ble.AdvDevice
import com.flexispot.ble.ota.ui.DeviceDetailActivity
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

/**
 * @author luman
 * @date 19-11-26
 * 电视升降架
 **/
class RackActivity : LuManActivity<FraDeviceRackBinding, MediaViewModel>() {

    private val handler = Handler()

    private var progressDialog: ProgressDialog? = null
    private var stopAnimRunnable: Runnable? = null
    private var dismissRunnable: Runnable? = null
    private var mSureResetDialog: ResetDialog? = null
    private var opeLaterDialog : OpeLaterDialog? = null
    private var name:String?=""
    private var mac:String?=""
    override fun layoutId() = R.layout.fra_device_rack
    override fun vmFactory() = MediaViewModelFactory()
    override fun barColor() = R.color.detailTop
    override fun ifDark() = false
    var cdt = MyCountDownTimer(10000,1000)
    override fun viewOpe() {
        super.viewOpe()

        //判断app状态
        if (AppStatusManager.getInstance().getAppStatus() == AppStatus.STATUS_RECYCLE){
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
        viewModel.init(intent.extras!!["params"] as Device)
        name=(intent.extras!!["params"] as Device).name
        mac=(intent.extras!!["params"] as Device).mac
        viewModel.connectDevice(this)

//        viewModel.connectDevice(this)
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
                            AnimationUtils.loadAnimation(this@RackActivity, R.anim.progress_rotate)
                        animation.interpolator = LinearInterpolator()
                        tv_restore.visibility = View.GONE
                        iv_loading.visibility = View.VISIBLE
                        iv_loading.startAnimation(animation)
                        stopAnimRunnable = Runnable {
                            iv_loading.clearAnimation()
                            tv_restore.visibility = View.VISIBLE
                            iv_loading.visibility = View.GONE
                            viewModel.showToast(getString(R.string.reset_failed))
                        }
                        handler.postDelayed(stopAnimRunnable!!, 10000)

                        if (cdt==null){
                            cdt = MyCountDownTimer(10000,1000)
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
        //测试，打开ota或者获取设备信息
//        tv_title.setOnLongClickListener(View.OnLongClickListener {
////            viewModel.sendData(MediaData.OpenOTA ())
//            viewModel.cleanLianJie(mac!!)
//            return@OnLongClickListener false
//        })

        tv_up.setHandleButtonOnClickListener(handleViewListener)
        tv_down.setHandleButtonOnClickListener(handleViewListener)
//        get_new.setOnClickListener(View.OnClickListener {
//            val intent: Intent  = Intent(this, DeviceDetailActivity::class.java)
//            intent.putExtra("device",viewModel.get(mac!!,1))
//            intent.putExtras(intent)
//            startActivityForResult(intent,10001)
//        })

    }
    public inner class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {

        }
        override fun onTick(millisUntilFinished: Long) {
            viewModel.sendData(MediaData.reset())
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10001 && resultCode == Activity.RESULT_OK) {
            finish()
        }else{

        }
    }

    protected override fun onResume() {
//        viewModel.cleanLianJie(mac!!)
//
        super.onResume()
    }

    //上下两键处理器
    private val handleViewListener = object : MoreLongRackHandleImageView.HandleButtonOnClickListener {
        override fun onShortPress(view: View) {
            when (view.tag) {
                "1" -> {
                    viewModel.sendData(MediaData.clickUp())
                    if (viewModel.resetJob == null) {
                        viewModel.sendData(MediaData.stop())
                    }
                }
                "2" -> {
                    viewModel.sendData(MediaData.clickDown())
                    if (viewModel.resetJob == null) {
                        viewModel.sendData(MediaData.stop())
                    }
                }
            }
        }

        override fun onLongPress(view: View) {
            if (viewModel.heightForShow.value == "RST" && view.tag == "2") {
//                viewModel.reset = false
                viewModel.reset()
            } else {
                when (view.tag) {
                    "1" -> {
//                        viewModel.sendData(MediaData.longClickUp())
                        viewModel. resetUP()                     }
                    "2" -> {
//                        viewModel.sendData(MediaData.longClickDown())
                        viewModel.reset()
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
            Logger.d("松开手指")
            if (viewModel.resetJob == null) {
                viewModel.sendData(MediaData.stop())
            }
        }

        override fun onDown(view: View) {
            onToMaxCheck { }
        }

        override fun onUp(view: View) {
            if (viewModel.resetJob != null) {
//                viewModel.reset = true
                viewModel.cancelReset()
            }
        }
    }

    private var reset:Boolean?=false
    override fun observeVM() {
        super.observeVM()
        viewModel.connectState.observe(this, Observer {
            dismissProgressDialog()
            if (!it){
                if (connectNum<2){
                    connectNum++
                    viewModel.connectDevice(this)
//                    showProgressDialog()
                }else {
                    showDisconnectWindow()
                }
            }else{
                connectNum=0;
            }
        })

        viewModel.unit.observe(this, Observer {
            tv_unit.text = if (it == 0) "mm" else "inch"
        })

//        viewModel.version.observe(this, Observer {
//            preferencesUtility!!.setVersion(it,name)
//        })

        viewModel.reset.observe(this, Observer {
            reset=it
            if (it){
                cdt.cancel()
            }
        })


        viewModel.heightForShow.observe(this, Observer {
            if (it.startsWith("E") || it.startsWith("RST")) {
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
                when{

                    (it == "E01") -> {
                        //运行时间过长
                        showOpeTooLongDialog(R.string.ope_later)
                    }
                    (it == "E02") -> {
                        //过温
                        showOpeTooLongDialog(R.string.ope_later_temp)
                    }
                    (it == "E03" || it == "E04") -> {
                        //过流
                        LumanHelper.aboutToast().showLongToast(R.string.auto_fixed)
                    }
                    else -> {
                        //复位去
                        gp_normal.visibility = View.INVISIBLE
                        gp_reset.visibility = View.VISIBLE
                        iv_label.visibility= View.INVISIBLE
                        tv_label.visibility= View.INVISIBLE

                    }

                }
            } else {
                if (tv_restore.visibility == View.GONE && stopAnimRunnable == null) {
//                    viewModel.reset = false
                    tv_restore.visibility = View.VISIBLE
                }
                if(gp_normal.visibility == View.INVISIBLE){
                    gp_normal.visibility = View.VISIBLE
                    gp_reset.visibility = View.INVISIBLE
                    iv_label.visibility= View.VISIBLE
                    tv_label.visibility= View.VISIBLE
                }
                tv_height.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60f)
                tv_unit.visibility = View.VISIBLE
                tv_height.text = it
            }
        })
    }

    /**
     * 信息发送前的预处理，先发清空指令
     */
    private fun onToMaxCheck(block: () -> Unit) {
        viewModel.sendData(MediaData.stop())
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
//                    viewModel.showToast(getString(R.string.connect_time_out))

                        viewModel.destroy()
                        showDisconnectWindow()
//               finish()
                }
            }
            handler.postDelayed(dismissRunnable, 8000)
        }
    }
    var connectNum=0;

    private fun showDisconnectWindow(){
        val dialog = DisconnectedDialog()
        dialog.setCallback(object : DisconnectedDialog.Callback{
            override fun sure() {
                finish()
            }
        })
//                viewModel.showToast(getString(R.string.dis_connect_device))
//                finish()
        dialog.show(supportFragmentManager, "disconnect")
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

    /**
     * 显示运行时间过长弹窗
     */
    private fun showOpeTooLongDialog(source : Int){
        if(opeLaterDialog == null) {
            opeLaterDialog = OpeLaterDialog(source)
            opeLaterDialog?.setCallback(object : OpeLaterDialog.Callback {
                override fun sure() {
                    finish()
                }
            })
            opeLaterDialog?.show(supportFragmentManager, "ope too long")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
        dismissProgressDialog()
        if (stopAnimRunnable != null) {
            handler.removeCallbacks(stopAnimRunnable)
            stopAnimRunnable = null
        }
        if (dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable)
            dismissRunnable = null
        }
    }
}