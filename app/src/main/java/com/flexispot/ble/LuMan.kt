
package com.flexispot.ble
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.luman.core.ConfigParam
import com.luman.core.LumanHelper
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.tencent.bugly.crashreport.CrashReport
import com.uuzuche.lib_zxing.activity.ZXingLibrary
import org.litepal.LitePal
import kotlin.properties.Delegates

import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.multidex.BuildConfig
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.clj.fastble.BleManager
import com.luman.core.util.ContextUtil.getApplication


/**
 * Application基类
 */
class LuMan : Application() {

    companion object {
        lateinit var instance: Application
        var context: Context by Delegates.notNull()
    }

    init {
        instance = this
        LumanHelper.init(instance)
    }

    override fun onCreate() {
        super.onCreate()
        initConfig()
        context = applicationContext
        BleManager.getInstance().init(this)


    }

    private fun initConfig() {
        //数据库初始化
        LitePal.initialize(this)
        //扫码初始化
        ZXingLibrary.initDisplayOpinion(this)
        //Bugly日志处理
        if (ConfigParam.Bugly_Switch) {
            CrashReport.initCrashReport(instance, ConfigParam.Bugly_APPID, BuildConfig.DEBUG)
        }
        //Multidex分包处理
        if (ConfigParam.Multidex_Switch) {
            MultiDex.install(this)
        }
        //收集错误信息
//



        //日志初始化
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)
            .methodCount(0)
            .tag(ConfigParam.LOG_TAG)
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

}