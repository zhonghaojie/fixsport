package com.flexispot.ble.gui.connected

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProviders
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.R
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.databinding.FraConnectedBinding
import com.flexispot.ble.gui.device.desk.DeskTwoActivity
import com.flexispot.ble.gui.device.desk_rotate.RotateDeskActivity
import com.flexispot.ble.gui.device.media.MediaTwoActivity
import com.flexispot.ble.gui.device.rack.RackTwoActivity
import com.flexispot.ble.gui.devices.DevicesActivity
import com.flexispot.ble.gui.devices.DevicesViewModel
import com.flexispot.ble.gui.threadMill.TreadMillActivity

import com.luman.core.LumanHelper
import com.luman.mvvm.base.LuManActivity
import kotlinx.android.synthetic.main.fra_connected.*
import kotlinx.android.synthetic.main.topbar.*

/**
 * @author luman
 * @date 19-11-26
 * 连接成功
 **/
class ConnectedActivity : LuManActivity<FraConnectedBinding, ConnectedVIewModel>() {

    override fun layoutId() = R.layout.fra_connected

    override fun viewOpe() {
        super.viewOpe()
        tv_title.setText(R.string.connect_device_two)
        iv_back.setOnClickListener {
            finish()
        }

        val device = intent.extras!!["params"] as Device
        et_name.setText(device.name)
        et_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    tv_submit.isEnabled = false
                    tv_submit.setTextColor(Color.parseColor("#017AFF"))
                } else if (!tv_submit.isEnabled) {
                    tv_submit.isEnabled = true
                    tv_submit.setTextColor(Color.WHITE)
                }
            }
        })

        iv_pic.setImageResource(
            when (device.type) {
                DeviceType.MEDIA.type -> {
                    R.mipmap.ic_media_b
                }
                DeviceType.DESK.type -> {
                    R.mipmap.ic_desk_b
                }
                DeviceType.RACK.type -> {
                R.mipmap.ic_rask_b
            }
                DeviceType.THREAD.type -> {
                    R.mipmap.pic_treadmill
                }
                else -> {
                    R.mipmap.ic_media_b
                }
            }
        )

        tv_submit.setOnClickListener {
            if (et_name.text.toString().trim() == "") {
                viewModel.showToast(getString(R.string.input_sth))
            } else {
                device.nickname = et_name.text.toString().trim()
                device.name = device.name
                val ac = LumanHelper.aboutActivityManager().findAc(DevicesActivity::class.java.name)
                ViewModelProviders.of(ac!!).get(DevicesViewModel::class.java)
                    .addDevices(device)
                val bundle = Bundle()
                bundle.putSerializable("params", device)
                val intent: Intent
                when (device.type) {
                    DeviceType.RACK.type -> {
                        intent = Intent(this, RackTwoActivity::class.java)
                    }
                    DeviceType.MEDIA.type -> {
                        intent = Intent(this, MediaTwoActivity::class.java)
                    }
                    DeviceType.DESK.type -> {
                        if (device.secondType == 0) {
                            intent = Intent(this, DeskTwoActivity::class.java)

                        } else {
                            intent = Intent(this, RotateDeskActivity::class.java)

                        }
                    }
                    DeviceType.THREAD.type -> {
                        intent = Intent(this, TreadMillActivity::class.java)
                    }
                    else -> {
                        intent = Intent(this, RackTwoActivity::class.java)
                    }
                }
                intent.putExtras(bundle)
                startActivity(intent)
                finish()

            }
        }
    }
}