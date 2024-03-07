package com.flexispot.ble.gui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import com.gyf.barlibrary.ImmersionBar
import com.flexispot.ble.R

import com.flexispot.ble.gui.devices.DevicesActivity
import com.flexispot.ble.gui.devices.WebViewActivity
import com.flexispot.ble.gui.outkill.AppStatus
import com.flexispot.ble.gui.outkill.AppStatusManager
import com.flexispot.ble.gui.threadMill.PreferencesUtility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author luman
 * @date 19-12-16
 **/
class WelcomeActivity : AppCompatActivity() {
    private var preferencesUtility: PreferencesUtility? = null
    private var user: Int = 0;


    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_welcome)
        ImmersionBar.with(this).statusBarDarkFont(true).fitsSystemWindows(true)
            .statusBarColor(R.color.white).init()
        GlobalScope.launch {
            delay(2000)
            //app状态改为正常

            if (user == 0) {
                val message = Message()
                message.what = 1
                handler.sendMessage(message)
            }else{
                AppStatusManager.getInstance().setAppStatus(AppStatus.STATUS_NORMAL);
                startActivity(Intent(this@WelcomeActivity, DevicesActivity::class.java))
                finish()
            }
        }
        preferencesUtility = PreferencesUtility.getInstance(this@WelcomeActivity)
        user = preferencesUtility!!.getUser()
    }

    var dialog:AgreeDailog?=null

    private fun showUserAgreementDialog() {
        if (dialog==null){
            dialog=AgreeDailog(this@WelcomeActivity)
        }


        dialog!!.show()
        dialog!!.setLeftButton{
            dialog?.dismiss()
            finish()
        }
        dialog!!.setRightButton(){
            preferencesUtility!!.user = 1
            AppStatusManager.getInstance().setAppStatus(AppStatus.STATUS_NORMAL);
            startActivity(Intent(this@WelcomeActivity, DevicesActivity::class.java))
            finish()
            dialog?.dismiss()
        }
        dialog!!.setHintText {
            startActivity(Intent(this@WelcomeActivity, WebViewActivity::class.java))
        }

    }
    private val handler = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> {
                    showUserAgreementDialog()
                }

                else -> {
                }
            }

        }
    }


}