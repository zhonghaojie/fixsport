package com.flexispot.ble.gui.threadMill;

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.clj.fastble.data.BleDevice
import com.flexispot.ble.R
import com.flexispot.ble.gui.threadMill.TreadMillImageAdapter
import com.flexispot.ble.gui.view.StatusBarUtil
import com.luman.core.LumanHelper

import com.walking.secretary.confignetwork.TreadMillModel
import com.walking.secretary.confignetwork.TreadMillModel.OnOperationListener
import java.util.*

class TreadMillNoviceGuideOne : AppCompatActivity(),
    OnOperationListener {
    private var toolbar: Toolbar? = null
    private var vp: ViewPager? = null
    private val viewGroup: ViewGroup? = null
    private var btn_next: Button? = null
    private var adapter: TreadMillImageAdapter? = null
    private var images: MutableList<Int>? = null
    private var isRunning = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tread_novice_one)
        able = resources.configuration.locale.country
        StatusBarUtil.setStatusBarMode(this, true, R.color.white)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setNavigationOnClickListener(View.OnClickListener { finish() })
        find()
        setData()
        val ac =
            LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
        ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance()
            .setOnOperationListener(this)
    }

    var able = "CN"
    private var id = 0
    private fun find() {
        vp = findViewById(R.id.vp)
        btn_next = findViewById(R.id.btn_next)
        group =
            findViewById<View>(R.id.viewGroup) as ViewGroup
        images = ArrayList()
        if (able == "CN") {
            images!!.add(R.mipmap.pic_one)
            images!!.add(R.mipmap.pic_two)
        } else {
            images!!.add(R.mipmap.pic_one_e)
            images!!.add(R.mipmap.pic_two_e)
        }
        adapter = TreadMillImageAdapter(this@TreadMillNoviceGuideOne, images)
        //绑定监听事件
        vp!!.setOnPageChangeListener(GuidePageChangeListener())
        vp!!.setAdapter(adapter)
        vp!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                id = position
                if (position == 0) {
                    btn_next!!.setText(getString(R.string.start))
                } else {
                    btn_next!!.setText(getString(R.string.wancheng))
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        btn_next!!.setOnClickListener(View.OnClickListener {
            if (id == 0) {
                vp!!.setCurrentItem(1)
            } else {
                //跳转
                val intent = Intent(
                    this@TreadMillNoviceGuideOne,
                    TreadMillNoviceGuideTwo::class.java
                )
                startActivity(intent)
                finish()
            }
        })
    }

    private lateinit var imageViews: Array<ImageView?>

    //包裹点点的LinearLayout
    private var group: ViewGroup? = null
    private var imageView: ImageView? = null
    private fun setData() {
        //有多少张图就有多少个点点
        imageViews = arrayOfNulls(2)
        for (i in 0..1) {
            imageView = ImageView(this@TreadMillNoviceGuideOne)
            val params =
                LinearLayout.LayoutParams(30, 30)
            params.setMargins(15, 0, 20, 0)
            imageView!!.layoutParams = params
            imageViews[i] = imageView

            //默认第一张图显示为选中状态
            if (i == 0) {
                imageViews[i]!!.setBackgroundResource(R.mipmap.ico_round_sel)
            } else {
                imageViews[i]!!.setBackgroundResource(R.mipmap.ico_round)
            }
            group!!.addView(imageViews[i])
        }
    }



    override fun onScanFailure() {}
    override fun onStartConnect() {}
    override fun onConnectSuccess() {}
    override fun onConnectFail() {}
    override fun onConnectFailService() {}
    override fun onGetDeviceInfo(maxheight: Int, minheight: Int, unit: Int) {}
    override fun onGetScanDevice(scanResult: BleDevice) {}
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
            if (run == "运行") {
                val ac =
                    LumanHelper.aboutActivityManager().findAc(TreadMillActivity::class.java.name)
                ViewModelProviders.of(ac!!).get(TreadMillModel::class.java).getInstance().setkey(6)
            }
            println("ppppppp123132123"+run)
        }



    }

    override fun onMaxSpeed(speed: Double) {}
    override fun onFixed(length: Int, time: Int) {}
    override fun onRecord(
        recordTime: String,
        length: Int,
        time: Int,
        step: Int,
        num: Int
    ) {
    }

    override fun onTreadTime(time: String, number: Int) {}
    override fun process(now: Int, max: Int) {}
    override fun clean(a: Int, b: Int) {}

    //pageView监听器
    internal inner class GuidePageChangeListener : OnPageChangeListener {
        override fun onPageScrollStateChanged(arg0: Int) {
            // TODO Auto-generated method stub
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            // TODO Auto-generated method stub
        }

        //如果切换了，就把当前的点点设置为选中背景，其他设置未选中背景
        override fun onPageSelected(arg0: Int) {
            // TODO Auto-generated method stub
            for (i in imageViews.indices) {
                imageViews[arg0]!!.setBackgroundResource(R.mipmap.ico_round_sel)
                if (arg0 != i) {
                    imageViews[i]!!.setBackgroundResource(R.mipmap.ico_round)
                }
            }
        }
    }
}