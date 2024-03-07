package com.flexispot.ble.gui.add

import android.Manifest
import android.Manifest.permission
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.LabelRelation
import com.flexispot.ble.R
import com.flexispot.ble.data.repository.add.AddDeviceFactory
import com.flexispot.ble.databinding.FraAddDeviceBinding
import com.flexispot.ble.gui.around.AroundActivity
import com.flexispot.ble.gui.device.desk.DeskTwoActivity
import com.flexispot.ble.gui.device.desk_rotate.RotateDeskActivity
import com.flexispot.ble.gui.device.media.MediaActivity
import com.flexispot.ble.gui.device.rack.RackActivity
import com.flexispot.ble.gui.devices.DevicesActivity
import com.flexispot.ble.gui.devices.DevicesViewModel
import com.flexispot.ble.gui.threadMill.TreadMillActivity

import com.luman.core.LumanHelper
import com.luman.mvvm.base.LuManActivity
import com.luman.mvvm.base.LumanAdapter
import com.orhanobut.logger.Logger
import com.uuzuche.lib_zxing.activity.CaptureActivity
import com.uuzuche.lib_zxing.activity.CodeUtils

import kotlinx.android.synthetic.main.fra_add_device.*
import kotlinx.android.synthetic.main.topbar.*
import org.json.JSONObject


/**
 * @author luman
 * @date 19-11-26
 * 添加设备
 **/
class AddActivity : LuManActivity<FraAddDeviceBinding, AddViewModel>() {

    companion object {
        const val REQUEST_CODE = 1
        const val TO_AROUND_CODE = 2
    }

    override fun layoutId() = R.layout.fra_add_device
    override fun vmFactory() = AddDeviceFactory()

    override fun viewOpe() {
        super.viewOpe()
        tv_title.setText(R.string.add_device)
        iv_back.setOnClickListener {
            finish()
        }
        iv_ope_r.setImageResource(R.mipmap.ic_scan)
        iv_ope_r.setOnClickListener {
            if (checkPermission()) {
                val intent = Intent(this@AddActivity, CaptureActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
            } else {
                askPermission()
            }
        }
        //原二级列表实现
//        val lm = GridLayoutManager(context, 4)
//        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(i: Int) = if (viewModel.types()[i].type == 0) 4 else 1
//        }
//        rv_data.layoutManager = lm
//        rv_data.adapter = AddDeviceAdapter(viewModel.types(), context!!, arguments!!["params"] as ArrayList<Device>)
        //一级列表
        rv_data.layoutManager = LinearLayoutManager(this)
        rv_data.adapter =
            object : LumanAdapter<DeviceType>(this, viewModel.types(), R.layout.item_type) {
                override fun setWidget(data: DeviceType, holder: BaseViewHolder, position: Int) {
                    when (data.type) {
                        DeviceType.RACK.type -> {
                            holder.setBackGroundResource(R.id.cl_item, R.drawable.bg_type_rask)
                                .loadImage(R.id.iv_pic, R.mipmap.ic_rask_b)
                        }
                        DeviceType.DESK.type -> {
                            holder.setBackGroundResource(R.id.cl_item, R.drawable.bg_type_desk)
                                .loadImage(R.id.iv_pic, R.mipmap.ic_desk_b)
                        }
                        DeviceType.MEDIA.type -> {
                            holder.setBackGroundResource(R.id.cl_item, R.drawable.bg_type_media)
                                .loadImage(R.id.iv_pic, R.mipmap.ic_media_b)
                        }
                        DeviceType.THREAD.type -> {
                            holder.setBackGroundResource(R.id.cl_item, R.drawable.bg_type_thread)
                                .loadImage(R.id.iv_pic, R.mipmap.pic_treadmill)
                        }
                    }

                        holder.setText(R.id.tv_name, LabelRelation.getLabelByType(data.type))
                            .setClickListner(R.id.cl_item, View.OnClickListener {
                                val bundle = Bundle()
                                bundle.putSerializable("type", data)
                                val intent = Intent(this@AddActivity, AroundActivity::class.java)
                                intent.putExtras(bundle)
                                startActivityForResult(intent, TO_AROUND_CODE)
                            })



                }
            }
    }

    /**
     * 权限检查
     */
    private fun checkPermission(): Boolean {
        val pm = packageManager
        val permission0 =
            PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                permission.CAMERA,
                packageName
            )
        val permission1 =
            PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                permission.VIBRATE,
                packageName
            )
        return permission0 && permission1
    }

    /**
     * 权限获取
     */
    private fun askPermission() {
        val permissonItems = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.VIBRATE
        )
        ActivityCompat.requestPermissions(this, permissonItems, 5)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                val bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    val result = bundle.getString(CodeUtils.RESULT_STRING);
                    Logger.d("扫码结果$result")
                    try {
                        val obj = JSONObject(result)
                        val devName = obj["bluetooth_name"]
                        if (devName.toString().isEmpty()) {
                            viewModel.showToast(getString(R.string.content_error))
                        } else {
                            val bundle1 = Bundle()
                            bundle1.putString("name", devName.toString())
                            //匹配本地数据
                            val ac = LumanHelper.aboutActivityManager()
                                .findAc(DevicesActivity::class.java.name)
                            if (ac != null) {
                                try {
                                    val devices =
                                        ViewModelProviders.of(ac).get(DevicesViewModel::class.java)
                                            .devices()
                                    for (tempDevice in devices) {
                                        if (tempDevice.name == devName.toString()) {
                                            val bundle = Bundle()
                                            bundle.putSerializable("params", tempDevice)
                                            val intent: Intent
                                            when (tempDevice.type) {
                                                DeviceType.RACK.type -> {
                                                    intent = Intent(this, RackActivity::class.java)
                                                }
                                                DeviceType.MEDIA.type -> {
                                                    intent = Intent(this, MediaActivity::class.java)
                                                }
                                                DeviceType.DESK.type -> {
                                                    if (tempDevice.secondType == 0) {
                                                        intent =
                                                            Intent(this, DeskTwoActivity::class.java)
                                                    } else {
                                                        intent = Intent(
                                                            this,
                                                            RotateDeskActivity::class.java
                                                        )
                                                    }
                                                }
                                                DeviceType.THREAD.type -> {
                                                    intent = Intent(this, TreadMillActivity::class.java)
                                                }
                                                else -> {
                                                    intent = Intent(this, RackActivity::class.java)
                                                }
                                            }
                                            intent.putExtras(bundle)
                                            startActivity(intent)
                                            return
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    LumanHelper.aboutToast().showShortToast(R.string.unkown_error)
                                    finish()
                                }
                            }
                            //本地无匹配数据，跳转页面
                            val intent = Intent(this@AddActivity, AroundActivity::class.java)
                            intent.putExtras(bundle1)
                            startActivity(intent)
                        }
                    } catch (e: Exception) {
                        viewModel.showToast(getString(R.string.content_error))
                        e.printStackTrace()
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    viewModel.showToast(getString(R.string.content_error))
                }
            }
        } else if (requestCode == TO_AROUND_CODE && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var times = 0
        if (requestCode == 5 && grantResults.size == 2) {
            for (tempRes in grantResults) {
                if (tempRes != PackageManager.PERMISSION_GRANTED) {
                    times = -1
                    break
                }
            }
        }
        if (times == 0) {
            val intent = Intent(this@AddActivity, CaptureActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        } else {
            //权限被拒绝了
            val listener =
                DialogInterface.OnClickListener { dialogInterface, i ->
                    run {
                        viewModel.showToast(getString(R.string.not_can_use_function))
                    }
                }
            val builder = AlertDialog.Builder(this@AddActivity)
                .setTitle(getText(R.string.warring))
                .setMessage(getText(R.string.not_can_use_function))
                .setPositiveButton(getText(R.string.complete), listener)
                .setNegativeButton(getText(R.string.cancel), listener)
            builder.show()
        }
    }
}