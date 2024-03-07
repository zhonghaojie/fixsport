package com.luman.core

/**
 * 配置项
 */
object ConfigParam {

    //Bugly开关
    const val Bugly_Switch = true
    const val Bugly_APPID = "20427a70ac"
    //Multidex开关
    const val Multidex_Switch = false
    //日志标签
    const val LOG_TAG = "LuMan"

    //缓存区大小
    const val CACHE_SIZE = 10 * 1024 * 1024L
    //http请求连接前缀
    const val HTTP_HEAD = "https://192.168.1.1/rest/"
    //图片的固定前缀
    const val IMAGE_HEAD = ""
    //请求成功的状态码
    const val SUCCESS_CODE = 0
    const val TIME_OUT = 2000L
}