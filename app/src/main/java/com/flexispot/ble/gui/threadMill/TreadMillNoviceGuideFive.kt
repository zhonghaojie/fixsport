package com.flexispot.ble.gui.threadMill;


import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.clj.fastble.data.BleDevice
import com.flexispot.ble.R
import com.flexispot.ble.gui.threadMill.PreferencesUtility
import com.flexispot.ble.gui.view.StatusBarUtil


import com.walking.secretary.confignetwork.TreadMillModel
import com.luman.core.LumanHelper



class TreadMillNoviceGuideFive : AppCompatActivity(), TreadMillModel.OnOperationListener {
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

    private var btn_next: Button? = null

    private var preferencesUtility: PreferencesUtility? = null

    private var a = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tread_novice_five)
        StatusBarUtil.setStatusBarMode(this, true, R.color.white)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setNavigationOnClickListener { finish() }

        val ac =
            LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setOnOperationListener(this)
        btn_next = findViewById(R.id.btn_next)
        btn_next!!.setOnClickListener {
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setMaxSpeed(60)
            finish()
        }
        preferencesUtility = PreferencesUtility.getInstance(this@TreadMillNoviceGuideFive)
        preferencesUtility!!.novice = 1
    }


    override fun onDestroy() {

        super.onDestroy()
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
            ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setShield(0, 0, 0, 0)
        }
    }
}