package com.luman.core

import android.app.Application
import com.luman.core.manager.ActivityStackManager
import com.luman.core.util.ContextUtil
import com.luman.core.util.SupportFunc
import com.luman.core.util.ToastUtil

/**
 * 帮助工具开放类
 * @Editor luman
 * @Time 2019-10-28 17:27
 **/
object LumanHelper {

    //Toast管理器
    fun aboutToast() = ToastUtil

    //活动栈管理器
    fun aboutActivityManager() = ActivityStackManager

    //帮助方法集
    fun aboutFunc() = SupportFunc

    /**
     * 初始化工具组件
     */
    fun init(application: Application) {
        ContextUtil.initApp(application)
    }

    //获取Application对象
    fun getApplicationContext(): Application? = ContextUtil.getApplication()

    //根据资源id获取字符串
    fun getStringForPkg(id: Int): String {
        if (ContextUtil.getApplication() != null) {
            return ContextUtil.getApplication()!!.getString(id)
        } else {
            return ""
        }
    }

    /**
     * 清除相关占用
     */
    fun clear() {
//        ContextUtil.clear()
//        ToastUtil.clear()
//        HttpManager.clear()
    }

}