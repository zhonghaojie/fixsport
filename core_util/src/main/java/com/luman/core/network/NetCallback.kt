package com.luman.core.network

/**
 * @Editor luman
 * @Time 2019-11-06 11:21
 **/
interface NetCallback<T> {

    fun success(data: T?)

    fun fail(code: Int, msg: String)

}

const val REQUEST_ERROR_CODE = -13412