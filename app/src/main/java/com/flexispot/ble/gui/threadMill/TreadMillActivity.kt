package com.flexispot.ble.gui.threadMill;

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.clj.fastble.data.BleDevice
import com.flexispot.ble.R
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.databinding.ActivityThreadBinding
import com.flexispot.ble.gui.dialog.DisconnectedDialog
import com.flexispot.ble.gui.dialog.DisconnectedTwoDialog
import com.flexispot.ble.gui.threadMill.MotionDailog
import com.flexispot.ble.gui.threadMill.PreferencesUtility
import com.flexispot.ble.gui.view.BtnCy

import com.luman.core.LumanHelper
import com.luman.mvvm.base.LuManActivity

import com.walking.secretary.confignetwork.TreadMillModel
import kotlinx.android.synthetic.main.activity_thread.*
import java.util.*

class TreadMillActivity : LuManActivity<ActivityThreadBinding, TreadMillModel>() {
    private var isConnecting: Boolean = false

    private val bleListener = object : TreadMillModel.OnOperationListener {

        override fun onGetScanDevice(scanResult: BleDevice) {
            if (scanResult != null) {
                if (TextUtils.equals(name, scanResult.getName())) {
                    //                    if (TextUtils.equals(intent.getStringExtra("address"),scanResult.mac)){
                    isConnecting = true
                    viewModel.getInstance().startConnect(scanResult.getMac())
                    viewModel.getInstance().stopScan()
//                    println("ppppppppppppp")
                }
            }
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

        override fun onStartConnect() {

        }

        override fun onConnectSuccess() {
            dismissProgressDialog()

        }

        override fun onConnectFail() {
            dismissProgressDialog()
            showDisconnectWindow()



        }

        override fun onConnectFailService() {
            dismissProgressDialog()
            showDisconnectServiceWindow()
        }

        override fun onGetDeviceInfo(maxheight: Int, minheight: Int, unit: Int) {
            panduan(minheight, unit)
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
            Contuntime++
            if (Contuntime == 4) {
                viewModel.getInstance().getDeviceInfo();
            }

            if (Contuntime % 2 == 0) {
                if (isyingzhi != isYingZhi) {
                    isyingzhi = isYingZhi
                    var message = Message()
                    message.what = 10;
                    handler1.sendMessage(message);

                }

                running(run)
                displaySpeed = speed / 10
                displayTime = time
                displayStep = step
                displayLength = length
                if (displayStats.equals("童锁")) {
                    displayStats = "自动"
                }
                if (!displayStats.equals(stats)) {
                    displayStats = stats
                    moshi()
                }


                var message = Message()
                message.what = 6;
                handler1.sendMessage(message);

            }

        }

        override fun onScanFailure() {

        }

    }
    private var isOneInToActivity=true
    private var unitflag = "";
    private fun panduan(minheight: Int, unit: Int) {
        unitflag = "" + unit
//        println("ppppppppp"+unit+"p"+preferencesUtility!!.novice)
        if (isOneInToActivity) {
            if (unit == 0) {//没有自动,新手引导没法完成，不进入新手引导
                lin_choice.visibility = View.GONE
                xinshou.visibility = View.GONE
                xinshou_two.visibility = View.GONE
            } else {
                lin_choice.visibility = View.VISIBLE
                val dialog = MotionDailog(this)
                if (minheight < 60) {
                    dialog.show()
                    xinshou.visibility = View.VISIBLE
                    xinshou_two.visibility = View.VISIBLE
                    dialog.setEditText(getString(R.string.thread_unlock))
                    dialog.setLeftButton(resources.getString(R.string.cancel)) {
                        dialog.dismiss()
                    }
                    dialog.setRightButton(resources.getString(R.string.submut)) {
                        //                    Xins(0)
                        val intent =
                            Intent(this@TreadMillActivity, TreadMillNoviceGuideOne::class.java)
                        startActivityForResult(intent, REQUEST_CODE_HEIGHT_Novice)
                        dialog.dismiss()
                    }
                    if (minheight!=30){
                        viewModel.getInstance().setMaxSpeed(30)//设置速度
                    }
                    preferencesUtility!!.novice = 0
                }
//                else {
//                    xinshou.visibility = View.GONE
//                    xinshou_two.visibility = View.GONE
//                    dialog.setEditText(getString(R.string.thread_lock))
//                    dialog.setLeftButton(resources.getString(R.string.cancel)) {
//                        dialog.dismiss()
//                    }
//                    dialog.setRightButton(resources.getString(R.string.submut)) {
//                        //                    Xins(0)
//                        val intent =
//                            Intent(this@TreadMillActivity, TreadMillNoviceGuideOne::class.java)
//                        startActivityForResult(intent, REQUEST_CODE_HEIGHT_Novice)
//                        dialog.dismiss()
//
//                    }
//                }
            }

        }else{
            if (minheight < 60) {
                xinshou.visibility = View.VISIBLE
                xinshou_two.visibility = View.VISIBLE
            } else {
                xinshou.visibility = View.GONE
                xinshou_two.visibility = View.GONE
            }
        }
    }

    var Contuntime = 0;
    var isyingzhi = 0
    private val handler = Handler()
    private var progressDialog: ProgressDialog? = null
    private var stopAnimRunnable: Runnable? = null
    private var dismissRunnable: Runnable? = null
    private var mac: String? = ""
    private var name: String? = ""
    override fun vmFactory() = TreadFactory()
    override fun viewOpe() {
        super.viewOpe()


        if (Build.VERSION.SDK_INT >= 21) {
            val window = window
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.statusBarColor = Color.TRANSPARENT
        }
        if (intent.extras == null || intent.extras!!["params"] == null) {
            finish()
        }
        showProgressDialog()
        viewModel.getInstance().startScan()

//        viewModel.init(intent.extras!!["params"] as Device)
//        viewModel.connectDevice(this)

        mac = (intent.extras!!["params"] as Device).mac
        name = (intent.extras!!["params"] as Device).name
        preferencesUtility = PreferencesUtility.getInstance(this@TreadMillActivity)
        toolbar.setNavigationOnClickListener { finish() }
        click()
        setdata()


        LumanHelper.aboutActivityManager().push(this)
    }

    private val REQUEST_CODE_HEIGHT_Novice = 6
    private var novice = 1
    private var preferencesUtility: PreferencesUtility? = null

//    private fun Xins(number: Int) {
//        novice = preferencesUtility!!.getNovice()
//        if (novice == 0 && number == 0) {
//            val intent = Intent(this@ThreadMillActivity, ThreadMillNoviceGuideOne::class.java)
//            startActivityForResult(intent, REQUEST_CODE_HEIGHT_Novice)
//        } else if (novice == 1 && number == 1) {
//            viewModel.getInstance().setMaxSpeed(60)
//        }
//    }

    protected override fun onResume() {
        viewModel.getInstance().setOnOperationListener(bleListener);

        super.onResume()
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_HEIGHT_Novice) {
            isOneInToActivity=false
            Contuntime=0
            novice = preferencesUtility!!.getNovice()
            if (novice!=1){
                viewModel.getInstance().setMaxSpeed(30)//设置速度
            }
            displayStats=""

        } else if (requestCode == REQUEST_CODE_HEIGHT_SETTINGS) {
            setdata()
            isOneInToActivity=false
            Contuntime=0
            novice = preferencesUtility!!.getNovice()
            if (novice!=1){
                viewModel.getInstance().setMaxSpeed(30)//设置速度
            }
            displayStats=""
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    private val handler1 = object : Handler() {

        override fun handleMessage(msg: Message) {
//            Log.e("aaa", "{${msg.what}}")
            when (msg.what) {
//                1 -> {
//
//                    Log.e("aa","aaaaaaaaaaaaaaa")
//                }
                2 -> {
                    if (isRunning.equals("运行")) {
                        end.visibility = View.VISIBLE
                        liner_start.visibility = View.GONE
                    } else {
                        end.visibility = View.GONE
                        liner_start.visibility = View.VISIBLE
                    }

//                    Log.e("aa", "bbbbbbbbbbbbbbbb")

                }

                4 -> {
                    shoudong_speed.visibility = View.GONE
                    lin_zidong.visibility = View.VISIBLE
                }
                5 -> {
                    shoudong_speed.visibility = View.VISIBLE
                    lin_zidong.visibility = View.GONE
                }
                6 -> {
                    setTodayData(displayLength, displayTime, displayStep)

                    if (isyingzhi == 1) {
                        system.setText(getString(R.string.british_system))
                        zidong_speed.text =
                            getString(R.string.new_speed) + "  " + String.format(
                                "%.1f",
                                displaySpeed * 0.62137119
                            ) + "  "+getString(R.string.mph)
                        zidong_text.text =
                            getString(R.string.new_speed) + "  " + String.format(
                                "%.1f",
                                displaySpeed * 0.62137119
                            ) + "  "+getString(R.string.mph)
                    } else {
                        system.setText(getString(R.string.metric_system))
                        zidong_speed.text =
                            getString(R.string.new_speed) + "  " + displaySpeed + " "+getString(R.string.kmh)
                        zidong_text.text =
                            getString(R.string.new_speed) + "  " + displaySpeed + "  "+getString(R.string.kmh)
                    }



                }
                10 -> {
                    setdata()
                    preferencesUtility!!.system = isyingzhi
                    if (isyingzhi == 1) {
                        system.setText(getString(R.string.british_system))
                        zidong_speed.text =
                            getString(R.string.new_speed) + "  " + String.format(
                                "%.1f",
                                displaySpeed * 0.62137119
                            ) + "  "+getString(R.string.mph)
                        zidong_text.text =
                            getString(R.string.new_speed) + "  " + String.format(
                                "%.1f",
                                displaySpeed * 0.62137119
                            ) + "   "+getString(R.string.mph)
                    } else {
                        system.setText(getString(R.string.metric_system))
                        zidong_speed.text =
                            getString(R.string.new_speed) + "  " + displaySpeed + " "+getString(R.string.kmh)
                        zidong_text.text =
                            getString(R.string.new_speed) + "  " + displaySpeed + " "+getString(R.string.kmh)
                    }

                    if (displayLength > 1000) {
                        val tt = displayLength.toDouble() / 1000
                        if (isyingzhi == 1) {
                            tv_speed.text = String.format("%.2f", tt * 0.62137119) + ""
                            tv_speeddw.setText(R.string.mile)
                        } else {
                            tv_speed.text = tt.toString() + ""
                            tv_speeddw.setText(R.string.km)
                        }
                    } else {
                        if (isyingzhi == 1) {
                            tv_speed.text =
                                String.format("%.2f", 0.62137119 * displayLength.toDouble() / 1000) + ""
                            tv_speeddw.setText(R.string.mile)
                        } else {
                            tv_speed.text = displayLength.toString() + ""
                            tv_speeddw.setText(R.string.rice)
                        }
                    }




                }
                else -> {
                }
            }
        }

    }


    private var SpeedM1 = 0
    private var SpeedM2 = 0
    private var SpeedM3 = 0
    private var item: ArrayList<Double>? = null
    private var itemYin: ArrayList<Double>? = null
    private fun setdata() {
        if (item == null) {
            item = ArrayList<Double>()
            item!!.add(1.0)
            item!!.add(1.5)
            item!!.add(2.0)
            item!!.add(2.5)
            item!!.add(3.0)
            item!!.add(3.5)
            item!!.add(4.0)
            item!!.add(4.5)
            item!!.add(5.0)
            item!!.add(5.5)
            item!!.add(6.0)
        }
        if (itemYin == null) {
            itemYin = ArrayList<Double>()
            itemYin!!.add(0.6)
            itemYin!!.add(0.9)
            itemYin!!.add(1.2)
            itemYin!!.add(1.5)
            itemYin!!.add(1.8)
            itemYin!!.add(2.1)
            itemYin!!.add(2.4)
            itemYin!!.add(2.7)
            itemYin!!.add(3.1)
            itemYin!!.add(3.4)
            itemYin!!.add(3.7)
        }

        var a = preferencesUtility?.getM1()
        if (a!! >= 0) {
//            if (isyingzhi == 1) {
//
//                m1.setText(itemYin!!.get(a).toString() + getString(R.string.mph))
//
//            } else {
//                m1.setText(item!!.get(a).toString() + getString(R.string.kmh))
//            }
            SpeedM1 = (item!!.get(a) * 10).toString().toDouble().toInt()
        }
        val b = preferencesUtility?.getM2()
        if (b!! >= 0) {
//            if (isyingzhi == 1) {
//                m2.setText(itemYin!!.get(b).toString() + getString(R.string.mph))
//            } else {
//                m2.setText(item!!.get(b).toString() + getString(R.string.kmh))
//            }
            SpeedM2 = (item!!.get(b) * 10).toString().toDouble().toInt()
        }
        val c = preferencesUtility?.getM3()
        if (c!! >= 0) {
//            if (isyingzhi == 1) {
//                m3.setText(itemYin!!.get(c).toString() + getString(R.string.mph))
//            } else {
//                m3.setText(item!!.get(c).toString() + getString(R.string.kmh))
//            }
            SpeedM3 = (item!!.get(c) * 10).toString().toDouble().toInt()
        }
        if (isyingzhi == 1) {
            tv_speeddw.text = getString(R.string.mile)
        } else {
            tv_speeddw.text = getString(R.string.rice)
        }
    }

    private fun click() {
        system.setOnClickListener {
            if (isyingzhi == 0) {
                viewModel.getInstance().setkey(15)
            } else {
                viewModel.getInstance().setkey(16)

            }






        }

        //速度减
        walker_jian.setConstraintLayoutOnClickListener(object :
            BtnCy.ConstraintLayoutOnClickListener {
            override fun onLongPressh() {
            }

            override fun onUpspring() {
            }

            override fun onShortPressh() {
                m1.isSelected = false
                m2.isSelected = false
                m3.isSelected = false
                viewModel.getInstance().setkey(5)
            }

        })

        //速度加
        walker_add.setConstraintLayoutOnClickListener(object :
            BtnCy.ConstraintLayoutOnClickListener {
            override fun onLongPressh() {
            }

            override fun onUpspring() {
            }

            override fun onShortPressh() {
                m1.isSelected = false
                m2.isSelected = false
                m3.isSelected = false
                viewModel.getInstance().setkey(4)
            }

        })

        m1.setOnClickListener {
            m1.isSelected = true
            m2.isSelected = false
            m3.isSelected = false
            if (SpeedM1 != 0) {
                viewModel.getInstance().setFunctionSpeed(SpeedM1)//设置速度
            }

        }

        m2.setOnClickListener {
            m1.isSelected = false
            m2.isSelected = true
            m3.isSelected = false
            if (SpeedM2 != 0) {
                viewModel.getInstance().setFunctionSpeed(SpeedM2)//设置速度
            }
        }
        m3.setOnClickListener {
            m1.isSelected = false
            m2.isSelected = false
            m3.isSelected = true
            if (SpeedM3 != 0) {
                viewModel.getInstance().setFunctionSpeed(SpeedM3)//设置速度
            }
        }

        automatic.setOnClickListener {
            automatic.isSelected = true
            manual.isSelected = false
            shoudong_speed.visibility = View.GONE
            lin_zidong.visibility = View.VISIBLE
            viewModel.getInstance().setkey(2)
        }
        manual.setOnClickListener {
            automatic.isSelected = false
            manual.isSelected = true
            shoudong_speed.visibility = View.VISIBLE
            lin_zidong.visibility = View.GONE
            viewModel.getInstance().setkey(1)
        }
        liner_start.setOnClickListener {
            if (isRunning == "停止") {
                viewModel.getInstance().setkey(6)
            }
        }
        image_start.setOnClickListener {
            if (isRunning == "停止") {
                viewModel.getInstance().setkey(6)
            }
        }


        end.setOnClickListener {
            if (isRunning == "运行") {
                viewModel.getInstance().setkey(6)
            }

        }
        end_image.setOnClickListener {
            if (isRunning == "运行") {
                viewModel.getInstance().setkey(6)
            }
        }
        ll_set.setOnClickListener {
            val intent = Intent(this@TreadMillActivity, TreadSettingActivity::class.java)
            intent.putExtra("unit", unitflag)
            intent.putExtra("run",isRunning)
                    intent.putExtra("isyingzhi",isyingzhi)
            startActivityForResult(intent, REQUEST_CODE_HEIGHT_SETTINGS)
        }

        xinshou.setOnClickListener {

            val intent = Intent(this@TreadMillActivity, TreadMillNoviceGuideOne::class.java)
            startActivityForResult(intent, REQUEST_CODE_HEIGHT_Novice)


        }
        xinshou_two.setOnClickListener {

            val intent = Intent(this@TreadMillActivity, TreadMillNoviceGuideOne::class.java)
            startActivityForResult(intent, REQUEST_CODE_HEIGHT_Novice)
        }
    }

    private val REQUEST_CODE_HEIGHT_SETTINGS = 10
    private var displaySpeed = 0.0//实时速度
    private var displayLength = 0//实时距离
    private var displayTime = 0//实时时间
    private var displayStep = 0//实时步数
    private var displayStats = ""
    private var isOne = true

    private fun moshi() {
        if (displayStats.equals("自动")) {
            automatic.isSelected = true
            manual.isSelected = false
            var message = Message()
            message.what = 4;
            handler1.sendMessage(message);

        } else if (displayStats.equals("手动")) {
            automatic.isSelected = false
            manual.isSelected = true
            var message = Message()
            message.what = 5;
            handler1.sendMessage(message);
        }

    }


    private var isRunning = ""

    private fun running(run: String) {
        if (!run.equals(isRunning)) {

            isRunning = run
            var message = Message()
            message.what = 2;
            handler1.sendMessage(message);

        }
    }

    private var MaxLength = 0
    private fun setTodayData(length: Int, time: Int, step: Int) {

        foot.text = step.toString() + ""
        target.setText(FormatMiss(time))
        if (MaxLength != length) {
            MaxLength = length
            val b = 70 * length
            if (length > 1000) {
                val tt = length.toDouble() / 1000
                if (isyingzhi == 1) {
                    tv_speed.text = String.format("%.2f", tt * 0.62137119) + ""
                    tv_speeddw.setText(R.string.mile)
                } else {
                    tv_speed.text = tt.toString() + ""
                    tv_speeddw.setText(R.string.km)
                }

            } else {
                if (isyingzhi == 1) {
                    tv_speed.text =
                        String.format("%.2f", 0.62137119 * length.toDouble() / 1000) + ""
                    tv_speeddw.setText(R.string.mile)
                } else {
                    tv_speed.text = length.toString() + ""
                    tv_speeddw.setText(R.string.rice)
                }
            }

            if (b > 1000) {
                val ttt = (length * 70).toDouble() / 1000
                tv_cal.text = ttt.toString() + ""
                tv_caldw.setText(R.string.kcal)
            } else {
                tv_cal.text = b.toString() + ""
                tv_caldw.setText(R.string.cal)
            }
        }

    }

    private fun FormatMiss(miss: Int): String {
        val hh = if (miss / 3600 > 9) (miss / 3600).toString() + "" else "0" + miss / 3600
        val mm =
            if (miss % 3600 / 60 > 9) (miss % 3600 / 60).toString() + "" else "0" + miss % 3600 / 60
        val ss =
            if (miss % 3600 % 60 > 9) (miss % 3600 % 60).toString() + "" else "0" + miss % 3600 % 60
        return "$hh:$mm:$ss"
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
//                    finish()
//                    viewModel.destroy()
                    showDisconnectWindow()
                }
            }
            handler.postDelayed(dismissRunnable, 15000)
        }
    }

    private fun showDisconnectWindow() {
        val dialog = DisconnectedDialog()
        dialog.setCallback(object : DisconnectedDialog.Callback {
            override fun sure() {
                finish()
            }
        })
//                viewModel.showToast(getString(R.string.dis_connect_device))
//                finish()
        dialog.show(supportFragmentManager, "disconnect")
    }

    private fun showDisconnectServiceWindow() {
        val dialog = DisconnectedTwoDialog()
        dialog.setCallback(object : DisconnectedTwoDialog.Callback {
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

    override fun onDestroy() {
        super.onDestroy()
        dismissProgressDialog()
//        viewModel.destroy()
        if (stopAnimRunnable != null) {
            handler.removeCallbacks(stopAnimRunnable)
            stopAnimRunnable = null
        }
        if (dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable)
            dismissRunnable = null
        }
        viewModel.getInstance().disconnect()
    }

    var connectNum = 0;
    override fun observeVM() {
        super.observeVM()
//        viewModel.connectState.observe(this, Observer {
//            dismissProgressDialog()
//            if (!it) {
//                if (connectNum<2){
//                    connectNum++
//                    viewModel.connectDevice(this)
////                    showProgressDialog()
//                }else {
//                    showDisconnectWindow()
//                }
//            }else{
//                connectNum=0;
//            }
//        })
    }

    override fun layoutId(): Int {
        return R.layout.activity_thread
    }
}
