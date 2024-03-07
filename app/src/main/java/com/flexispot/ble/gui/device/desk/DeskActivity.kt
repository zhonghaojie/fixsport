package com.flexispot.ble.gui.device.desk

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.flexispot.ble.R
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.device.rack.RackViewModelFactory
import com.flexispot.ble.databinding.FraDeviceDeskBinding
import com.flexispot.ble.gui.dialog.DisconnectedDialog
import com.flexispot.ble.gui.device.rack.RackViewModel
import com.flexispot.ble.gui.view.MoreLongHandleImageView
import com.luman.mvvm.base.LuManActivity
import kotlinx.android.synthetic.main.fra_device_desk.*
import kotlinx.android.synthetic.main.topbar.*

/**
 * @author luman
 * @date 19-11-26
 * 升降桌
 **/
class DeskActivity : LuManActivity<FraDeviceDeskBinding, RackViewModel>() {

    private var progressDialog: ProgressDialog? = null

    override fun layoutId() = R.layout.fra_device_desk
    override fun vmFactory() = RackViewModelFactory()
    override fun barColor() = R.color.detailTop
    override fun ifDark() = false



    override fun viewOpe() {
        super.viewOpe()
        if (intent.extras == null || intent.extras!!["params"] == null) {
            finish()
        }
        showProgressDialog()
        viewModel.deviceInit(intent.extras!!["params"] as Device)
        viewModel.connect(this)

        tv_title.setText(R.string.desk)
        iv_back.setOnClickListener {
            finish()
        }

        //站姿和坐姿
        tv_stand.setOnClickListener {
            if (viewModel.memoryHeightForStand == -1) {
                viewModel.showToast(getString(R.string.tips_setting_stand))
            } else {
                viewModel.controlHight(viewModel.memoryHeightForStand)
            }
        }
        tv_stand.setOnLongClickListener {
            viewModel.modifyMemoryHeightStand(viewModel.currentHeight, this)
            viewModel.showToast(getString(R.string.setting_stand_success))
            return@setOnLongClickListener false
        }

        tv_sit.setOnClickListener {
            if (viewModel.memoryHeightForSit == -1) {
                viewModel.showToast(getString(R.string.tips_setting_down))
            } else {
                viewModel.controlHight(viewModel.memoryHeightForSit)
            }
        }
        tv_sit.setOnLongClickListener {
            viewModel.modifyMemoryHeightSit(viewModel.currentHeight, this)
            viewModel.showToast(getString(R.string.setting_down_success))
            return@setOnLongClickListener false
            true
        }
        //上升和下降
        tv_up.setHandleButtonOnClickListener(handleViewListener)

        tv_down.setHandleButtonOnClickListener(handleViewListener)
    }




    //上下两键处理器
    private val handleViewListener = object : MoreLongHandleImageView.HandleButtonOnClickListener {
        override fun onShortPress(view: View) {
            when (view.tag) {
                "1" -> {
                    viewModel.onDeviceButtonChick(3, 1, this@DeskActivity)
                }
                "2" -> {
                    viewModel.onDeviceButtonChick(4, 1, this@DeskActivity)
                }
            }
        }

        override fun onLongPress(view: View) {
            when (view.tag) {
                "1" -> {
                    viewModel.onDeviceButtonChick(3, 2, this@DeskActivity)
                }
                "2" -> {
                    viewModel.onDeviceButtonChick(4, 2, this@DeskActivity)
                }
            }
        }

        override fun toMax(view: View) {
//            when (view.tag) {
//                "1" -> {
//                    viewModel.onDeviceButtonChick(3, 2, this@DeskActivity)
//                }
//                "2" -> {
//                    viewModel.onDeviceButtonChick(4, 2, this@DeskActivity)
//                }
//            }

        }

        override fun onUpspring(view: View) {
            when (view.tag) {
                "1" -> {
                    viewModel.onDeviceButtonChick(3, 0, this@DeskActivity)
                }
                "2" -> {
                    viewModel.onDeviceButtonChick(4, 0, this@DeskActivity)
                }
            }
        }

        override fun onDown(view: View) {
            when (view.tag) {
                "1" -> {
                    viewModel.onDeviceButtonChick(3, 0, this@DeskActivity)
                }
                "2" -> {
                    viewModel.onDeviceButtonChick(4, 0, this@DeskActivity)
                }
            }
        }

        override fun onUp(view: View) {

        }
    }


    override fun observeVM() {
        super.observeVM()
        viewModel.connectState.observe(this, Observer {
            dismissProgressDialog()
            if (!it){
                showDisconnectWindow()
            }
        })

        viewModel.unit.observe(this, Observer {
            tv_unit.text = if (it == 0) "mm" else "inch"
        })

        viewModel.heightForShow.observe(this, Observer {
            tv_value.text = it
        })
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(this)
        if (progressDialog != null) {
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.setCancelable(false)//点击屏幕和按返回键都不能取消加载框
            progressDialog!!.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        finish()
                    }
                })
            progressDialog!!.setMessage(getText(R.string.connect_device))    //设置内容
            progressDialog!!.show()
            //设置超时自动消失
            Handler().postDelayed({
                //取消加载框
                if (dismissProgressDialog()!!) {
                    //超时处理
                    viewModel.clear()
                    showDisconnectWindow()
                }
            }, 12000)//超时时间20秒
        }
    }

    private fun showDisconnectWindow(){
        val dialog = DisconnectedDialog()
        dialog.setCallback(object : DisconnectedDialog.Callback{
            override fun sure() {
                finish()
            }
        })
        dialog.show(supportFragmentManager, "disconnect")
    }

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
        viewModel.clear()
        dismissProgressDialog()
    }
}