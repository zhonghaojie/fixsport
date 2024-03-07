package com.flexispot.ble.gui.threadMill;


import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.clj.fastble.data.BleDevice
import com.flexispot.ble.R
import com.flexispot.ble.gui.devices.WebViewActivity
import com.flexispot.ble.gui.threadMill.ArtDailog
import com.flexispot.ble.gui.threadMill.PreferencesUtility
import com.flexispot.ble.gui.threadMill.WalkerSettingTargetActivity
import com.flexispot.ble.gui.view.ActionSheetDialog
import com.flexispot.ble.gui.view.StatusBarUtil
import com.kaopiz.kprogresshud.KProgressHUD
import com.luman.core.LumanHelper

import com.walking.secretary.confignetwork.TreadMillModel


class TreadSettingActivity : AppCompatActivity(),TreadMillModel.OnOperationListener  {

    private var toolbar: Toolbar? = null

    private var guide_to_use: RelativeLayout? = null//新手引导
    private var preset_speed: RelativeLayout? = null//预设速度
    private var standby_time: RelativeLayout? = null//设置待机时间
    private var initialization: RelativeLayout? = null//初始化
    private var explain: RelativeLayout? = null//使用说明
    private var company: RelativeLayout? = null//单位切换
    private var privacy: RelativeLayout? = null//隐私政策
    private var tv_unit:TextView?=null


    private var tv_standby: TextView? = null//显示待机时间
    var isyingzhi=0;
    private var preferencesUtility: PreferencesUtility? = null
    var unit="1";//0无自动 英制,1有自动 公制
    var progressHUD: KProgressHUD? = null
    internal var mToast: Toast? = null

    private var mSureDeleteDialog: ArtDailog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_walker)
        findId()
        StatusBarUtil.setStatusBarMode(this, true, R.color.bg_color);
        preferencesUtility = PreferencesUtility.getInstance(this@TreadSettingActivity)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setNavigationOnClickListener { finish() }
        var name = (intent.extras!!["unit"] )
        isRunning=(intent.extras!!["isRunning"]).toString()
        isyingzhi=(intent.extras!!["isyingzhi"] as Int)
        tv_unit=findViewById(R.id.tv_unit)
        if (isyingzhi==0){
            tv_unit!!.setText(R.string.metric_system)
        }else{
            tv_unit!!.setText(R.string.british_system)
        }

        progressHUD = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel(resources.getString(R.string.loading))
            .setDetailsLabel(resources.getString(R.string.one_moment_please))
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        if ("0".equals(name)){
            guide_to_use!!.visibility=View.GONE
            unit=""+name;
        }

        val time = preferencesUtility!!.standby
        if (time == 30) {//30
            tv_standby!!.text = "30s"
        } else if (time == 60) {//60
            tv_standby!!.text = "60s"
        } else if (time == 120) {//120秒
            tv_standby!!.text = "120s"
        }




    }

    open fun showLoading() {
        if (!progressHUD?.isShowing!!) {
            progressHUD?.show()
        }
    }

    open fun dismissLoading() {
        if (progressHUD?.isShowing!!) {
            progressHUD?.dismiss()
        }
    }


    private var isRunning = ""



    private fun findId() {
        guide_to_use = findViewById(R.id.guide_to_use)
        preset_speed = findViewById(R.id.preset_speed)
        standby_time = findViewById(R.id.standby_time)
        tv_standby = findViewById(R.id.tv_standby)
        initialization=findViewById(R.id.initialization)
        company=findViewById(R.id.company)
        explain=findViewById(R.id.explain)
        privacy=findViewById(R.id.privacy)

        standby_time!!.setOnClickListener { Tan() }

        preset_speed!!.setOnClickListener {
            val intent = Intent(this@TreadSettingActivity, WalkerSettingTargetActivity::class.java)
            startActivity(intent)
        }

        guide_to_use!!.setOnClickListener {
            val intent = Intent(this@TreadSettingActivity, TreadMillNoviceGuideOne::class.java)
            startActivityForResult(intent, REQUEST_CODE_HEIGHT_Novice)
        }
        company!!.setOnClickListener {
            val intent = Intent(this@TreadSettingActivity, TreadMillCompany::class.java)
            intent.putExtra("isyingzhi",isyingzhi)
            startActivityForResult(intent, REQUEST_CODE_HEIGHT_Compang)

        }

        explain!!.setOnClickListener {
            val intent = Intent(this@TreadSettingActivity, TreadMillWeb::class.java)
            startActivity(intent)

        }
        privacy!!.setOnClickListener {
            val intent = Intent(this@TreadSettingActivity, WebViewActivity::class.java)
            startActivity(intent)

        }

        val ac =
            LumanHelper.aboutActivityManager()
                .findAc(TreadMillActivity::class.java.name)
        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance()
            .setOnOperationListener(this)
        initialization!!.setOnClickListener{
            if (mSureDeleteDialog == null) {
                mSureDeleteDialog =
                    ArtDailog(this@TreadSettingActivity)
            }

            mSureDeleteDialog!!.show()

            mSureDeleteDialog!!.setLeftButton({
                mSureDeleteDialog!!.dismiss()
            })

            mSureDeleteDialog!!.setRightButton({
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance()
                    .setMaxSpeed(30)
                mSureDeleteDialog!!.dismiss()
                showLoading()
                if (isRunning == "运行") {
                    ViewModelProviders.of(ac!!).get(TreadMillModel::class.java)
                        .getInstance().setkey(6)
                }
                Handler().postDelayed({
                    ViewModelProviders.of(ac!!).get(TreadMillModel::class.java)
                        .getInstance().setkey(1)
                }, 1000)
                Handler().postDelayed({
                    if (unit == "0") {
                        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java)
                            .getInstance().setkey(15)
                        tv_unit!!.setText(R.string.british_system)
                        preferencesUtility!!.system=1
                    } else {
                        tv_unit!!.setText(R.string.metric_system)
                        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java)
                            .getInstance().setkey(16)
                        preferencesUtility!!.system=0
                    }
                    dismissLoading()
                }, 2000)
            })
        }

    }



    private val REQUEST_CODE_HEIGHT_Novice = 6
    private val REQUEST_CODE_HEIGHT_Compang = 7

    private var novice = 1
    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_HEIGHT_Novice) {
            val ac =
                LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setOnOperationListener(this)
            novice = preferencesUtility!!.getNovice()
            if (novice!=1){
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setMaxSpeed(30)//设置速度
            }

        }else if(requestCode == REQUEST_CODE_HEIGHT_Compang&& resultCode == RESULT_OK){
            val ac =
                LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setOnOperationListener(this)
            val position: Int = data!!.getIntExtra("isyingzhi",0);
            isyingzhi=position
            if (position==0){
                tv_unit!!.setText(R.string.metric_system)
            }else{
                tv_unit!!.setText(R.string.british_system)
            }

        }
        super.onActivityResult(requestCode, resultCode, data)

    }



    private fun Tan() {
        val ac =
            LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
        ActionSheetDialog(this@TreadSettingActivity)
            .builder()
            .setCancelable(false)
            .setCanceledOnTouchOutside(false)
            .addSheet2(ActionSheetDialog.SheetItemColor.Blue) { which ->
                if (which == 1) {//30
                    preferencesUtility!!.standby = 30
                    ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setDormancy(60, 0)
                    tv_standby!!.text = "30s"
                } else if (which == 2) {//60
                    preferencesUtility!!.standby = 60
                    ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setDormancy(120, 0)
                    tv_standby!!.text = "60s"

                } else if (which == 3) {//120秒
                    preferencesUtility!!.standby = 120
                    ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setDormancy(240, 0)
                    tv_standby!!.text = "120s"

                }
            }.show()

    }
    protected override fun onResume() {


        super.onResume()
    }

    override fun onDestroy() {

        super.onDestroy()
    }

    private fun showToast(msg: String) {
        if (mToast == null) {
            mToast = Toast.makeText(this@TreadSettingActivity, msg, Toast.LENGTH_SHORT)
        } else {
            mToast!!.setText(msg)
        }
        mToast!!.show()
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

    override fun onGetScanDevice(scanResult: BleDevice) {
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

        if (!run.equals(isRunning)) {
            isRunning = run
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

}


