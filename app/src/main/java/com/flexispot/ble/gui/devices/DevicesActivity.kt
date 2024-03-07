package com.flexispot.ble.gui.devices

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.ImgRelation
import com.flexispot.ble.LabelRelation
import com.flexispot.ble.R
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.devices.DevicesFactory
import com.flexispot.ble.databinding.FraDevicesBinding
import com.flexispot.ble.gui.dialog.DeleteDeviceDialog
import com.flexispot.ble.gui.dialog.ModifyNameDialog
import com.flexispot.ble.gui.ModifyWindow
import com.flexispot.ble.gui.WelcomeActivity
import com.flexispot.ble.gui.add.AddActivity
import com.flexispot.ble.gui.device.desk.DeskTwoActivity
import com.flexispot.ble.gui.device.desk_rotate.RotateDeskActivity
import com.flexispot.ble.gui.device.media.MediaTwoActivity
import com.flexispot.ble.gui.device.rack.RackTwoActivity
import com.flexispot.ble.gui.outkill.AppStatus
import com.flexispot.ble.gui.outkill.AppStatusManager
import com.flexispot.ble.gui.threadMill.TreadMillActivity
import com.luman.core.LumanHelper
import com.luman.mvvm.base.LuManActivity
import com.luman.mvvm.base.LumanAdapter
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fra_devices.*

/**
 * @author luman
 * @date 2019/11/26
 * 设备列表
 */
class DevicesActivity : LuManActivity<FraDevicesBinding, DevicesViewModel>() {

    companion object {
        const val REQUEST_OPEN_BT_CODE = 10086
    }

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mEditWindow: ModifyWindow? = null
    private var mModifyNameDialog: ModifyNameDialog? = null
    private var mSureDeleteDialog: DeleteDeviceDialog? = null
    private var tempDevice: Device? = null

    override fun layoutId() = R.layout.fra_devices
    override fun vmFactory() = DevicesFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
    }

    override fun viewOpe() {
        setTheme(R.style.AppTheme)
        //判断app状态
        if (AppStatusManager.getInstance().getAppStatus() == AppStatus.STATUS_RECYCLE){
            //被回收，跳转到启动页面
            var intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }


        checkSupport()
        super.viewOpe()
        mBluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager?.getAdapter()
        if (!isLocationEnabled()) {
            openGPS()
        }


        iv_add.setOnClickListener {
            if (checkPermission()) {
                checkBluetoothState(Device("", "", DeviceType.ALL.type,""))
            } else {
                askPermission(Device("", "", DeviceType.ALL.type,""))
            }
        }


        //空列表页
        iv_empty.setOnClickListener {
            if (checkPermission()) {
                checkBluetoothState(Device("", "", DeviceType.ALL.type,""))
            } else {
                askPermission(Device("", "", DeviceType.ALL.type,""))
            }
        }

        //全选
        tv_all.setOnClickListener {
            viewModel.selectAll()
            rv_devices.adapter?.notifyDataSetChanged()
        }

        //完成
        tv_complete.setOnClickListener {
            viewModel.clearSelectState()
            rv_devices.adapter?.notifyDataSetChanged()
        }

        //设备类型栏
        rv_types.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        rv_types.adapter =
            object : LumanAdapter<Int>(this, viewModel.labels(), R.layout.item_label) {
                override fun setWidget(data: Int, holder: BaseViewHolder, position: Int) {
                    holder.setText(R.id.tv_name, LabelRelation.getLabelByType(data))
                        .setTextColor(
                            R.id.tv_name,
                            if (position == viewModel.labelIndex()) Color.BLACK else Color.parseColor(
                                "#999999"
                            )
                        )
                        .setTextStyle(
                            R.id.tv_name,
                            if (position == viewModel.labelIndex()) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                        )
                        .setBackGroundResource(
                            R.id.tv_name,
                            if (position == viewModel.labelIndex()) R.drawable.bg_type_selected else Color.parseColor(
                                "#00000000"
                            )
                        )
                        .setClickListner(R.id.tv_name, View.OnClickListener {
                            viewModel.getByType(data)
                        })
                }
            }

        //设备列表
        rv_devices.layoutManager = GridLayoutManager(this, 2)
        rv_devices.adapter =
            object : LumanAdapter<Device>(this, viewModel.devices(), R.layout.item_device) {
                override fun setWidget(data: Device, holder: BaseViewHolder, position: Int) {
                    holder.loadImage(R.id.iv_pic, ImgRelation.getTypeInDevices(data.type))
                        .setText(R.id.tv_name, data.nickname)
                        .ifShadow(data.selected)
                        .setVisible(
                            R.id.cb_select,
                            if (viewModel.selectedDevices().size == 0) View.GONE else View.VISIBLE
                        )
                        .setChecked(R.id.cb_select, data.selected)
                        .setClickListner(R.id.ll_item, View.OnClickListener {
                            if (viewModel.selectedDevices().size > 0) {
                                if (!data.selected) {
                                    viewModel.selectOne(data)
                                } else {
                                    viewModel.reduceOne(data)
                                }
                                rv_devices.adapter?.notifyItemChanged(position)
                            } else {
                                if (checkPermission()) {
                                    checkBluetoothState(data)
                                } else {
                                    askPermission(data)
                                }
                            }
                        })
                        .setLongClickListner(R.id.ll_item, View.OnLongClickListener {
                            if (viewModel.selectedDevices().size == 0) {
                                viewModel.selectOne(data)
                            }
                            return@OnLongClickListener true
                        })
                }
            }

        viewModel.initData()
    }

    override fun observeVM() {
        super.observeVM()
        //设备列表刷新处理
        viewModel.devicesNeedRefresh().observe(this, Observer {
            if (gp_empty.visibility == View.VISIBLE) {
                if (viewModel.devices().size > 0) {
                    gp_empty.visibility = View.GONE
                    gp_device.visibility = View.VISIBLE
                }
            } else {
                if (viewModel.devices().size == 0) {
                    gp_empty.visibility = View.VISIBLE
                    gp_device.visibility = View.GONE
                }
            }
            (rv_devices.adapter as LumanAdapter<Device>?)?.reloadData(viewModel.devices())
        })
        //标签列表刷新处理
        viewModel.labelNeedRefresh().observe(this, Observer {
            rv_types.adapter?.notifyDataSetChanged()
        })
        //设备选择弹窗处理
        viewModel.selectedAmount().observe(this, Observer {
            if (it == 0) {
                mSureDeleteDialog?.dismiss()
                mModifyNameDialog?.dismiss()
                if (mEditWindow != null) {
                    mEditWindow?.dismiss()
                    rv_types.canTouch(true)
                    gp_title0.visibility = View.VISIBLE
                    gp_title1.visibility = View.GONE
                }
            } else {
                if (mEditWindow == null) {
                    mEditWindow = ModifyWindow(
                        this@DevicesActivity,
                        object : ModifyWindow.Callback {
                            override fun modify() {
                                if (viewModel.selectedDevices().size <= 0) {
                                    return
                                }
                                if (mModifyNameDialog == null) {
                                    mModifyNameDialog =
                                        ModifyNameDialog.getInstance(viewModel.repository.multiSelectedDevices[0])
                                    mModifyNameDialog!!.setCallback(object :
                                        ModifyNameDialog.Callback {
                                        override fun modified(
                                            newName: String
                                        ) {
                                            Logger.d("进行修改：$newName")
                                            viewModel.modifyName(newName)
                                        }

                                        override fun dismiss() {
                                            mEditWindow?.showAtLocation(
                                                cl_view,
                                                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                                                0,
                                                LumanHelper.aboutFunc().getScreenHeight() - LumanHelper.aboutFunc().dp2px(
                                                    167
                                                )
                                            )
                                        }
                                    })
                                } else {
                                    mModifyNameDialog?.arguments?.putSerializable(
                                        "params",
                                        viewModel.repository.multiSelectedDevices[0]
                                    )
                                }
                                mModifyNameDialog?.show(supportFragmentManager, "modify")
                            }

                            override fun delete() {
                                if (mSureDeleteDialog == null) {
                                    mSureDeleteDialog =
                                        DeleteDeviceDialog()
                                    mSureDeleteDialog!!.setCallback(object :
                                        DeleteDeviceDialog.Callback {
                                        override fun sure() {
                                            viewModel.deleteDevice()
                                        }

                                        override fun cancel() {
                                            if (viewModel.selectedDevices().size > 0) {
                                                mEditWindow?.showAtLocation(
                                                    cl_view,
                                                    Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                                                    0,
                                                    LumanHelper.aboutFunc().getScreenHeight() - LumanHelper.aboutFunc().dp2px(
                                                        167
                                                    )
                                                )
                                            }
                                        }
                                    })
                                }
                                mSureDeleteDialog?.show(supportFragmentManager, "delete")
                            }
                        })
                }
                mEditWindow?.changeEnabel(viewModel.selectedDevices().size == 1)
                if (!mEditWindow!!.isShowing) {
                    //改变顶部栏
                    gp_title0.visibility = View.GONE
                    gp_title1.visibility = View.VISIBLE
                    rv_types.canTouch(false)

                    rv_devices.adapter?.notifyDataSetChanged()
                    mEditWindow?.showAtLocation(
                        cl_view,
                        Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                        0,
                        LumanHelper.aboutFunc().getScreenHeight() - LumanHelper.aboutFunc().dp2px(
                            167
                        )
                    )
                }

                tv_amount.text =
                    "${getString(R.string.selected)}${viewModel.selectedDevices().size}${getString(R.string.amoount)}"
            }
        })
    }

    private fun checkPermission(): Boolean {
        val pm = packageManager
        val permission0 =
            PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                Manifest.permission.BLUETOOTH_ADMIN,
                packageName
            )
        val permission1 =
            PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                Manifest.permission.BLUETOOTH,
                packageName
            )
        val permission2 =
            PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                packageName
            )
        val permission3 =
            PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                packageName
            )
        return permission0 && permission1 && permission2
                && permission3
    }
    /**
     * 判断定位服务是否开启
     *
     * @param
     * @return true 表示开启
     */
    fun isLocationEnabled(): Boolean {
        var locationMode = 0
        val locationProviders: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode =
                    Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                return false
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            locationProviders = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
            return !TextUtils.isEmpty(locationProviders)
        }
    }

    private fun openGPS() {
        val builder = AlertDialog.Builder(this)
            .setTitle(getText(R.string.app_name))
            .setMessage(getText(R.string.must_open_Location))
            .setPositiveButton(getText(R.string.agree)) { dialogInterface, i ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, 887)
                dialogInterface.dismiss()
            }
            .setNegativeButton(getText(R.string.refuse)) { dialogInterface, i ->
                LumanHelper.aboutToast().showShortToast(R.string.thank_you_Location)
            }
        builder.show()
    }
    /**
     * 权限检查
     */
    private fun askPermission(device: Device) {
        tempDevice = device
        val permissonItems = arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissonItems, 5)
    }

    /**
     * 检查蓝牙状态
     */
    private fun checkBluetoothState(device: Device?) {
        if (device == null || mBluetoothAdapter == null) {
            return
        }
        if (!mBluetoothAdapter!!.isEnabled()) {
            val builder = AlertDialog.Builder(this)
                .setTitle(getText(R.string.app_name))
                .setMessage(getText(R.string.must_open_bluetooth))
                .setPositiveButton(getText(R.string.agree)) { dialogInterface, i ->
                    tempDevice = device
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(intent, REQUEST_OPEN_BT_CODE)
                }
                .setNegativeButton(getText(R.string.refuse)) { dialogInterface, i ->
                    LumanHelper.aboutToast().showShortToast(R.string.thank_you)
                }
            builder.show()
        } else {
            toNextPage(device)
        }
    }

    /**
     * 页面跳转
     */
    private fun toNextPage(device: Device) {
        val bundle = Bundle()
        bundle.putSerializable("params", device)
        val intent: Intent
        when (device.type) {
            DeviceType.DESK.type -> {
                if (device.secondType == 0) {
                    intent = Intent(this, DeskTwoActivity::class.java)
                } else {
                    intent = Intent(this, RotateDeskActivity::class.java)
                }
            }
            DeviceType.MEDIA.type -> {
                intent = Intent(this, MediaTwoActivity::class.java)
            }
            DeviceType.RACK.type -> {
                intent = Intent(this, RackTwoActivity::class.java)
            }
            DeviceType.THREAD.type -> {
                intent = Intent(this, TreadMillActivity::class.java)
            }
            else -> {
                intent = Intent(this, AddActivity::class.java)
            }
        }
        intent.putExtras(bundle)
        startActivity(intent)
    }

    /**
     * 判断是否支持蓝牙4.0
     */
    private fun checkSupport() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            val builder = AlertDialog.Builder(this@DevicesActivity)
                .setTitle(getText(R.string.erro))
                .setMessage(getText(R.string.not_support_device))
                .setPositiveButton(
                    getText(R.string.agree)
                ) { dialogInterface, i -> finish() }
                .setNegativeButton(
                    getText(R.string.cancel)
                ) { dialogInterface, i -> finish() }
            builder.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OPEN_BT_CODE && resultCode == Activity.RESULT_OK && tempDevice != null) {
            val open = mBluetoothAdapter!!.enable()
            if (open) {
                toNextPage(tempDevice!!)
            } else {
                viewModel.showToast(getString(R.string.please_open_bluetooth))
                val intent = Intent()
                intent.action = Settings.ACTION_BLUETOOTH_SETTINGS
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    startActivityForResult(intent, REQUEST_OPEN_BT_CODE)
                } catch (ex: ActivityNotFoundException) {
                    ex.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var times = 0
        if (requestCode == 5 && grantResults.size == 4) {
            for (tempRes in grantResults) {
                if (tempRes != PackageManager.PERMISSION_GRANTED) {
                    times = -1
                    break
                }
            }
        }
        if (times == 0) {
            checkBluetoothState(tempDevice)
        } else {
            //权限被拒绝了
            val listener =
                DialogInterface.OnClickListener { dialogInterface, i ->
                    run {
                        viewModel.showToast(getString(R.string.not_can_use_app))
                    }
                }
            val builder = AlertDialog.Builder(this@DevicesActivity)
                .setTitle(getText(R.string.warring))
                .setMessage(getText(R.string.not_can_use_app))
                .setPositiveButton(getText(R.string.complete), listener)
                .setNegativeButton(getText(R.string.cancel), listener)
            builder.show()
        }
    }
}