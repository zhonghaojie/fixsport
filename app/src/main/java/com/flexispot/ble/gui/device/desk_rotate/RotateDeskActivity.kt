package com.flexispot.ble.gui.device.desk_rotate

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import com.flexispot.ble.R
import com.flexispot.ble.data.RotateDeskData
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.device.desk_rotate.RotateDeskViewModelFactory
import com.flexispot.ble.databinding.FraDeviceDeskRotateBinding
import com.flexispot.ble.gui.view.MoreLongHandleImageView
import com.luman.mvvm.base.LuManActivity
import kotlinx.android.synthetic.main.fra_device_desk_rotate.*
import kotlinx.android.synthetic.main.topbar_w.*

/**
 * @author luman
 * @date 19-11-29
 **/
class RotateDeskActivity : LuManActivity<FraDeviceDeskRotateBinding, RotateDeskViewModel>() {

    private var progressDialog: ProgressDialog? = null

    override fun layoutId() = R.layout.fra_device_desk_rotate
    override fun vmFactory() = RotateDeskViewModelFactory()
    override fun barColor() = R.color.detailTop
    override fun ifDark() = false

    override fun viewOpe() {
        super.viewOpe()
        super.viewOpe()
        if (intent.extras == null || intent.extras!!["params"] == null) {
            finish()
        }
        showProgressDialog()
        viewModel.init(intent.extras!!["params"] as Device)
        viewModel.connectDevice(this)
        iv_back.setOnClickListener {
            finish()
        }
        tv_title.setText(R.string.rotate_desk)

        tv_down.setHandleButtonOnClickListener(listener)
        tv_up.setHandleButtonOnClickListener(listener)
        tv_add.setHandleButtonOnClickListener(listener)
        tv_reduce.setHandleButtonOnClickListener(listener)
    }

    override fun observeVM() {
        super.observeVM()

        viewModel.connectState.observe(this, Observer {
            if (it) {
                dismissProgressDialog()
            } else {
                viewModel.showToast(getString(R.string.dis_connect_device))
                finish()
            }
        })

        viewModel.heightForShow.observe(this, Observer {
            tv_height.text = it
        })

        viewModel.angleForShow.observe(this, Observer {
            tv_angle.text = it
        })
    }

    private val listener = object : MoreLongHandleImageView.HandleButtonOnClickListener {
        override fun onLongPress(view: View) {}
        override fun toMax(view: View) {}
        override fun onUpspring(view: View) {}

        override fun onShortPress(view: View) {
            when (view.tag) {
                "0" -> {
                    if (viewModel.repository.minHeight == 0f || viewModel.currentHeight > viewModel.repository.minHeight) {
                        viewModel.sendData(RotateDeskData.controlDown())
                    } else {
                        //TODO 提示已达最低
                    }
                }
                "1" -> {
                    if (viewModel.repository.maxHeight == 0f || viewModel.currentHeight < viewModel.repository.maxHeight) {
                        viewModel.sendData(RotateDeskData.controlUp())
                    } else {
                        //TODO 提示已达最高j
                    }
                }
                "2" -> {
                    viewModel.sendData(RotateDeskData.controlOppositeRotate())
                }
                "3" -> {
                    viewModel.sendData(RotateDeskData.controlForwardRotate())
                }
                else -> {
                }
            }
        }

        override fun onUp(view: View) {
            when (view.tag) {
                "0" -> {

                }
                "1" -> {

                }
                "2" -> {

                }
                "3" -> {

                }
                else -> {

                }
            }
        }

        override fun onDown(view: View) {
            when (view.tag) {
                "0" -> {

                }
                "1" -> {

                }
                "2" -> {

                }
                "3" -> {

                }
                else -> {

                }
            }
        }
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
                getString(R.string.cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        finish()
                    }
                })
            progressDialog!!.setCancelable(false)//点击屏幕和按返回键都不能取消加载框
            progressDialog!!.setMessage(getText(R.string.connect_device))    //设置内容
            progressDialog!!.show()
            //设置超时自动消失
            Handler().postDelayed({
                //取消加载框
                if (dismissProgressDialog()!!) {
                    //超时处理
                    viewModel.showToast(getString(R.string.connect_time_out))
                    finish()
                }
            }, 20000)//超时时间20秒
        }
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
        viewModel.destroy()
        dismissProgressDialog()
    }
}