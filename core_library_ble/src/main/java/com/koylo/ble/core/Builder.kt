package com.koylo.ble.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.koylo.ble.flexispot.flexispotUUID

/**
 * 蓝牙工具建造者类，负责统筹配置参数
 */
class Builder {

    /**
     * 是否自动处理蓝牙权限获取结果
     */
    private var autoDealBluetoothPermission: Boolean = true

    fun autoDealBluetoothPermission() = autoDealBluetoothPermission
    /**
     * 绑定Activity
     */
    private var context: FragmentActivity? = null
    /**
     * 绑定fragment
     */
    private var fragment: Fragment? = null

    fun context() = context
    fun fragment() = fragment
    fun clearContext() {
        context = null
    }

    /**
     * 是否只是单次数据收发（true则接收到数据后即断开，开启此模式需保证发送数据有响应）
     */
    private var isOpeOnce: Boolean = false

    fun isOpeOnce() = isOpeOnce
    /**
     * 是否有日志输出
     */
    private var hasLog: Boolean = true

    fun hasLog() = hasLog
    /**
     * 统一回调
     */
    private var callback: Callback? = null

    fun callback() = callback
    /**
     * 扫描时长，单位秒
     */
    private var scanPeriod: Long = 10000L

    fun scanPeriod() = scanPeriod

    /**
     * 要搜索的设备名
     */
    private var filterName: String? = null

    /**
     * 要过滤的设备名
     */
    fun filterName() = filterName

    /**
     * uuid
     */
    private var uuids: UuidDto = UuidDto(
        flexispotUUID.SERVICE_UUID,
        flexispotUUID.flexispot_MEASUREMENT_POINT,
        flexispotUUID.flexispot_CONTROL_POINT
    )

    fun uuids() = uuids

    /**
     * 设置是否自动处理蓝牙获取结果，默认true
     */
    fun autoDealBlootoothPermission(value: Boolean): Builder {
        autoDealBluetoothPermission = value
        return this

    }

    /*
     * 设置是否单次数据操作，默认false
     */
    fun opeOnce(value: Boolean): Builder {
        isOpeOnce = value
        return this
    }

    /**
     * 绑定activity
     */
    fun activity(value: FragmentActivity?): Builder {
        context = value
        return this
    }

    /**
     * 绑定Fragment
     */
    fun fragment(value: Fragment?): Builder {
        context = value?.activity
        fragment = value
        return this
    }

    /**
     * 是否有日志，默认true
     */
    fun hasLog(value: Boolean): Builder {
        hasLog = value
        return this
    }

    /**
     * 回调设置
     */
    fun callback(value: Callback): Builder {
        callback = value
        return this
    }

    /**
     * 扫描周期，单位毫秒 默认10 * 1000毫秒
     */
    fun scanPeriod(value: Long): Builder {
        scanPeriod = value
        return this
    }

    /**
     * 设置uuid
     */
    fun setUuid(uuidDto: UuidDto): Builder {
        this.uuids = uuids
        return this
    }

    /**
     * 设置过滤名
     */
    fun setName(name: String?): Builder {
        if (name != null) {
            this.filterName = name
        }
        return this
    }


    /**
     * 构建蓝牙工具对象
     */
    fun create(): BluetoothTool {
        if (context == null) {
            throw IllegalStateException("fragment/activity required.")
        }
        return BluetoothTool(this)
    }

}