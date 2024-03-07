package com.flexispot.ble

import com.koylo.ble.flexispot.DeviceType

/**
 * @author luman
 * @date 19-11-26
 **/
object ImgRelation {

    /**
     * 用于设备列表页的图片对应
     */
    fun getTypeInDevices(type: Int): Int {
        val resource: Int
        when (type) {
            DeviceType.DESK.type -> {
                resource = R.mipmap.ic_desk_s
            }
            DeviceType.MEDIA.type -> {
                resource = R.mipmap.ic_media_s
            }
            DeviceType.RACK.type -> {
                resource = R.mipmap.ic_rask_s
            }
            DeviceType.THREAD.type -> {
                resource = R.mipmap.pic_treadmill_s
            }
            else -> {
                resource = R.mipmap.ic_desk_s
            }
        }
        return resource
    }

}