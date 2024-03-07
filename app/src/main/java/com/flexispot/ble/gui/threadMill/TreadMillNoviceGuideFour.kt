package com.flexispot.ble.gui.threadMill;

import android.content.Intent
import android.graphics.drawable.AnimationDrawable

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.clj.fastble.data.BleDevice
import com.flexispot.ble.R
import com.flexispot.ble.gui.threadMill.PreferencesUtility
import com.flexispot.ble.gui.threadMill.WaveView
import com.flexispot.ble.gui.view.StatusBarUtil


import com.walking.secretary.confignetwork.TreadMillModel
import com.luman.core.LumanHelper


class TreadMillNoviceGuideFour : AppCompatActivity(), TreadMillModel.OnOperationListener {
    override fun onGetScanDevice(scanResult: BleDevice) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMaxSpeed(speed: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFixed(length: Int, time: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRecord(recordTime: String, length: Int, time: Int, step: Int, num: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTreadTime(time: String, number: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun process(now: Int, max: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clean(a: Int, b: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var toolbar: Toolbar? = null
    private var kuang_one: ImageView? = null
    private var step_upper: ImageView? = null
    private var kuang_two: ImageView? = null
    private var step_two: ImageView? = null
    private var zidong_one: TextView? = null
    private var zidong_two: TextView? = null
    private var btn_next: Button? = null
    private var wave_one: WaveView? = null
    private var wave_two: WaveView? = null
    private var wave_three: WaveView? = null
    private var wave_four: WaveView? = null
    private var wave_five: WaveView? = null
    private var wave_six: WaveView? = null


    private var number: LinearLayout? = null


    private var a = 0
    private var b = 0//判断进行到第几部
    private var displaySpeed = 0.0//实时速度


    internal var mToast: Toast? = null

    internal var c = 0

    private var speedd = 0.0
    private var running = ""

    private val handler = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> setdata(speedd, running)

                else -> {
                }
            }

        }
    }

    private var isyingzhi = 0
    private var preferencesUtility: PreferencesUtility? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tread_novice_four)
        StatusBarUtil.setStatusBarMode(this, true, R.color.white)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setNavigationOnClickListener { finish() }

        val ac =
            LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setOnOperationListener(this)
        find()
        click()
        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setMaxSpeed(40)
        preferencesUtility = PreferencesUtility.getInstance(this@TreadMillNoviceGuideFour)
        isyingzhi = preferencesUtility!!.system
    }

    private fun find() {
        kuang_one = findViewById(R.id.kuang_one)
        step_upper = findViewById(R.id.step)//人在上面动画
        kuang_two = findViewById(R.id.kuang_two)
        step_two = findViewById(R.id.step_two)//人在下面动画
        zidong_one = findViewById(R.id.zidong_one)//未进入运动
        zidong_two = findViewById(R.id.zidong_two)//进入运动
        btn_next = findViewById(R.id.btn_next)
        step_upper!!.setBackgroundResource(R.drawable.thread_anim_two)
        step_two!!.setBackgroundResource(R.drawable.thread_anim_two)
        val animaition = step_upper!!.background as AnimationDrawable
        animaition.isOneShot = false
        animaition.start()
        val animaition2 = step_two!!.background as AnimationDrawable
        animaition2.isOneShot = false
        animaition2.start()
        number = findViewById(R.id.number)
        wave_one = findViewById(R.id.wave_one)
        wave_two = findViewById(R.id.wave_two)
        wave_three = findViewById(R.id.wave_three)
        wave_four = findViewById(R.id.wave_four)
        wave_five = findViewById(R.id.wave_five)
        wave_six = findViewById(R.id.wave_six)
        wave_one!!.progress = 0
        wave_two!!.progress = 0
        wave_three!!.progress = 0
        wave_four!!.progress = 0
        wave_five!!.progress = 0
        wave_six!!.progress = 0

    }

    private fun click() {
        btn_next!!.setOnClickListener {
            btn()
        }
    }

    private fun showToast(msg: String) {
        if (mToast == null) {
            mToast = Toast.makeText(this@TreadMillNoviceGuideFour, msg, Toast.LENGTH_SHORT)
        } else {
            mToast!!.setText(msg)
        }
        mToast!!.show()
    }


    private fun setdata(speed: Double, run: String) {

        if ((b == 1) or (b == 3) or (b == 5) && speed >= 40) {
            b++
            kuang_one!!.visibility = View.GONE
            step_upper!!.visibility = View.GONE
            kuang_two!!.visibility = View.VISIBLE
            step_two!!.visibility = View.VISIBLE
            c = 0
        } else if ((b == 2) or (b == 4) or (b == 6) && speed == 0.0) {
            b++
            kuang_one!!.visibility = View.VISIBLE
            step_upper!!.visibility = View.VISIBLE
            kuang_two!!.visibility = View.GONE
            step_two!!.visibility = View.GONE
            c = 0
        }
        if ((b == 1) or (b == 3) or (b == 5)) {
            if (isyingzhi==1){
                var s = String.format(
                    "%.1f",
                    displaySpeed * 0.62137119
                )
                val str =  getString(R.string.new_speed)+" <font color='#6A0EE'>" + s + "</font> mile/h <br/>" +
                        getString(R.string.control_thread)+  " <font color='#6A0EE'> 2.5 </font> mile/h"
                zidong_two!!.text = Html.fromHtml(str)
            }else{
                val str =  getString(R.string.new_speed)+" <font color='#6A0EE'>" + displaySpeed + "</font> km/h <br/>" +
                        getString(R.string.control_thread)+  " <font color='#6A0EE'> 4.0 </font> km/h"
                zidong_two!!.text = Html.fromHtml(str)
            }



        } else if ((b == 2) or (b == 4) or (b == 6)) {
            if (isyingzhi==1){
                var s = String.format(
                    "%.1f",
                    displaySpeed * 0.62137119
                )
                val str = getString(R.string.new_speed)+" <font color='#6A0EE'>" + s + "</font> mile/h <br/>" +
                        getString(R.string.control_thread)+  " <font color='#6A0EE'> 0 </font> mile/h"
                zidong_two!!.text = Html.fromHtml(str)
            }else{
                val str = getString(R.string.new_speed)+" <font color='#6A0EE'>" + displaySpeed + "</font> km/h <br/>" +
                        getString(R.string.control_thread)+  " <font color='#6A0EE'> 0 </font> km/h"
                zidong_two!!.text = Html.fromHtml(str)
            }




        }

        if (b == 7) {
            b++
//            if ("运行" == run) {
//                val ac =
//                    LumanHelper.aboutActivityManager().findAc(ThreadMillActivity::class.java.name)
//                ViewModelProviders.of(ac!!).get(ThreadMillModel::class.java).setkey(6)
//            }
            val intent =
                Intent(this@TreadMillNoviceGuideFour, TreadMillNoviceGuideFive::class.java)
            startActivity(intent)
            finish()
        }

        if (b == 1) {
            val b1 = (displaySpeed * 100 / 4).toInt()
            wave_one!!.progress = b1
        } else if (b == 2) {
            val b2 = ((4.0 - displaySpeed) * 100 / 4).toInt()
            wave_one!!.progress = 100
            wave_two!!.progress = b2
        } else if (b == 3) {
            val b3 = (displaySpeed * 100 / 4).toInt()
            wave_two!!.progress = 100
            wave_three!!.progress = b3
        } else if (b == 4) {
            val b4 = ((4.0 - displaySpeed) * 100 / 4).toInt()
            wave_three!!.progress = 100
            wave_four!!.progress = b4
        } else if (b == 5) {
            val b5 = (displaySpeed * 100 / 4).toInt()
            wave_four!!.progress = 100
            wave_five!!.progress = b5
        } else if (b == 6) {
            val b6 = ((4.0 - displaySpeed) * 100 / 4).toInt()
            wave_five!!.progress = 100
            wave_six!!.progress = b6
        }


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
        isYingZhi:Int
    ) {
        a++
        if (a == 1) {
            val ac =
                LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setShield(1, 1, 1, 1)
        } else if (a == 3) {


        }else if(a==20){
            if (speed<31){
                val ac =  LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setMaxSpeed(40)
            }
        } else if (a > 4) {
            if ("自动" != stats) {
                val ac =
                    LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setkey(2)
            }

        }
        displaySpeed = speed / 10
        running = run
        speedd = speed
        if (speed>0.1&&isspeek){
            btn()
        }
        val message = Message()
        message.what = 1
        handler.sendMessage(message)
    }

    private var isspeek:Boolean=true

    private fun btn() {
        isspeek=false
        step_upper!!.visibility = View.VISIBLE
        b = 1
        zidong_one!!.visibility = View.GONE
        btn_next!!.visibility = View.GONE
        zidong_two!!.visibility = View.VISIBLE
        number!!.visibility = View.VISIBLE
        if (isyingzhi==1){
            var s = String.format(
                "%.1f",
                displaySpeed * 0.62137119
            )
            val str = getString(R.string.new_speed)+" <font color='#6A0EE'>" + s + "</font> mile/h <br/>" +
                    getString(R.string.control_thread)+  " <font color='#6A0EE'> 2.485 </font> mile/h"
            zidong_two!!.text = Html.fromHtml(str)
        }else{
            val str = getString(R.string.new_speed)+" <font color='#6A0EE'>" + displaySpeed + "</font> km/h <br/>" +
                    getString(R.string.control_thread)+  " <font color='#6A0EE'> 4.0 </font> km/h"
            zidong_two!!.text = Html.fromHtml(str)
        }
        c = 0
    }

}