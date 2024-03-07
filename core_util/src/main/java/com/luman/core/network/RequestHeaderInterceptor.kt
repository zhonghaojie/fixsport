package com.luman.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * @Editor luman
 * @Time 2019-11-06 10:32
 **/
class RequestHeaderInterceptor : Interceptor {

    private val headers = HashMap<String, String>()

    fun addHeader(key: String, value: String) {
        headers[key] = value
    }

    fun clear() {
        headers.clear()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val newBuilder = chain.request().newBuilder()
        for ((key, value) in headers) {
            newBuilder.addHeader(key, value)
        }
        return chain.proceed(newBuilder.build())
    }

}