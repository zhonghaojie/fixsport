package com.flexispot.ble

import com.koylo.ble.flexispot.DeviceType

/**
 * @author luman
 * @date 19-11-26
 **/
object LabelRelation {

    /**
     * 根据类型获取表签名
     */
    fun getLabelByType(type: Int): Int {
        val label: Int
        when (type) {
            DeviceType.RACK.type -> {
                label = R.string.rack
            }
            DeviceType.MEDIA.type -> {
                label = R.string.media
            }
            DeviceType.DESK.type -> {
                label = R.string.desk
            }
            DeviceType.THREAD.type -> {
                label = R.string.mill
            }

            else -> {
                label = R.string.all_devices
            }
        }
        return label
    }

}