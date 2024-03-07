package com.flexispot.ble.data.bean

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.io.Serializable

/**
 * 设备信息
 * @param name 设备名
 * @param mac 设备mac地址
 * @param type 设备类型
 * @param secondType 二级类型，翻转蓝牙桌1
 */
class Device(var name: String, val mac: String, val type: Int,var nickname:String) : LitePalSupport(), Serializable {
    var secondType: Int = 0
    @Column(ignore = true)
    var selected: Boolean = false
}
