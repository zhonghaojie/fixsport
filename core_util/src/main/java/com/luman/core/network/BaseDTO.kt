package com.luman.core.network

/**
 * @Editor luman
 * @Time 2019-10-31 13:40
 * @param code 错误码
 * @param msg 错误信息
 * @param data 返回内容主体
 **/
class BaseDTO<T>(val code: Int, val msg: String?, val data: T?)