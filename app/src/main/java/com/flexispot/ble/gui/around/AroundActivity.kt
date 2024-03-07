package com.flexispot.ble.gui.around

import `in`.srain.cube.views.ptr.PtrClassicDefaultHeader
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.ImgRelation
import com.flexispot.ble.R
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.around.SearchModelFactory
import com.flexispot.ble.databinding.FraAroundDevicesBinding
import com.flexispot.ble.gui.connected.ConnectedActivity
import com.flexispot.ble.gui.devices.DevicesActivity
import com.flexispot.ble.gui.devices.DevicesViewModel
import com.luman.core.LumanHelper
import com.luman.mvvm.base.LuManActivity
import com.luman.mvvm.base.LumanAdapter
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fra_around_devices.*
import kotlinx.android.synthetic.main.topbar.*
import java.io.Serializable


/**
 * @author luman
 * @date 19-11-26
 * 周边设备
 **/
class AroundActivity : LuManActivity<FraAroundDevicesBinding, AroundViewModel>() {

    override fun layoutId() = R.layout.fra_around_devices
    override fun vmFactory() = SearchModelFactory()

    private var mType: DeviceType? = null
    private var mDevName: String? = null
    private lateinit var mHeader: PtrClassicDefaultHeader

    override fun viewOpe() {
        super.viewOpe()

        mType = intent.extras!!.get("type") as DeviceType?
        tv_title.setText(R.string.nearby)
        mDevName = intent.extras!!.get("name") as String?
        iv_back.setOnClickListener {
            finish()
        }
        rv_data.layoutManager = GridLayoutManager(this, 2)
        rv_data.adapter = object :
            LumanAdapter<Device>(this, viewModel.devices(), R.layout.item_device_around) {
            override fun setWidget(data: Device, holder: BaseViewHolder, position: Int) {
                holder.loadImage(R.id.iv_pic, ImgRelation.getTypeInDevices(data.type))
                    .setText(R.id.tv_name, data.name)
                    .setClickListner(R.id.ll_item, View.OnClickListener {
                        //跳转至连接成功页面
                        val bundle = Bundle()
                        bundle.putSerializable("params", data)
                        Logger.d("二级类型：${data.secondType}")
                        val intent = Intent(this@AroundActivity, ConnectedActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                        setResult(Activity.RESULT_OK)
                        finish()
                    })
            }
        }
        mHeader = PtrClassicDefaultHeader(this)
        sr_data.headerView = mHeader
        sr_data.isKeepHeaderWhenRefresh = true
        sr_data.addPtrUIHandler(mHeader)
        sr_data.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                if (viewModel.bleTool() == null) {
                    val ac =
                        LumanHelper.aboutActivityManager().findAc(DevicesActivity::class.java.name)
                    if (ac != null) {
                        try {
                            viewModel.beginSearch(
                                ViewModelProviders.of(ac).get(DevicesViewModel::class.java).devices(),
                                this@AroundActivity, mType, mDevName
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            LumanHelper.aboutToast().showShortToast(R.string.unkown_error)
                            finish()
                        }
                    }
                } else {
                    viewModel.searchAgain()
                }
            }

            override fun checkCanDoRefresh(
                frame: PtrFrameLayout?,
                content: View?,
                header: View?
            ): Boolean {
                return rv_data.tag == "1"
            }
        })

        sr_data.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                sr_data.viewTreeObserver.removeOnPreDrawListener(this)
                sr_data.autoRefresh()
                return true
            }
        })

    }

    override fun observeVM() {
        super.observeVM()
        viewModel.refresh().observe(this, Observer {
            rv_data.adapter?.notifyDataSetChanged()
        })
        viewModel.end().observe(this, Observer {
            rv_data.tag = "1"
            sr_data.refreshComplete()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
    }
}