package com.luman.core

import com.luman.core.ConfigParam.CACHE_SIZE
import com.luman.core.network.BaseDTO
import com.luman.core.network.NetCallback
import com.luman.core.network.REQUEST_ERROR_CODE
import com.luman.core.network.RequestHeaderInterceptor
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine

/**
 * 网络请求帮助类
 * @Editor luman
 * @Time 2019-10-31 13:47
 **/
object HttpManager {

    private var okHttpClient: OkHttpClient? = null
    private val headerInterceptor: RequestHeaderInterceptor =
        RequestHeaderInterceptor()

    private fun buildHttpLogging(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Logger.d(ConfigParam.LOG_TAG, message)
            }
        })
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    private fun buildHttpClient(): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(headerInterceptor)
        if (BuildConfig.DEBUG) {
            httpClientBuilder.addInterceptor(buildHttpLogging())
        }
        if (LumanHelper.getApplicationContext() != null) {
            //配置缓存
            val cacheFile =
                LumanHelper.getApplicationContext()!!.getExternalFilesDir("${LumanHelper.getApplicationContext()!!.packageName}_cache")
            val cacheSize = CACHE_SIZE
            httpClientBuilder.cache(Cache(cacheFile!!, cacheSize))
        }

        okHttpClient = httpClientBuilder
            .callTimeout(ConfigParam.TIME_OUT, TimeUnit.MILLISECONDS)
            .build()
        return okHttpClient!!
    }

    private var retrofit = Retrofit.Builder()
        .baseUrl(ConfigParam.HTTP_HEAD)
        .client(buildHttpClient())
        .build()

    suspend fun <T> Call<BaseDTO<T>>.await(callback: NetCallback<T>) {
        withContext(Dispatchers.IO) {
            suspendCoroutine<T> {
                enqueue(object : Callback<BaseDTO<T>> {
                    override fun onFailure(call: Call<BaseDTO<T>>, t: Throwable) {
                        callback.fail(
                            REQUEST_ERROR_CODE,
                            LumanHelper.getStringForPkg(R.string.request_fail)
                        )
                    }

                    override fun onResponse(
                        call: Call<BaseDTO<T>>,
                        response: Response<BaseDTO<T>>
                    ) {
                        val body = response.body()
                        when {
                            body == null -> callback.fail(
                                REQUEST_ERROR_CODE,
                                LumanHelper.getStringForPkg(R.string.empty_response)
                            )
                            body.code != ConfigParam.SUCCESS_CODE -> callback.fail(
                                body.code,
                                body.msg ?: LumanHelper.getStringForPkg(R.string.request_fail)
                            )
                            else -> callback.success(body.data)
                        }
                    }
                })
            }
        }
    }

    fun <T> createApi(tClass: Class<T>): T = retrofit.create(tClass)

    //往http的header添加键值对
    fun addHeader(key: String, value: String) {
        headerInterceptor.addHeader(key, value)
    }

    //清除协议头(退出登陆等场景可用)
    fun clearHeader() {
        headerInterceptor.clear()
    }

    fun clear() {
        clearHeader()
        okHttpClient = null
    }
}