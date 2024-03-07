package com.luman.core.util

import android.app.Application

/**
 * @Editor luman
 * @Time 2019-10-28 17:00
 **/
object ContextUtil {

    private var application: Application? = null

    fun initApp(application: Application) {
        ContextUtil.application = application
    }

    fun getApplication() = application

    fun clear() {
        application = null
    }
}