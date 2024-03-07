package com.flexispot.ble.gui.threadMill;

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Html
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.clj.fastble.data.BleDevice
import com.flexispot.ble.R
import com.flexispot.ble.gui.threadMill.PreferencesUtility
import com.flexispot.ble.gui.view.StatusBarUtil
import com.luman.core.LumanHelper

import com.walking.secretary.confignetwork.TreadMillModel

class TreadMillNoviceGuideTwo : AppCompatActivity(), TreadMillModel.OnOperationListener {
    override fun onGetScanDevice(scanResult: BleDevice) {

    }

    override fun onMaxSpeed(speed: Double) {

    }

    override fun onFixed(length: Int, time: Int) {

    }

    override fun onRecord(recordTime: String, length: Int, time: Int, step: Int, num: Int) {

    }

    override fun onTreadTime(time: String, number: Int) {

    }

    override fun process(now: Int, max: Int) {

    }

    override fun clean(a: Int, b: Int) {

    }

    private var text_view: TextView? = null
    private var text_view_next: TextView? = null
    private var rel_next: RelativeLayout? = null
    private var progressBar: ProgressBar? = null
    private var maxtime: TextView? = null
    private var anim_one: ImageView? = null
    private var anim_two: ImageView? = null
    private var toolbar: Toolbar? = null

    private var a = 0//判断时间
    private var b = 0//判断到哪一步0启动1运动中
    private var c = 0//判断3km/h持续多长时间

    private var displaySpeed = 0.0//实时速度
    private var falg = true
    private var isyingzhi = 0

    internal var mToast: Toast? = null
    private val handler = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> {
                    b = 1
                    if (isyingzhi==1){
                        text_view!!.text = getString(R.string.thread_remind_yin)

                    }else{
                        text_view!!.text = getString(R.string.thread_remind)
                    }

                    text_view_next!!.visibility = View.VISIBLE
                    rel_next!!.visibility = View.VISIBLE
                    anim_one!!.setBackgroundResource(R.drawable.thread_anim_one)
                    anim_two!!.setBackgroundResource(R.drawable.thread_anim_two)
                    val animaition = anim_one!!.background as AnimationDrawable
                    animaition.isOneShot = false
                    animaition.start()
                    val animaition2 = anim_two!!.background as AnimationDrawable
                    animaition2.isOneShot = false
                    animaition2.start()
                }
                2 -> {
                    val cc = c * 100 / 60
                    progressBar!!.progress = cc
                    maxtime!!.text = getString(R.string.already_walked) + " " + c / 2 + "s"
                }
                3 -> {
                    if (isyingzhi == 1) {
                        var s = String.format(
                            "%.1f",
                            displaySpeed * 0.62137119
                        )
                        val str =
                            getString(R.string.new_speed) + " <font color='#6A0EE'>$s </font> mile/h"
                        text_view_next!!.text = Html.fromHtml(str)
                    } else {
                        val str =
                            getString(R.string.new_speed) + " <font color='#6A0EE'>$displaySpeed </font> km/h"
                        text_view_next!!.text = Html.fromHtml(str)
                    }


                }
                else -> {
                }
            }

        }
    }

    private var preferencesUtility: PreferencesUtility? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tread_novice_two)
        StatusBarUtil.setStatusBarMode(this, true, R.color.white)
        preferencesUtility = PreferencesUtility.getInstance(this@TreadMillNoviceGuideTwo)
        isyingzhi = preferencesUtility!!.system


        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setNavigationOnClickListener { finish() }

        find()


        val ac =
            LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)

        if (ac != null) {
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance()
                .setOnOperationListener(this)
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance()
                .setMaxSpeed(30)
        }


    }

    private fun find() {

        anim_one = findViewById(R.id.anim_one)
        anim_two = findViewById(R.id.anim_two)
        text_view = findViewById(R.id.text_view)
        text_view_next = findViewById(R.id.text_view_next)
        rel_next = findViewById(R.id.rel_next)
        progressBar = findViewById(R.id.press)
        maxtime = findViewById(R.id.time)
        val str = getString(R.string.walking_thread) + "<br/>" + getString(R.string.qidong)
        text_view!!.text = Html.fromHtml(str)


    }

    private fun showToast(msg: String) {
        if (mToast == null) {
            mToast = Toast.makeText(this@TreadMillNoviceGuideTwo, msg, Toast.LENGTH_SHORT)
        } else {
            mToast!!.setText(msg)
        }
        mToast!!.show()
    }

    override fun onDestroy() {

        super.onDestroy()
    }

    override fun onScanFailure() {

    }

    override fun onStartConnect() {

    }

    override fun onConnectSuccess() {

    }

    override fun onConnectFail() {

    }
    override fun onConnectFailService() {

    }
    override fun onGetDeviceInfo(maxheight: Int, minheight: Int, unit: Int) {

    }

    override fun onSpeed(
        speed: Double,
        length: Int,
        time: Int,
        step: Int,
        stats: String,
        id: Int,
        run: String,
        maxspeed: Int,
        isYingZhi: Int
    ) {

        a++
        if (a == 2) {
            val ac =
                LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance()
                .setShield(0, 0, 0, 1)
        } else if (a == 4) {
            if ("手动" != stats) {
                val ac =
                    LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setkey(1)
            }
        }

        displaySpeed = speed / 10
        val message = Message()
        message.what = 3
        handler.sendMessage(message)

        if (speed > 0 && b == 0) {
            val message = Message()
            message.what = 1
            handler.sendMessage(message)
        }

        if (displaySpeed >= 3 && b == 1) {
            c++//0.5s一次
            val message = Message()
            message.what = 2
            handler.sendMessage(message)
        }

        if (c >= 60 && falg) {
            falg = false
            if (run == "运行") {
                val ac =
                    LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setkey(6)
                //    showToast("进来了，停止");
            }
            //跳转
            val intent =
                Intent(this@TreadMillNoviceGuideTwo, TreadMillNoviceGuideThree::class.java)
            startActivity(intent)
            finish()
        }

    }

}