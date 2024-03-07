package com.flexispot.ble.gui.threadMill;


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.clj.fastble.data.BleDevice
import com.flexispot.ble.R
import com.flexispot.ble.gui.threadMill.PreferencesUtility
import com.flexispot.ble.gui.view.StatusBarUtil
import com.kaopiz.kprogresshud.KProgressHUD
import com.luman.core.LumanHelper

import com.walking.secretary.confignetwork.TreadMillModel


class TreadMillCompany : AppCompatActivity(), TreadMillModel.OnOperationListener {
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

    private var toolbar: Toolbar? = null

    private var metric: ImageView? = null
    private var british: ImageView? = null
    private var rel_metric:RelativeLayout?=null
    private var rel_british:RelativeLayout?=null

    var progressHUD: KProgressHUD? = null
    private var preferencesUtility: PreferencesUtility? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_unit)
        StatusBarUtil.setStatusBarMode(this, true, R.color.bg_color)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setNavigationOnClickListener {

            val intent: Intent = intent
            intent.putExtra("isyingzhi", isyingzhi)
            setResult(Activity.RESULT_OK, intent)
            finish()


        }
        metric = findViewById(R.id.metric)
        british = findViewById(R.id.british)
        preferencesUtility = PreferencesUtility.getInstance(this@TreadMillCompany)
        progressHUD = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel(resources.getString(R.string.loading))
            .setDetailsLabel(resources.getString(R.string.one_moment_please))
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        val ac =
            LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setOnOperationListener(this)

        metric!!.setOnClickListener{
            if (isyingzhi==1){
                showLoading()
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setkey(16)
            }
        }
        british!!.setOnClickListener{
            if (isyingzhi==0){
                showLoading()
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setkey(15)
            }
        }
        isyingzhi=(intent.extras!!["isyingzhi"] as Int)
        if (isyingzhi==0){
            metric!!.setImageResource(R.mipmap.icon_xuanzhong)
        }else{
            british!!.setImageResource(R.mipmap.icon_xuanzhong)
        }

        rel_metric=findViewById(R.id.rel_metric);
        rel_british=findViewById(R.id.rel_british);

        rel_metric!!.setOnClickListener{
            if (isyingzhi==1){
                showLoading()
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setkey(16)
            }
        }
        rel_british!!.setOnClickListener{
            if (isyingzhi==0){
                showLoading()
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setkey(15)
            }
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            val intent: Intent = intent
            intent.putExtra("isyingzhi", isyingzhi)
            setResult(Activity.RESULT_OK, intent)
            finish()

        }
        return super.onKeyDown(keyCode, event)
    }


    var isyingzhi = 0    //0gonzhi
    override fun onDestroy() {

        super.onDestroy()
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

        if (isyingzhi != isYingZhi) {
            isyingzhi = isYingZhi
            dismissLoading()
          if (isyingzhi==0){
              metric!!.setImageResource(R.mipmap.icon_xuanzhong)
              british!!.setImageResource(R.mipmap.icon_weixuanzhong)
              preferencesUtility!!.system=0
          }else{
              metric!!.setImageResource(R.mipmap.icon_weixuanzhong)
              british!!.setImageResource(R.mipmap.icon_xuanzhong)
              preferencesUtility!!.system=1
          }
        }



    }
}