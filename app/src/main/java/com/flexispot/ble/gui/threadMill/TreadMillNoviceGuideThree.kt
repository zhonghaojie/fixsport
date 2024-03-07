package com.flexispot.ble.gui.threadMill;

import android.content.Intent

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.clj.fastble.data.BleDevice
import com.flexispot.ble.R
import com.flexispot.ble.gui.view.StatusBarUtil


import com.walking.secretary.confignetwork.TreadMillModel
import com.luman.core.LumanHelper


class TreadMillNoviceGuideThree : AppCompatActivity(), TreadMillModel.OnOperationListener {
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


    private var a = 0

    internal var mToast: Toast? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tread_novice_three)
        StatusBarUtil.setStatusBarMode(this, true, R.color.white)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setNavigationOnClickListener { finish() }

        val ac =
            LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setOnOperationListener(this)
        val image = findViewById<ImageView>(R.id.image)
        val image2=findViewById<ImageView>(R.id.image_two)
        var able = resources.configuration.locale.country
        if (able.equals("CN")) {
            image.visibility= View.VISIBLE
            image2.visibility= View.GONE
        }else{
            image.visibility= View.GONE
            image2.visibility= View.VISIBLE
        }

    }

    override fun onDestroy() {

        super.onDestroy()
    }

    private fun showToast(msg: String) {
        if (mToast == null) {
            mToast = Toast.makeText(this@TreadMillNoviceGuideThree, msg, Toast.LENGTH_SHORT)
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
        a = 0
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
        if (a == 2) {
            val ac =
                LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setShield(1, 1, 1, 0)
        }
        //            System.out.println(run+"pppppppppppppppppppp");
        if ("自动" == stats) {
            //跳转
            val intent =
                Intent(this@TreadMillNoviceGuideThree, TreadMillNoviceGuideFour::class.java)
            startActivity(intent)
            finish()
        }

    }
}